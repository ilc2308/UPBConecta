package co.edu.upb.upbconecta.dto;

import co.edu.upb.upbconecta.model.RolUsuario;
import jakarta.validation.constraints.NotNull;

public record CambiarRolRequest(@NotNull RolUsuario rol) {}
