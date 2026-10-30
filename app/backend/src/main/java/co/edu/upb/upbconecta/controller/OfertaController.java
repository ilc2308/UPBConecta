package co.edu.upb.upbconecta.controller;

import co.edu.upb.upbconecta.dto.OfertaCreateDTO;
import co.edu.upb.upbconecta.dto.OfertaDTO;
import co.edu.upb.upbconecta.dto.OfertaResumenDTO;
import co.edu.upb.upbconecta.service.OfertaService;
import co.edu.upb.upbconecta.service.SesionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de ofertas de trabajo.
 * - POST /api/v1/ofertas        : publica un trabajo (UPBCONECT-39); 409 si es un envio duplicado (UPBCONECT-40).
 * - GET  /api/v1/ofertas        : lista trabajos activos para el Dashboard (UPBCONECT-37). Exige sesion.
 * - GET  /api/v1/ofertas/mias   : publicaciones del usuario de la sesion (UPBCONECT-40).
 *
 * TODO(UPBCONECT-5/6): cuando llegue el JWT institucional, reemplazar
 *   @AuthenticationPrincipal SesionService.Usuario por el principal del JWT.
 */
@Validated
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

    /**
     * Listado paginado de ofertas ABIERTAS y vigentes, ordenadas de la mas reciente a la mas antigua.
     * Solo se devuelven ofertas cuya fecha_limite es estrictamente posterior al momento de la consulta.
     *
     * @param page pagina solicitada, base 0 (default: 0)
     * @param size elementos por pagina, entre 1 y 50 (default: 10)
     */
    @GetMapping
    public Page<OfertaResumenDTO> listarActivas(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return ofertaService.listarActivas(page, size);
    }

    @GetMapping("/mias")
    public List<OfertaDTO> mias(@AuthenticationPrincipal SesionService.Usuario usuario) {
        return ofertaService.misOfertas(usuario);
    }
}
