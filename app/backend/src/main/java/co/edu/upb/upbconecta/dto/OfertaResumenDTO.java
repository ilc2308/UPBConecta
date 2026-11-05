package co.edu.upb.upbconecta.dto;

import co.edu.upb.upbconecta.domain.EstadoOferta;
import co.edu.upb.upbconecta.domain.Oferta;

import java.time.LocalDateTime;

/**
 * Resumen de una oferta para el listado del Dashboard (UPBCONECT-37).
 * Solo expone los campos necesarios para la tarjeta: no incluye condiciones ni publicadorCorreo.
 */
public record OfertaResumenDTO(
        Long id,
        String titulo,
        String actividad,
        Integer valorOfrecido,
        LocalDateTime fechaLimite,
        boolean urgente,
        String ubicacion,
        EstadoOferta estado,
        LocalDateTime fechaPublicacion,
        String publicadorCorreo
) {
    public static OfertaResumenDTO desde(Oferta o) {
        return new OfertaResumenDTO(
                o.getId(),
                o.getTitulo(),
                o.getActividad(),
                o.getValorOfrecido(),
                o.getFechaLimite(),
                o.isUrgente(),
                o.getUbicacion(),
                o.getEstado(),
                o.getFechaPublicacion(),
                o.getPublicadorCorreo()
        );
    }
}
