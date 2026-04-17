package tech.bhos.Lost_Items.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Long userId,
        String email
) {
}
