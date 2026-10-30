package co.edu.upb.upbconecta.dto;

import co.edu.upb.upbconecta.model.RolUsuario;
import co.edu.upb.upbconecta.model.UsuarioEntity;

import java.util.UUID;

public record UsuarioResponse(UUID id, String correo, String nombre, RolUsuario rol, String carrera) {
    public static UsuarioResponse desde(UsuarioEntity usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getCorreo(), usuario.getNombre(),
                usuario.getRol(), usuario.getCarrera());
    }
}
