package co.edu.upb.upbconecta.repository;

import co.edu.upb.upbconecta.domain.Postulacion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Acceso a la tabla "postulacion". */
public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {

    boolean existsByOfertaIdAndEstudianteId(Long ofertaId, UUID estudianteId);

    boolean existsByOfertaIdAndEstudianteCorreo(Long ofertaId, String correo);

    /** Postulaciones de una oferta con su estudiante, la mas antigua primero. */
    @EntityGraph(attributePaths = "estudiante")
    List<Postulacion> findByOfertaIdOrderByFechaPostulacionAsc(Long ofertaId);
}
