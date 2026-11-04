package co.edu.upb.upbconecta.controller;

import co.edu.upb.upbconecta.dto.AuthResponse;
import co.edu.upb.upbconecta.dto.MicrosoftRegisterRequest;
import co.edu.upb.upbconecta.dto.MicrosoftTokenRequest;
import co.edu.upb.upbconecta.dto.UsuarioResponse;
import co.edu.upb.upbconecta.exception.AccesoDenegadoException;
import co.edu.upb.upbconecta.exception.NoAutenticadoException;
import co.edu.upb.upbconecta.service.MicrosoftAuthService;
import co.edu.upb.upbconecta.service.SesionService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String COOKIE = "UPBC_SESSION";

    private final SesionService sesiones;
    private final MicrosoftAuthService microsoftAuth;
    private final boolean demoHabilitado;
    private final boolean cookieSegura;

    public AuthController(SesionService sesiones, MicrosoftAuthService microsoftAuth,
                          @Value("${app.demo.enabled:false}") boolean demoHabilitado,
                          @Value("${app.cookie.secure:true}") boolean cookieSegura) {
        this.sesiones = sesiones;
        this.microsoftAuth = microsoftAuth;
        this.demoHabilitado = demoHabilitado;
        this.cookieSegura = cookieSegura;
    }

    @PostMapping("/demo")
    public ResponseEntity<SesionService.Usuario> loginDemo() {
        if (!demoHabilitado) {
            throw new AccesoDenegadoException("El modo demo esta deshabilitado");
        }
        SesionService.Usuario usuario = new SesionService.Usuario("demo@upb.edu.co", "Usuario Demo");
        String id = sesiones.crear(usuario);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie(id, Duration.ofHours(8)).toString())
                .body(usuario);
    }

    @PostMapping("/microsoft/register")
    public AuthResponse registrarMicrosoft(@Valid @RequestBody MicrosoftRegisterRequest request) {
        return microsoftAuth.registrar(request.idToken(), request.rol(), request.carrera());
    }

    @PostMapping("/microsoft/login")
    public AuthResponse iniciarSesionMicrosoft(@Valid @RequestBody MicrosoftTokenRequest request) {
        return microsoftAuth.iniciarSesion(request.idToken());
    }

    @GetMapping("/me")
    public Object me(@CookieValue(name = COOKIE, required = false) String id,
                     @AuthenticationPrincipal Jwt jwt) {
        if (jwt != null) {
            return microsoftAuth.buscarUsuario(jwt.getSubject());
        }
        return sesiones.buscar(id)
                .orElseThrow(() -> new NoAutenticadoException("No hay sesion activa"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = COOKIE, required = false) String id) {
        sesiones.eliminar(id);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString())
                .build();
    }

    private ResponseCookie cookie(String valor, Duration duracion) {
        return ResponseCookie.from(COOKIE, valor)
                .httpOnly(true)
                .secure(cookieSegura)
                .sameSite("Strict")
                .path("/")
                .maxAge(duracion)
                .build();
    }
}
