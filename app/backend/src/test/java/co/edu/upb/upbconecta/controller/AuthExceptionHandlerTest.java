package co.edu.upb.upbconecta.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import co.edu.upb.upbconecta.dto.AuthErrorResponse;
import co.edu.upb.upbconecta.exception.AuthFailureException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class AuthExceptionHandlerTest {

    private final AuthExceptionHandler handler = new AuthExceptionHandler();

    @Test
    void errorIncluyeCodigoMensajeYFechaEnFormatoSeguro() {
        ResponseEntity<AuthErrorResponse> response = handler.authFailure(
                new AuthFailureException("INVALID_TOKEN", "El token no es válido.", HttpStatus.UNAUTHORIZED),
                new MockHttpServletRequest("POST", "/api/v1/auth/microsoft/login"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("INVALID_TOKEN", response.getBody().codigo());
        assertEquals("El token no es válido.", response.getBody().mensaje());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void usuarioNoRegistradoConservaCodigoDeDominio() {
        ResponseEntity<AuthErrorResponse> response = handler.noEncontrado(
                new co.edu.upb.upbconecta.exception.ResourceNotFoundException(
                        "USER_NOT_REGISTERED", "Completa tu registro."),
                new MockHttpServletRequest("POST", "/api/v1/auth/microsoft/login"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("USER_NOT_REGISTERED", response.getBody().codigo());
    }
}
