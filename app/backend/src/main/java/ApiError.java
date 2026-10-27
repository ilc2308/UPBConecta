package co.edu.upb.upbconecta.dto;

import java.time.Instant;
import java.util.Map;

/** Formato único de error que devuelve la API. */
public record ApiError(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp,
        Map<String, String> fieldErrors) {

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(status, error, message, path, Instant.now(), Map.of());
    }
}
