package co.edu.upb.upbconecta.repository;

import co.edu.upb.upbconecta.domain.Postulacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** Acceso a la tabla "postulacion". */
public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {

    boolean existsByOfertaIdAndEstudianteId(Long ofertaId, UUID estudianteId);

    boolean existsByOfertaIdAndEstudianteCorreo(Long ofertaId, String correo);

}
