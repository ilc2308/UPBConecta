package co.edu.upb.upbconecta.config;

import co.edu.upb.upbconecta.service.SesionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee la cookie de sesion (HttpOnly) y, si corresponde a una sesion valida,
 * marca la peticion como autenticada para Spring Security.
 */
public class SesionCookieFilter extends OncePerRequestFilter {

    static final String COOKIE = "UPBC_SESSION";

    private final SesionService sesiones;

    public SesionCookieFilter(SesionService sesiones) {
        this.sesiones = sesiones;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            for (Cookie cookie : cookies) {
                if (COOKIE.equals(cookie.getName())) {
                    sesiones.buscar(cookie.getValue()).ifPresent(usuario ->
                        SecurityContextHolder.getContext().setAuthentication(
                            UsernamePasswordAuthenticationToken.authenticated(usuario, null, List.of())));
                }
            }
        }
        chain.doFilter(request, response);
    }
}
