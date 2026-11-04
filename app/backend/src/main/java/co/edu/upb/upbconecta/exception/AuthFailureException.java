package co.edu.upb.upbconecta.exception;

import org.springframework.http.HttpStatus;

public class AuthFailureException extends RuntimeException {

    private final String codigo;
    private final HttpStatus status;

    public AuthFailureException(String codigo, String mensaje, HttpStatus status) {
        super(mensaje);
        this.codigo = codigo;
        this.status = status;
    }

    public String codigo() {
        return codigo;
    }

    public HttpStatus status() {
        return status;
    }
}
