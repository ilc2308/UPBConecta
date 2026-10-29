package co.edu.upb.upbconecta.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Datos que envia el formulario "Publicar trabajo" (POST /api/v1/ofertas).
 * Si algo no cumple, GlobalExceptionHandler responde 400 con la lista de campos invalidos.
 * El publicador NO viene aqui: se toma de la sesion.
 */
public record OfertaCreateDTO(
        @NotBlank(message = "es obligatorio")
        @Size(max = 100, message = "maximo 100 caracteres")
        String titulo,

        @NotBlank(message = "es obligatoria")
        @Size(max = 1000, message = "maximo 1000 caracteres")
        String actividad,

        @NotBlank(message = "son obligatorias")
        @Size(max = 1000, message = "maximo 1000 caracteres")
        String condiciones,

        @NotNull(message = "es obligatorio")
        @Positive(message = "debe ser mayor que 0")
        Integer valorOfrecido,

        @NotNull(message = "es obligatoria")
        @Future(message = "debe ser una fecha futura")
        LocalDateTime fechaLimite,

        boolean urgente,

        @NotBlank(message = "es obligatoria")
        @Size(max = 150, message = "maximo 150 caracteres")
        String ubicacion
) {
}
