package tech.bhos.Lost_Items;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tech.bhos.Lost_Items.dto.AuthResponse;
import tech.bhos.Lost_Items.dto.LoginRequest;
import tech.bhos.Lost_Items.dto.RegisterRequest;
import tech.bhos.Lost_Items.dto.UserResponse;
import tech.bhos.Lost_Items.security.AppUserPrincipal;
import tech.bhos.Lost_Items.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        HttpHeaders headers = new HttpHeaders();
        AuthResponse response = authService.register(request, headers);
        return ResponseEntity.ok().headers(headers).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        HttpHeaders headers = new HttpHeaders();
        AuthResponse response = authService.login(request, headers);
        return ResponseEntity.ok().headers(headers).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        HttpHeaders headers = new HttpHeaders();
        AuthResponse response = authService.refresh(refreshToken, headers);
        return ResponseEntity.ok().headers(headers).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        HttpHeaders headers = new HttpHeaders();
        authService.logout(refreshToken, headers);
        return ResponseEntity.noContent().headers(headers).build();
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AppUserPrincipal principal) {
        return authService.currentUser(principal);
    }
}
