package co.edu.upb.upbconecta.exception;

/** Se lanza cuando un recurso pedido (oferta, usuario, etc.) no existe. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
