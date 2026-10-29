package co.edu.upb.upbconecta.dto;

import co.edu.upb.upbconecta.domain.EstadoOferta;
import co.edu.upb.upbconecta.domain.Oferta;

import java.time.LocalDateTime;

/** Oferta tal como la devuelve la API. */
public record OfertaDTO(
        Long id,
        String titulo,
        String actividad,
        String condiciones,
        Integer valorOfrecido,
        LocalDateTime fechaLimite,
        boolean urgente,
        String ubicacion,
        EstadoOferta estado,
        LocalDateTime fechaPublicacion,
        String publicadorCorreo
) {
    public static OfertaDTO desde(Oferta o) {
        return new OfertaDTO(o.getId(), o.getTitulo(), o.getActividad(), o.getCondiciones(),
                o.getValorOfrecido(), o.getFechaLimite(), o.isUrgente(), o.getUbicacion(),
                o.getEstado(), o.getFechaPublicacion(), o.getPublicadorCorreo());
    }
}
