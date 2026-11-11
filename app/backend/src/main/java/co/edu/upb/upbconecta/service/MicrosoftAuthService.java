package co.edu.upb.upbconecta.service;

import co.edu.upb.upbconecta.dto.AuthResponse;
import co.edu.upb.upbconecta.dto.UsuarioResponse;
import co.edu.upb.upbconecta.exception.AccesoDenegadoException;
import co.edu.upb.upbconecta.exception.AuthFailureException;
import co.edu.upb.upbconecta.exception.ConflictoNegocioException;
import co.edu.upb.upbconecta.exception.NoAutenticadoException;
import co.edu.upb.upbconecta.exception.ResourceNotFoundException;
import co.edu.upb.upbconecta.model.RolUsuario;
import co.edu.upb.upbconecta.model.UsuarioEntity;
import co.edu.upb.upbconecta.repository.UsuarioRepository;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.oauth2.jwt.JwtValidationException;
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
    private final long tokenExpirationHours;

    @Autowired
    public MicrosoftAuthService(
            UsuarioRepository usuarios,
            JwtEncoder jwtEncoder,
            @Value("${app.microsoft.tenant-id}") String tenantId,
            @Value("${app.microsoft.client-id}") String clientId,
            @Value("${app.jwt.expiration-hours:8}") long tokenExpirationHours) {
        this(usuarios, jwtEncoder, crearDecoderMicrosoft(tenantId, clientId), tokenExpirationHours);
    }

    MicrosoftAuthService(
            UsuarioRepository usuarios,
            JwtEncoder jwtEncoder,
            JwtDecoder microsoftDecoder) {
        this(usuarios, jwtEncoder, microsoftDecoder, 8);
    }

    MicrosoftAuthService(
            UsuarioRepository usuarios,
            JwtEncoder jwtEncoder,
            JwtDecoder microsoftDecoder,
            long tokenExpirationHours) {
        if (tokenExpirationHours <= 0) {
            throw new IllegalArgumentException("La vigencia del JWT debe ser mayor que cero horas");
        }
        this.usuarios = usuarios;
        this.jwtEncoder = jwtEncoder;
        this.microsoftDecoder = microsoftDecoder;
        this.tokenExpirationHours = tokenExpirationHours;
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
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_REGISTERED",
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

    /** Cambia el rol activo del usuario y emite un JWT nuevo con el claim "rol" actualizado. */
    @Transactional
    public AuthResponse cambiarRol(String subject, RolUsuario nuevoRol) {
        UsuarioEntity usuario;
        try {
            usuario = usuarios.findById(java.util.UUID.fromString(subject))
                    .filter(UsuarioEntity::isActivo)
                    .orElseThrow(() -> new NoAutenticadoException("La cuenta ya no esta disponible"));
        } catch (IllegalArgumentException ex) {
            throw new NoAutenticadoException("Token de plataforma invalido");
        }
        usuario.cambiarRol(nuevoRol);
        return emitirToken(usuario);
    }

    private static JwtDecoder crearDecoderMicrosoft(String tenantId, String clientId) {
        if (tenantId.isBlank() || clientId.isBlank()) {
            return token -> {
                throw new AuthFailureException("PROVIDER_UNAVAILABLE",
                        "No fue posible validar tu cuenta en este momento.",
                        org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE);
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
            if (!(ex instanceof JwtValidationException)) {
                Throwable cause = ex;
                while (cause.getCause() != null && cause.getCause() != cause) {
                    cause = cause.getCause();
                }
                if (cause instanceof IOException) {
                    throw new AuthFailureException("PROVIDER_UNAVAILABLE",
                            "No fue posible validar tu cuenta en este momento.",
                            org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE);
                }
            }
            throw new AuthFailureException("INVALID_TOKEN",
                    "El token no es válido o ha vencido.", org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
    }

    private Identidad extraerIdentidad(Jwt token) {
        String correo = token.getClaimAsString("preferred_username");
        if (correo == null || correo.isBlank()) {
            correo = token.getClaimAsString("email");
        }
        if (correo == null || !correo.toLowerCase(java.util.Locale.ROOT).endsWith(DOMINIO_INSTITUCIONAL)) {
            throw new AuthFailureException("DOMAIN_NOT_ALLOWED",
                    "Solo se permiten cuentas institucionales de la UPB.",
                    org.springframework.http.HttpStatus.FORBIDDEN);
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
                .expiresAt(ahora.plus(tokenExpirationHours, ChronoUnit.HOURS))
                .subject(usuario.getId().toString())
                .claim("email", usuario.getCorreo())
                .claim("nombre", usuario.getNombre())
                .claim("rol", usuario.getRol().name())
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new AuthResponse(token, UsuarioResponse.desde(usuario));
    }

    private record Identidad(String oid, String correo, String nombre) {}
}
