package co.edu.upb.upbconecta.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
public class JwtConfig {

    @Bean
    SecretKey platformJwtKey(@Value("${app.jwt.secret}") String encodedSecret) {
        if (encodedSecret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET debe configurarse como una clave Base64");
        }
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(encodedSecret);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("JWT_SECRET debe ser una clave Base64 valida", ex);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET debe contener al menos 32 bytes");
        }
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey platformJwtKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(platformJwtKey));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey platformJwtKey) {
        return NimbusJwtDecoder.withSecretKey(platformJwtKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
