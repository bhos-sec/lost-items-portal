package tech.bhos.Lost_Items.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tech.bhos.Lost_Items.dto.AuthResponse;
import tech.bhos.Lost_Items.dto.LoginRequest;
import tech.bhos.Lost_Items.dto.RegisterRequest;
import tech.bhos.Lost_Items.dto.UserResponse;
import tech.bhos.Lost_Items.model.AppUser;
import tech.bhos.Lost_Items.model.RefreshToken;
import tech.bhos.Lost_Items.model.UserRole;
import tech.bhos.Lost_Items.repository.AppUserRepository;
import tech.bhos.Lost_Items.repository.RefreshTokenRepository;
import tech.bhos.Lost_Items.security.AppUserPrincipal;
import tech.bhos.Lost_Items.security.JwtService;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthService {
    private static final String REFRESH_COOKIE_NAME = "refreshToken";

    private final AppUserRepository appUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long refreshTokenExpirationSeconds;
    private final boolean refreshCookieSecure;
    private final String refreshCookieSameSite;

    public AuthService(
            AppUserRepository appUserRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${auth.refresh-token.expiration-seconds}") long refreshTokenExpirationSeconds,
            @Value("${auth.refresh-cookie.secure}") boolean refreshCookieSecure,
            @Value("${auth.refresh-cookie.same-site}") String refreshCookieSameSite
    ) {
        this.appUserRepository = appUserRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
        this.refreshCookieSecure = refreshCookieSecure;
        this.refreshCookieSameSite = refreshCookieSameSite;
    }

    public AuthResponse register(RegisterRequest request, HttpHeaders headers) {
        String normalizedEmail = normalizeEmail(request.email());
        if (appUserRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(CONFLICT, "Email is already in use");
        }

        AppUser user = appUserRepository.save(new AppUser(
                null,
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                UserRole.USER
        ));
        return issueSession(user, headers);
    }

    public AuthResponse login(LoginRequest request, HttpHeaders headers) {
        String normalizedEmail = normalizeEmail(request.email());
        AppUser user = appUserRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return issueSession(user, headers);
    }

    public AuthResponse refresh(String refreshToken, HttpHeaders headers) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Refresh token is missing");
        }

        RefreshToken existing = refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Refresh token is invalid"));

        if (existing.getExpiresAt().isBefore(Instant.now())) {
            existing.setRevoked(true);
            refreshTokenRepository.save(existing);
            throw new ResponseStatusException(UNAUTHORIZED, "Refresh token has expired");
        }

        AppUser user = appUserRepository.findById(existing.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found for refresh token"));

        existing.setRevoked(true);
        refreshTokenRepository.save(existing);
        return issueSession(user, headers);
    }

    public void logout(String refreshToken, HttpHeaders headers) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken).ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
        }
        headers.add(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString());
    }

    public UserResponse currentUser(AppUserPrincipal principal) {
        return new UserResponse(principal.userId(), principal.email(), principal.role().name());
    }

    private AuthResponse issueSession(AppUser user, HttpHeaders headers) {
        String accessToken = jwtService.generateAccessToken(user);

        RefreshToken refreshToken = refreshTokenRepository.save(new RefreshToken(
                null,
                UUID.randomUUID().toString(),
                user.getUserId(),
                Instant.now().plusSeconds(refreshTokenExpirationSeconds),
                false
        ));

        headers.add(HttpHeaders.SET_COOKIE, buildRefreshCookie(refreshToken.getToken()).toString());
        return new AuthResponse(
                accessToken,
                "Bearer",
                jwtService.accessTokenExpirationSeconds(),
                user.getUserId(),
                user.getEmail(),
                user.effectiveRole().name()
        );
    }

    private ResponseCookie buildRefreshCookie(String token) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path("/api/auth")
                .maxAge(refreshTokenExpirationSeconds)
                .build();
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path("/api/auth")
                .maxAge(0)
                .build();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
