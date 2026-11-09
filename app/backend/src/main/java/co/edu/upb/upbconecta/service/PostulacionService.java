package co.edu.upb.upbconecta.service;

import co.edu.upb.upbconecta.domain.EstadoOferta;
import co.edu.upb.upbconecta.domain.Oferta;
import co.edu.upb.upbconecta.domain.Postulacion;
import co.edu.upb.upbconecta.dto.PostulanteDTO;
import co.edu.upb.upbconecta.exception.AccesoDenegadoException;
import co.edu.upb.upbconecta.exception.ConflictoNegocioException;
import co.edu.upb.upbconecta.exception.ResourceNotFoundException;
import co.edu.upb.upbconecta.model.RolUsuario;
import co.edu.upb.upbconecta.model.UsuarioEntity;
import co.edu.upb.upbconecta.repository.OfertaRepository;
import co.edu.upb.upbconecta.repository.PostulacionRepository;
import co.edu.upb.upbconecta.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Postulaciones a ofertas (UPBCONECT-41).
 * Postular bloquea la fila de la oferta para que no se cruce con un cambio de su estado.
 */
@Service
public class PostulacionService {

    private final OfertaRepository ofertas;
    private final PostulacionRepository postulaciones;
    private final UsuarioRepository usuarios;

    public PostulacionService(OfertaRepository ofertas, PostulacionRepository postulaciones,
                              UsuarioRepository usuarios) {
        this.ofertas = ofertas;
        this.postulaciones = postulaciones;
        this.usuarios = usuarios;
    }

    /** Crea una postulacion PENDIENTE. El rol se lee de la base de datos, no del token. */
    @Transactional
    public PostulanteDTO postular(Long ofertaId, SesionService.Usuario sesion) {
        Oferta oferta = ofertaBloqueada(ofertaId);
        UsuarioEntity estudiante = usuarios.findByCorreo(sesion.correo())
                .filter(u -> u.isActivo() && u.getRol() == RolUsuario.ESTUDIANTE)
                .orElseThrow(() -> new AccesoDenegadoException("Solo los estudiantes pueden postularse"));

        LocalDateTime ahora = LocalDateTime.now();
        if (esDueno(oferta, sesion)) {
            throw new ConflictoNegocioException("No puedes postularte a tu propia oferta");
        }
        if (oferta.getEstado() != EstadoOferta.ABIERTA || !oferta.getFechaLimite().isAfter(ahora)) {
            throw new ConflictoNegocioException("Esta oferta ya no está disponible");
        }
        if (postulaciones.existsByOfertaIdAndEstudianteId(ofertaId, estudiante.getId())) {
            throw new ConflictoNegocioException("Ya te postulaste a esta oferta");
        }
        return PostulanteDTO.desde(postulaciones.save(new Postulacion(oferta, estudiante, ahora)));
    }

    private Oferta ofertaBloqueada(Long ofertaId) {
        return ofertas.findConBloqueoById(ofertaId).orElseThrow(() -> ofertaNoEncontrada(ofertaId));
    }

    private static ResourceNotFoundException ofertaNoEncontrada(Long ofertaId) {
        return new ResourceNotFoundException("Oferta no encontrada con ID: " + ofertaId);
    }

    private static boolean esDueno(Oferta oferta, SesionService.Usuario sesion) {
        return oferta.getPublicadorCorreo().equalsIgnoreCase(sesion.correo());
    }

}
