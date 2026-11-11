package co.edu.upb.upbconecta.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.upb.upbconecta.domain.EstadoOferta;
import co.edu.upb.upbconecta.domain.Oferta;
import co.edu.upb.upbconecta.dto.ActualizarEstadoOfertaDTO;
import co.edu.upb.upbconecta.dto.OfertaCreateDTO;
import co.edu.upb.upbconecta.dto.OfertaDTO;
import co.edu.upb.upbconecta.dto.OfertaDetalleDTO;
import co.edu.upb.upbconecta.dto.OfertaResumenDTO;
import co.edu.upb.upbconecta.exception.AccesoDenegadoException;
import co.edu.upb.upbconecta.exception.ConflictoNegocioException;
import co.edu.upb.upbconecta.exception.ResourceNotFoundException;
import co.edu.upb.upbconecta.model.UsuarioEntity;
import co.edu.upb.upbconecta.repository.OfertaRepository;
import co.edu.upb.upbconecta.repository.PostulacionRepository;
import co.edu.upb.upbconecta.repository.UsuarioRepository;

/** Reglas de negocio de las ofertas (UPBCONECT-39, 40, 37, 41, 43 y 44). */
@Service
public class OfertaService {

    /** Ventana en la que una oferta con el mismo titulo se considera un envio duplicado (doble clic). */
    static final Duration VENTANA_DUPLICADO = Duration.ofMinutes(1);

    private final OfertaRepository ofertas;
    private final UsuarioRepository usuarios;
    private final PostulacionRepository postulaciones;

    public OfertaService(OfertaRepository ofertas, UsuarioRepository usuarios,
                         PostulacionRepository postulaciones) {
        this.ofertas = ofertas;
        this.usuarios = usuarios;
        this.postulaciones = postulaciones;
    }

    /**
     * Publica una oferta en estado ABIERTA a nombre del usuario de la sesion.
     * Rechaza con 409 la misma oferta enviada de nuevo antes de un minuto.
     */
    @Transactional
    public OfertaDTO crear(OfertaCreateDTO datos, SesionService.Usuario publicador) {
        LocalDateTime ahora = LocalDateTime.now();
        String titulo = datos.titulo().trim();

        if (ofertas.existsByPublicadorCorreoAndTituloIgnoreCaseAndFechaPublicacionAfter(
                publicador.correo(), titulo, ahora.minus(VENTANA_DUPLICADO))) {
            throw new ConflictoNegocioException("Ya publicaste esta oferta hace un momento");
        }

        Oferta oferta = new Oferta(
                titulo,
                datos.actividad().trim(),
                datos.condiciones().trim(),
                datos.valorOfrecido(),
                datos.fechaLimite(),
                datos.urgente(),
                datos.ubicacion().trim(),
                publicador.correo(),
                ahora);
        return OfertaDTO.desde(ofertas.save(oferta));
    }

    /** Publicaciones del usuario de la sesion, la mas reciente primero ("Mis publicaciones"). */
    @Transactional(readOnly = true)
    public List<OfertaDTO> misOfertas(SesionService.Usuario usuario) {
        return ofertas.findByPublicadorCorreoOrderByFechaPublicacionDesc(usuario.correo()).stream()
                .map(OfertaDTO::desde)
                .toList();
    }

    /**
     * Listado paginado de ofertas ABIERTAS y con fecha_limite vigente, ordenadas de la
     * mas reciente a la mas antigua. Usado por el Dashboard (UPBCONECT-37).
     * Solo se listan ofertas cuya fecha_limite es estrictamente posterior al momento de la consulta.
     *
     * @param page numero de pagina (0-based)
     * @param size elementos por pagina (maximo 50 para evitar respuestas gigantes)
     */
    @Transactional(readOnly = true)
    public Page<OfertaResumenDTO> listarActivas(int page, int size) {
        int tamano = Math.min(size, 50);
        PageRequest pageable = PageRequest.of(page, tamano, Sort.by(Sort.Direction.DESC, "fechaPublicacion"));
        return ofertas
                .findByEstadoAndFechaLimiteAfter(EstadoOferta.ABIERTA, LocalDateTime.now(), pageable)
                .map(OfertaResumenDTO::desde);
    }

    /** Trabajos asignados al usuario de la sesion que aun no terminan ("Tareas pendientes"). (UPBCONECT-43) */
    @Transactional(readOnly = true)
    public List<OfertaDTO> tareasPendientes(SesionService.Usuario usuario) {
        return ofertas.findByEstudianteAsignadoCorreoAndEstadoInOrderByFechaLimiteAsc(
                        usuario.correo(), List.of(EstadoOferta.ASIGNADA, EstadoOferta.EN_PROCESO)).stream()
                .map(OfertaDTO::desde)
                .toList();
    }

    /** Trabajos asignados y finalizados del estudiante de la sesion (UPBCONECT-44). */
    @Transactional(readOnly = true)
    public List<OfertaDTO> misTrabajosAsignados(SesionService.Usuario usuario) {
        return ofertas.findByEstudianteAsignadoCorreoAndEstadoInOrderByFechaAsignacionDesc(
                        usuario.correo(), List.of(EstadoOferta.ASIGNADA, EstadoOferta.EN_PROCESO, EstadoOferta.FINALIZADA)).stream()
                .map(OfertaDTO::desde)
                .toList();
    }

    /**
     * Actualiza el estado de una oferta (UPBCONECT-44).
     * Máquina de estados: ABIERTA -> ASIGNADA -> FINALIZADA.
     * Solo el empleador que publicó la oferta puede cerrarla.
     */
    @Transactional
    public OfertaDTO actualizarEstado(Long id, ActualizarEstadoOfertaDTO datos, SesionService.Usuario usuario) {
        Oferta oferta = ofertas.findConBloqueoById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Oferta no encontrada con ID: " + id));

        if (!oferta.getPublicadorCorreo().equalsIgnoreCase(usuario.correo())) {
            throw new AccesoDenegadoException("Solo quien publicó la oferta puede actualizar su estado");
        }

        if (datos.estado() == EstadoOferta.FINALIZADA) {
            if (oferta.getEstado() == EstadoOferta.FINALIZADA) {
                throw new ConflictoNegocioException("La oferta ya se encuentra finalizada");
            }
            if (oferta.getEstado() != EstadoOferta.ASIGNADA && oferta.getEstado() != EstadoOferta.EN_PROCESO) {
                throw new ConflictoNegocioException("Solo se puede finalizar un trabajo en estado ASIGNADA o EN_PROCESO");
            }
            if (oferta.getEstudianteAsignado() == null) {
                throw new ConflictoNegocioException("No se puede finalizar un trabajo que no tenga un estudiante asignado");
            }
            oferta.finalizar(LocalDateTime.now());
        } else if (datos.estado() == EstadoOferta.EN_PROCESO) {
            if (oferta.getEstado() == EstadoOferta.FINALIZADA) {
                throw new ConflictoNegocioException("No se puede pasar a en proceso un trabajo ya finalizado");
            }
            if (oferta.getEstado() == EstadoOferta.EN_PROCESO) {
                throw new ConflictoNegocioException("La oferta ya se encuentra en proceso");
            }
            if (oferta.getEstado() != EstadoOferta.ASIGNADA) {
                throw new ConflictoNegocioException("Solo se puede pasar a EN_PROCESO un trabajo en estado ASIGNADA");
            }
            oferta.ponerEnProceso();
        } else {
            throw new ConflictoNegocioException("Transición de estado no permitida");
        }

        return OfertaDTO.desde(oferta);
    }

    /** Detalle de una oferta para el usuario de la sesion; 404 si no existe. (UPBCONECT-41) */
    @Transactional(readOnly = true)
    public OfertaDetalleDTO obtenerDetalle(Long id, SesionService.Usuario usuario) {
        Oferta oferta = ofertas.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Oferta no encontrada con ID: " + id));
        Optional<UsuarioEntity> publicador = usuarios.findByCorreo(oferta.getPublicadorCorreo());

        return new OfertaDetalleDTO(
                oferta.getId(),
                oferta.getTitulo(),
                oferta.getActividad(),
                oferta.getCondiciones(),
                oferta.getValorOfrecido(),
                oferta.getFechaLimite(),
                oferta.isUrgente(),
                oferta.getUbicacion(),
                oferta.getEstado(),
                oferta.getEstado() == EstadoOferta.ABIERTA && oferta.getFechaLimite().isAfter(LocalDateTime.now()),
                oferta.getFechaPublicacion(),
                publicador.map(UsuarioEntity::getNombre).orElse(oferta.getPublicadorCorreo()),
                oferta.getPublicadorCorreo(),
                publicador.map(UsuarioEntity::getCarrera).orElse(null),
                postulaciones.existsByOfertaIdAndEstudianteCorreo(id, usuario.correo()));
    }
}
