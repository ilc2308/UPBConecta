package co.edu.upb.upbconecta.dto;

import co.edu.upb.upbconecta.domain.EstadoOferta;

import java.time.LocalDateTime;

/**
 * Detalle de una oferta (UPBCONECT-41).
 * "disponible" indica si aun admite postulaciones: esta ABIERTA y su fecha limite no ha pasado.
 * "yaPostulado" se calcula para el usuario autenticado.
 * Si el publicador no esta registrado (por ejemplo, el usuario demo), su nombre es
 * el correo y su carrera es null.
 */
public record OfertaDetalleDTO(
        Long id,
        String titulo,
        String actividad,
        String condiciones,
        Integer valorOfrecido,
        LocalDateTime fechaLimite,
        boolean urgente,
        String ubicacion,
        EstadoOferta estado,
        boolean disponible,
        LocalDateTime fechaPublicacion,
        String publicadorNombre,
        String publicadorCorreo,
        String publicadorCarrera,
        boolean yaPostulado
) {
}
