package co.edu.upb.upbconecta.dto;

import co.edu.upb.upbconecta.domain.EstadoPostulacion;
import co.edu.upb.upbconecta.domain.Postulacion;

import java.time.LocalDateTime;

/** Postulacion junto con los datos del estudiante que se postulo. */
public record PostulanteDTO(
        Long postulacionId,
        String nombre,
        String correo,
        String carrera,
        EstadoPostulacion estado,
        LocalDateTime fechaPostulacion
) {
    public static PostulanteDTO desde(Postulacion p) {
        return new PostulanteDTO(p.getId(), p.getEstudiante().getNombre(), p.getEstudiante().getCorreo(),
                p.getEstudiante().getCarrera(), p.getEstado(), p.getFechaPostulacion());
    }
}
