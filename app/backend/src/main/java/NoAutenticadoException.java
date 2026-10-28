package co.edu.upb.upbconecta.controller;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Se lanza cuando el usuario no esta autenticado (401). */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class NoAutenticadoException extends RuntimeException {
    public NoAutenticadoException(String message) {
        super(message);
    }
    public NoAutenticadoException() {
        super("Se requiere autenticacion para acceder a este recurso");
    }
}