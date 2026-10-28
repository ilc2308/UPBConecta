package co.edu.upb.upbconecta.config;

import co.edu.upb.upbconecta.controller.SesionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Seguridad base de la API.
 * - El health check, los preflight de CORS y los endpoints de sesion (/auth/**) son publicos.
 * - La sesion viaja en una cookie HttpOnly que SesionCookieFilter traduce a una autenticacion.
 * - CSRF desactivado: la cookie es SameSite=Strict, por lo que el navegador no la envia
 *   en peticiones originadas desde otros sitios.
 * - El resto de endpoints exige autenticacion (401 si no hay sesion).
 * - El login con Microsoft (SSO) reemplazara el modo demo en UPBCONECT-5 y UPBCONECT-6.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SesionService sesiones) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .addFilterBefore(new SesionCookieFilter(sesiones), UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(ApiPrefixConfig.API_PREFIX + "/health").permitAll()
                .requestMatchers(ApiPrefixConfig.API_PREFIX + "/auth/**").permitAll()
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated());

        return http.build();
    }
}
