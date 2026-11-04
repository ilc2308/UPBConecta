package co.edu.upb.upbconecta.dto;

import java.time.Instant;

public record AuthErrorResponse(String codigo, String mensaje, Instant timestamp) {

    public static AuthErrorResponse of(String codigo, String mensaje) {
        return new AuthErrorResponse(codigo, mensaje, Instant.now());
    }
}
