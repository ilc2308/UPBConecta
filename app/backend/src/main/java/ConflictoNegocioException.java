package co.edu.upb.upbconecta.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Se lanza cuando hay un conflicto de negocio (409), como un recurso duplicado. */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictoNegocioException extends RuntimeException {
    public ConflictoNegocioException(String message) {
        super(message);
    }
}