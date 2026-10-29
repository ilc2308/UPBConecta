package co.edu.upb.upbconecta.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Se lanza cuando el usuario no tiene permisos para el recurso (403). */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String message) {
        super(message);
    }
    public AccesoDenegadoException() {
        super("No tienes permisos para acceder a este recurso");
    }
}