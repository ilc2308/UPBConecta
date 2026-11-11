package co.edu.upb.upbconecta.dto;

import co.edu.upb.upbconecta.domain.EstadoOferta;
import jakarta.validation.constraints.NotNull;

/** Solicitud para actualizar el estado de una oferta (UPBCONECT-44). */
public record ActualizarEstadoOfertaDTO(
        @NotNull(message = "El estado es obligatorio")
        EstadoOferta estado
) {
}
