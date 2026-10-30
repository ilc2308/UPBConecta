package co.edu.upb.upbconecta.service;

import co.edu.upb.upbconecta.domain.Oferta;
import co.edu.upb.upbconecta.dto.OfertaCreateDTO;
import co.edu.upb.upbconecta.dto.OfertaDTO;
import co.edu.upb.upbconecta.exception.ConflictoNegocioException;
import co.edu.upb.upbconecta.repository.OfertaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/** Reglas de negocio de las ofertas (UPBCONECT-39 y UPBCONECT-40). */
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
}
