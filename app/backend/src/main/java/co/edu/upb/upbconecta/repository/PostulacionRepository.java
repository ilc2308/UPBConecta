package co.edu.upb.upbconecta.repository;

import co.edu.upb.upbconecta.domain.Postulacion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** Acceso a la tabla "postulacion". */
public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {

    boolean existsByOfertaIdAndEstudianteId(Long ofertaId, UUID estudianteId);

    boolean existsByOfertaIdAndEstudianteCorreo(Long ofertaId, String correo);

    /** Postulaciones de una oferta con su estudiante, la mas antigua primero. */
    @EntityGraph(attributePaths = "estudiante")
    List<Postulacion> findByOfertaIdOrderByFechaPostulacionAsc(Long ofertaId);

    /** Cantidad de postulaciones por oferta: cada fila es [ofertaId, total]. (UPBCONECT-42) */
    @Query("select p.oferta.id, count(p) from Postulacion p where p.oferta.id in :ofertaIds group by p.oferta.id")
    List<Object[]> contarPorOferta(@Param("ofertaIds") Collection<Long> ofertaIds);
}