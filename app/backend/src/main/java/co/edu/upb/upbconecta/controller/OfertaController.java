package co.edu.upb.upbconecta.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.upb.upbconecta.dto.OfertaCreateDTO;
import co.edu.upb.upbconecta.dto.OfertaDTO;
import co.edu.upb.upbconecta.dto.OfertaDetalleDTO;
import co.edu.upb.upbconecta.dto.OfertaResumenDTO;
import co.edu.upb.upbconecta.dto.PostulanteDTO;
import co.edu.upb.upbconecta.exception.NoAutenticadoException;
import co.edu.upb.upbconecta.repository.UsuarioRepository;
import co.edu.upb.upbconecta.service.OfertaService;
import co.edu.upb.upbconecta.service.PostulacionService;
import co.edu.upb.upbconecta.service.SesionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Endpoints de ofertas de trabajo.
 * - POST /api/v1/ofertas        : publica un trabajo (UPBCONECT-39); 409 si es un envio duplicado (UPBCONECT-40).
 * - GET  /api/v1/ofertas        : lista trabajos activos para el Dashboard (UPBCONECT-37). Exige sesion.
 * - GET  /api/v1/ofertas/mias   : publicaciones del usuario de la sesion (UPBCONECT-40).
 * - GET  /api/v1/ofertas/asignadas : trabajos asignados al usuario de la sesion (UPBCONECT-43).
 * - GET  /api/v1/ofertas/{id}   : detalle de una oferta (UPBCONECT-41).
 * - POST /api/v1/ofertas/{id}/postulaciones : postula al estudiante de la sesion (UPBCONECT-41).
 * - GET  /api/v1/ofertas/{id}/postulaciones : postulantes de la oferta, solo para quien la publico.
 * - PATCH /api/v1/ofertas/{id}/postulaciones/{postulacionId}/seleccionar : asigna la oferta (UPBCONECT-43).
 *
 * Soporta tanto autenticacion JWT institucional (SSO Microsoft en produccion) como la sesion demo heredada.
 */
@Validated
@RestController
@RequestMapping("/ofertas")
public class OfertaController {

    private final OfertaService ofertaService;
    private final PostulacionService postulacionService;
    private final UsuarioRepository usuarioRepository;

    public OfertaController(OfertaService ofertaService,
                            PostulacionService postulacionService,
                            @Autowired(required = false) UsuarioRepository usuarioRepository) {
        this.ofertaService = ofertaService;
        this.postulacionService = postulacionService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OfertaDTO crear(@Valid @RequestBody OfertaCreateDTO datos, Authentication auth) {
        SesionService.Usuario usuario = extraerUsuario(auth);
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
    public     public Page<OfertaResumenDTO> listarActivas(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        if (q == null || q.isBlank()) {
            return ofertaService.listarActivas(page, size);
        }
        return ofertaService.buscarActivas(q, page, size);
    }

    @GetMapping("/{id}")
    public OfertaDetalleDTO obtenerDetalle(@PathVariable Long id, Authentication auth) {
        return ofertaService.obtenerDetalle(id, extraerUsuario(auth));
    }

    @PostMapping("/{id}/postulaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public PostulanteDTO postular(@PathVariable Long id, Authentication auth) {
        return postulacionService.postular(id, extraerUsuario(auth));
    }

    @GetMapping("/{id}/postulaciones")
    public List<PostulanteDTO> postulantes(@PathVariable Long id, Authentication auth) {
        return postulacionService.listar(id, extraerUsuario(auth));
    }

    @PatchMapping("/{id}/postulaciones/{postulacionId}/seleccionar")
    public List<PostulanteDTO> seleccionar(@PathVariable Long id, @PathVariable Long postulacionId,
                                           Authentication auth) {
        return postulacionService.seleccionar(id, postulacionId, extraerUsuario(auth));
    }

    @GetMapping("/asignadas")
    public List<OfertaDTO> asignadas(Authentication auth) {
        return ofertaService.tareasPendientes(extraerUsuario(auth));
    }

    @GetMapping("/mias")
    public List<OfertaDTO> mias(Authentication auth) {
        SesionService.Usuario usuario = extraerUsuario(auth);
        return ofertaService.misOfertas(usuario);
    }

    private SesionService.Usuario extraerUsuario(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new NoAutenticadoException("No hay sesion activa");
        }
        if (auth.getPrincipal() instanceof SesionService.Usuario demoUser) {
            return demoUser;
        }
        if (auth.getPrincipal() instanceof Jwt jwt) {
            String correo = jwt.getClaimAsString("email");
            if (correo == null || correo.isBlank()) {
                correo = jwt.getClaimAsString("preferred_username");
            }
            if (correo != null && !correo.isBlank()) {
                String nombre = jwt.getClaimAsString("nombre");
                if (nombre == null || nombre.isBlank()) {
                    nombre = jwt.getClaimAsString("name");
                }
                return new SesionService.Usuario(correo, nombre != null ? nombre : correo);
            }
            if (usuarioRepository != null) {
                try {
                    return usuarioRepository.findById(UUID.fromString(jwt.getSubject()))
                            .map(u -> new SesionService.Usuario(u.getCorreo(), u.getNombre()))
                            .orElseThrow(() -> new NoAutenticadoException("Usuario institucional no encontrado"));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        String name = auth.getName();
        if (name != null && !name.isBlank()) {
            if (name.contains("@")) {
                return new SesionService.Usuario(name, name);
            }
            if (usuarioRepository != null) {
                try {
                    return usuarioRepository.findById(UUID.fromString(name))
                            .map(u -> new SesionService.Usuario(u.getCorreo(), u.getNombre()))
                            .orElseThrow(() -> new NoAutenticadoException("Usuario institucional no encontrado"));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        throw new NoAutenticadoException("No se pudo identificar al usuario autenticado");
    }
}
