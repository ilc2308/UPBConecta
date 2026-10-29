package co.edu.upb.upbconecta.service;

import co.edu.upb.upbconecta.domain.Oferta;
import co.edu.upb.upbconecta.dto.OfertaCreateDTO;
import co.edu.upb.upbconecta.dto.OfertaDTO;
import co.edu.upb.upbconecta.repository.OfertaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Reglas de negocio de las ofertas (UPBCONECT-39). */
@Service
public class OfertaService {

    private final OfertaRepository ofertas;

    public OfertaService(OfertaRepository ofertas) {
        this.ofertas = ofertas;
    }

    /** Publica una oferta en estado ABIERTA a nombre del usuario de la sesion. */
    @Transactional
    public OfertaDTO crear(OfertaCreateDTO datos, SesionService.Usuario publicador) {
        Oferta oferta = new Oferta(
                datos.titulo().trim(),
                datos.actividad().trim(),
                datos.condiciones().trim(),
                datos.valorOfrecido(),
                datos.fechaLimite(),
                datos.urgente(),
                datos.ubicacion().trim(),
                publicador.correo(),
                LocalDateTime.now());
        return OfertaDTO.desde(ofertas.save(oferta));
    }
}
