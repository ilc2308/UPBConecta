package co.edu.upb.upbconecta.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final String COOKIE = "UPBC_SESSION";

    private final SesionService sesiones;
    private final boolean demoHabilitado;
    private final boolean cookieSegura;

    public AuthController(SesionService sesiones,
                          @Value("${app.demo.enabled:false}") boolean demoHabilitado,
                          @Value("${app.cookie.secure:true}") boolean cookieSegura) {
        this.sesiones = sesiones;
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

    @GetMapping("/me")
    public SesionService.Usuario me(@CookieValue(name = COOKIE, required = false) String id) {
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
