package co.edu.upb.upbconecta.controller;

import co.edu.upb.upbconecta.dto.AuthErrorResponse;
import co.edu.upb.upbconecta.exception.AccesoDenegadoException;
import co.edu.upb.upbconecta.exception.AuthFailureException;
import co.edu.upb.upbconecta.exception.ConflictoNegocioException;
import co.edu.upb.upbconecta.exception.NoAutenticadoException;
import co.edu.upb.upbconecta.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AuthExceptionHandler.class);

    @ExceptionHandler(AuthFailureException.class)
    public ResponseEntity<AuthErrorResponse> authFailure(AuthFailureException ex, HttpServletRequest request) {
        logIntentoFallido(request, ex.codigo());
        return response(ex.status(), ex.codigo(), ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<AuthErrorResponse> noEncontrado(ResourceNotFoundException ex, HttpServletRequest request) {
        logIntentoFallido(request, ex.code());
        return response(HttpStatus.NOT_FOUND, ex.code(), ex.getMessage());
    }

    @ExceptionHandler(NoAutenticadoException.class)
    public ResponseEntity<AuthErrorResponse> noAutenticado(NoAutenticadoException ex, HttpServletRequest request) {
        logIntentoFallido(request, "INVALID_TOKEN");
        return response(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "El token no es válido o ha vencido.");
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<AuthErrorResponse> accesoDenegado(AccesoDenegadoException ex, HttpServletRequest request) {
        logIntentoFallido(request, "FORBIDDEN");
        return response(HttpStatus.FORBIDDEN, "FORBIDDEN", "No fue posible autorizar el acceso.");
    }

    @ExceptionHandler(ConflictoNegocioException.class)
    public ResponseEntity<AuthErrorResponse> conflicto(ConflictoNegocioException ex, HttpServletRequest request) {
        logIntentoFallido(request, "CONFLICT");
        return response(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<AuthErrorResponse> solicitudInvalida(Exception ex, HttpServletRequest request) {
        logIntentoFallido(request, "INVALID_REQUEST");
        return response(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Los datos enviados no son válidos.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthErrorResponse> errorNoControlado(Exception ex, HttpServletRequest request) {
        logIntentoFallido(request, "PROVIDER_UNAVAILABLE");
        log.error("Fallo no controlado en autenticación: {}", ex.getClass().getSimpleName());
        return response(HttpStatus.SERVICE_UNAVAILABLE, "PROVIDER_UNAVAILABLE",
                "No fue posible validar tu cuenta en este momento. Inténtalo de nuevo.");
    }

    private void logIntentoFallido(HttpServletRequest request, String codigo) {
        log.warn("Intento de autenticación fallido en {} ({})", request.getRequestURI(), codigo);
    }

    private ResponseEntity<AuthErrorResponse> response(HttpStatus status, String codigo, String mensaje) {
        return ResponseEntity.status(status).body(AuthErrorResponse.of(codigo, mensaje));
    }
}
