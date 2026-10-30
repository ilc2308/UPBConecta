package co.edu.upb.upbconecta.controller;

import co.edu.upb.upbconecta.dto.OfertaCreateDTO;
import co.edu.upb.upbconecta.dto.OfertaDTO;
import co.edu.upb.upbconecta.service.OfertaService;
import co.edu.upb.upbconecta.service.SesionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Ofertas de trabajo. Todos los endpoints exigen sesion (SecurityConfig responde 401 si no hay).
 * - POST /api/v1/ofertas: publica un trabajo (UPBCONECT-39); 409 si es un envio duplicado (UPBCONECT-40).
 * - GET  /api/v1/ofertas/mias: publicaciones del usuario de la sesion (UPBCONECT-40).
 */
@RestController
@RequestMapping("/ofertas")
public class OfertaController {

    private final OfertaService ofertaService;

    public OfertaController(OfertaService ofertaService) {
        this.ofertaService = ofertaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OfertaDTO crear(@Valid @RequestBody OfertaCreateDTO datos,
                           @AuthenticationPrincipal SesionService.Usuario usuario) {
        return ofertaService.crear(datos, usuario);
    }

    @GetMapping("/mias")
    public List<OfertaDTO> mias(@AuthenticationPrincipal SesionService.Usuario usuario) {
        return ofertaService.misOfertas(usuario);
    }
}
