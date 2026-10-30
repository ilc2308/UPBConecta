package co.edu.upb.upbconecta.dto;

import co.edu.upb.upbconecta.model.RolUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MicrosoftRegisterRequest(
        @NotBlank String idToken,
        @NotNull RolUsuario rol,
        @NotBlank @Size(max = 120) String carrera) {}
