package co.edu.upb.upbconecta.service;

import co.edu.upb.upbconecta.dto.AuthResponse;
import co.edu.upb.upbconecta.dto.UsuarioResponse;
import co.edu.upb.upbconecta.exception.AccesoDenegadoException;
import co.edu.upb.upbconecta.exception.ConflictoNegocioException;
import co.edu.upb.upbconecta.exception.NoAutenticadoException;
import co.edu.upb.upbconecta.exception.ResourceNotFoundException;
import co.edu.upb.upbconecta.model.RolUsuario;
import co.edu.upb.upbconecta.model.UsuarioEntity;
import co.edu.upb.upbconecta.repository.UsuarioRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MicrosoftAuthService {

    private static final String DOMINIO_INSTITUCIONAL = "@upb.edu.co";

    private final UsuarioRepository usuarios;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder microsoftDecoder;

    public MicrosoftAuthService(
            UsuarioRepository usuarios,
            JwtEncoder jwtEncoder,
            @Value("${app.microsoft.tenant-id}") String tenantId,
            @Value("${app.microsoft.client-id}") String clientId) {
        this.usuarios = usuarios;
        this.jwtEncoder = jwtEncoder;
        this.microsoftDecoder = crearDecoderMicrosoft(tenantId, clientId);
    }

    @Transactional
    public AuthResponse registrar(String idToken, RolUsuario rol, String carrera) {
        Jwt microsoft = validarTokenMicrosoft(idToken);
        Identidad identidad = extraerIdentidad(microsoft);
        if (usuarios.existsByMicrosoftOidOrCorreo(identidad.oid(), identidad.correo())) {
            throw new ConflictoNegocioException("La cuenta ya existe. Inicia sesion con Microsoft.");
        }
        try {
            UsuarioEntity usuario = usuarios.saveAndFlush(new UsuarioEntity(
                    identidad.oid(), identidad.correo(), identidad.nombre(), rol, carrera.trim()));
            return emitirToken(usuario);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictoNegocioException("La cuenta ya existe. Inicia sesion con Microsoft.");
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse iniciarSesion(String idToken) {
        Identidad identidad = extraerIdentidad(validarTokenMicrosoft(idToken));
        UsuarioEntity usuario = usuarios.findByMicrosoftOid(identidad.oid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe una cuenta registrada. Completa el registro."));
        if (!usuario.isActivo()) {
            throw new AccesoDenegadoException("La cuenta esta inactiva");
        }
        return emitirToken(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarUsuario(String subject) {
        try {
            return usuarios.findById(java.util.UUID.fromString(subject))
                    .filter(UsuarioEntity::isActivo)
                    .map(UsuarioResponse::desde)
                    .orElseThrow(() -> new NoAutenticadoException("La cuenta ya no esta disponible"));
        } catch (IllegalArgumentException ex) {
            throw new NoAutenticadoException("Token de plataforma invalido");
        }
    }

    private JwtDecoder crearDecoderMicrosoft(String tenantId, String clientId) {
        if (tenantId.isBlank() || clientId.isBlank()) {
            return token -> {
                throw new NoAutenticadoException(
                        "El inicio de sesion Microsoft no esta configurado en el servidor");
            };
        }
        String issuer = "https://login.microsoftonline.com/" + tenantId + "/v2.0";
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(
                "https://login.microsoftonline.com/" + tenantId + "/discovery/v2.0/keys").build();
        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> audienceValidator = jwt ->
                jwt.getAudience().contains(clientId)
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(
                                new OAuth2Error("invalid_token", "Audience Microsoft no valida", null));
        decoder.setJwtValidator(jwt -> {
            OAuth2TokenValidatorResult issuerResult = issuerValidator.validate(jwt);
            return issuerResult.hasErrors() ? issuerResult : audienceValidator.validate(jwt);
        });
        return decoder;
    }

    private Jwt validarTokenMicrosoft(String idToken) {
        try {
            return microsoftDecoder.decode(idToken);
        } catch (org.springframework.security.oauth2.jwt.JwtException ex) {
            throw new NoAutenticadoException("El token de Microsoft no es valido o ha expirado");
        }
    }

    private Identidad extraerIdentidad(Jwt token) {
        String correo = token.getClaimAsString("preferred_username");
        if (correo == null || correo.isBlank()) {
            correo = token.getClaimAsString("email");
        }
        if (correo == null || !correo.toLowerCase(java.util.Locale.ROOT).endsWith(DOMINIO_INSTITUCIONAL)) {
            throw new AccesoDenegadoException("Dominio de correo no permitido. Usa tu cuenta @upb.edu.co.");
        }
        String oid = token.getClaimAsString("oid");
        String nombre = token.getClaimAsString("name");
        if (oid == null || oid.isBlank() || nombre == null || nombre.isBlank()) {
            throw new NoAutenticadoException("Microsoft no entrego los datos requeridos de la cuenta");
        }
        return new Identidad(oid, correo.toLowerCase(java.util.Locale.ROOT), nombre);
    }

    private AuthResponse emitirToken(UsuarioEntity usuario) {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("upbconecta")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(8, ChronoUnit.HOURS))
                .subject(usuario.getId().toString())
                .claim("rol", usuario.getRol().name())
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new AuthResponse(token, UsuarioResponse.desde(usuario));
    }

    private record Identidad(String oid, String correo, String nombre) {}
}
