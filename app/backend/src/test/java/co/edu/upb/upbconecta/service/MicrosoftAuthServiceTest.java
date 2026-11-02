package co.edu.upb.upbconecta.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import co.edu.upb.upbconecta.controller.GlobalExceptionHandler;
import co.edu.upb.upbconecta.exception.AccesoDenegadoException;
import co.edu.upb.upbconecta.exception.ConflictoNegocioException;
import co.edu.upb.upbconecta.exception.NoAutenticadoException;
import co.edu.upb.upbconecta.exception.ResourceNotFoundException;
import co.edu.upb.upbconecta.model.RolUsuario;
import co.edu.upb.upbconecta.model.UsuarioEntity;
import co.edu.upb.upbconecta.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

class MicrosoftAuthServiceTest {

    private static final String ISSUER = "https://login.microsoftonline.com/test-tenant/v2.0";
    private static final String CLIENT_ID = "test-client";
    private static final SecretKey TEST_KEY = new SecretKeySpec(
            Base64.getDecoder().decode("MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="), "HmacSHA256");

    private UsuarioRepository usuarios;
    private MicrosoftAuthService service;
    private JwtEncoder tokenEncoder;

    @BeforeEach
    void setUp() {
        usuarios = mock(UsuarioRepository.class);
        tokenEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(TEST_KEY));

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(TEST_KEY)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(jwt -> {
            var issuerResult = JwtValidators.createDefaultWithIssuer(ISSUER).validate(jwt);
            if (issuerResult.hasErrors()) {
                return issuerResult;
            }
            return jwt.getAudience().contains(CLIENT_ID)
                    ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(
                            new OAuth2Error("invalid_token", "Audience Microsoft no valida", null));
        });
        service = new MicrosoftAuthService(usuarios, mock(JwtEncoder.class), decoder);
    }

    @Test
    void correoFueraDelDominioDevuelve403() {
        AccesoDenegadoException exception = assertThrows(
                AccesoDenegadoException.class,
                () -> service.registrar(token("outside@example.com", ISSUER, CLIENT_ID),
                        RolUsuario.ESTUDIANTE, "Ingenieria"));

        assertEquals(HttpStatus.FORBIDDEN, statusOf(exception));
    }

    @Test
    void issuerIncorrectoDevuelve401() {
        NoAutenticadoException exception = assertThrows(
                NoAutenticadoException.class,
                () -> service.registrar(token("persona@upb.edu.co", "https://issuer-incorrecto", CLIENT_ID),
                        RolUsuario.ESTUDIANTE, "Ingenieria"));

        assertEquals(HttpStatus.UNAUTHORIZED, statusOf(exception));
    }

    @Test
    void audienceIncorrectoDevuelve401() {
        NoAutenticadoException exception = assertThrows(
                NoAutenticadoException.class,
                () -> service.registrar(token("persona@upb.edu.co", ISSUER, "another-client"),
                        RolUsuario.ESTUDIANTE, "Ingenieria"));

        assertEquals(HttpStatus.UNAUTHORIZED, statusOf(exception));
    }

    @Test
    void registroDuplicadoDevuelve409() {
        when(usuarios.existsByMicrosoftOidOrCorreo("microsoft-oid", "persona@upb.edu.co"))
                .thenReturn(true);

        ConflictoNegocioException exception = assertThrows(
                ConflictoNegocioException.class,
                () -> service.registrar(token("persona@upb.edu.co", ISSUER, CLIENT_ID),
                        RolUsuario.ESTUDIANTE, "Ingenieria"));

        assertEquals(HttpStatus.CONFLICT, statusOf(exception));
    }

    @Test
    void loginSinRegistroPrevioDevuelve404() {
        when(usuarios.findByMicrosoftOid("microsoft-oid")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.iniciarSesion(token("persona@upb.edu.co", ISSUER, CLIENT_ID)));

        assertEquals(HttpStatus.NOT_FOUND, statusOf(exception));
    }

    @Test
    void cuentaInactivaDevuelve403() {
        UsuarioEntity usuario = new UsuarioEntity(
                "microsoft-oid", "persona@upb.edu.co", "Persona", RolUsuario.ESTUDIANTE, "Ingenieria");
        ReflectionTestUtils.setField(usuario, "activo", false);
        when(usuarios.findByMicrosoftOid("microsoft-oid")).thenReturn(Optional.of(usuario));

        AccesoDenegadoException exception = assertThrows(
                AccesoDenegadoException.class,
                () -> service.iniciarSesion(token("persona@upb.edu.co", ISSUER, CLIENT_ID)));

        assertEquals(HttpStatus.FORBIDDEN, statusOf(exception));
    }

    private String token(String correo, String issuer, String audience) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .audience(List.of(audience))
                .issuedAt(now.minusSeconds(1))
                .expiresAt(now.plusSeconds(60))
                .subject("microsoft-subject")
                .claim("preferred_username", correo)
                .claim("oid", "microsoft-oid")
                .claim("name", "Persona")
                .build();
        return tokenEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    private HttpStatus statusOf(RuntimeException exception) {
        if (exception instanceof ResourceNotFoundException notFound) {
            return HttpStatus.valueOf(new GlobalExceptionHandler().noEncontrado(notFound).getStatus());
        }
        return exception.getClass().getAnnotation(ResponseStatus.class).value();
    }
}
