package co.edu.upb.upbconecta.dto;

import co.edu.upb.upbconecta.domain.EstadoOferta;
import co.edu.upb.upbconecta.domain.Oferta;

import java.time.LocalDateTime;

/**
 * Oferta tal como la devuelve la API.
 * totalPostulados solo se calcula en "Mis publicaciones" (UPBCONECT-42); en los demas casos queda en 0.
 */
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
        String publicadorCorreo,
        String estudianteAsignadoNombre,
        String estudianteAsignadoCorreo,
        LocalDateTime fechaAsignacion,
        LocalDateTime fechaCierre,
        long totalPostulados
) {
    /** Constructor sin asignacion ni conteo (quedan en null/0), para no cambiar los demas usos del DTO. */
    public OfertaDTO(Long id, String titulo, String actividad, String condiciones,
                     Integer valorOfrecido, LocalDateTime fechaLimite, boolean urgente,
                     String ubicacion, EstadoOferta estado, LocalDateTime fechaPublicacion,
                     String publicadorCorreo) {
        this(id, titulo, actividad, condiciones, valorOfrecido, fechaLimite, urgente,
                ubicacion, estado, fechaPublicacion, publicadorCorreo, null, null, null, null, 0L);
    }

    public static OfertaDTO desde(Oferta o) {
        return conConteo(o, 0L);
    }

    public static OfertaDTO conConteo(Oferta o, long totalPostulados) {
        String estNombre = o.getEstudianteAsignado() != null ? o.getEstudianteAsignado().getNombre() : null;
        String estCorreo = o.getEstudianteAsignado() != null ? o.getEstudianteAsignado().getCorreo() : null;
        return new OfertaDTO(
                o.getId(),
                o.getTitulo(),
                o.getActividad(),
                o.getCondiciones(),
                o.getValorOfrecido(),
                o.getFechaLimite(),
                o.isUrgente(),
                o.getUbicacion(),
                o.getEstado(),
                o.getFechaPublicacion(),
                o.getPublicadorCorreo(),
                estNombre,
                estCorreo,
                o.getFechaAsignacion(),
                o.getFechaCierre(),
                totalPostulados
        );
    }
}
