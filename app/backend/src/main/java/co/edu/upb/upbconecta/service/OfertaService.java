package co.edu.upb.upbconecta.service;

import co.edu.upb.upbconecta.domain.EstadoOferta;
import co.edu.upb.upbconecta.domain.Oferta;
import co.edu.upb.upbconecta.dto.OfertaCreateDTO;
import co.edu.upb.upbconecta.dto.OfertaDTO;
import co.edu.upb.upbconecta.dto.OfertaResumenDTO;
import co.edu.upb.upbconecta.exception.ConflictoNegocioException;
import co.edu.upb.upbconecta.repository.OfertaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/** Reglas de negocio de las ofertas (UPBCONECT-39, UPBCONECT-40 y UPBCONECT-37). */
@Service
public class OfertaService {

    /** Ventana en la que una oferta con el mismo titulo se considera un envio duplicado (doble clic). */
    static final Duration VENTANA_DUPLICADO = Duration.ofMinutes(1);

    private final OfertaRepository ofertas;

    public OfertaService(OfertaRepository ofertas) {
        this.ofertas = ofertas;
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
}
