package com.clinica.seguridad.dto;

public record LoginResponse(
    String token,
    String type,
    String username,
    Long expiresIn
) {
    public static LoginResponse fromToken(String token, String username, Long expiresIn) {
        return new LoginResponse(token, "Bearer", username, expiresIn);
    }
}
