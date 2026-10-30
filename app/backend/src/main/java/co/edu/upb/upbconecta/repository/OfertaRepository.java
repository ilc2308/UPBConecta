package co.edu.upb.upbconecta.repository;

import co.edu.upb.upbconecta.domain.EstadoOferta;
import co.edu.upb.upbconecta.domain.Oferta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/** Acceso a la tabla "oferta". Spring Data genera las consultas a partir de los nombres de los metodos. */
public interface OfertaRepository extends JpaRepository<Oferta, Long> {

    /** Hay una oferta con este titulo, de este publicador, creada despues de "desde". (UPBCONECT-40) */
    boolean existsByPublicadorCorreoAndTituloIgnoreCaseAndFechaPublicacionAfter(
            String publicadorCorreo, String titulo, LocalDateTime desde);

    /** Publicaciones de un usuario, la mas reciente primero. (UPBCONECT-40) */
    List<Oferta> findByPublicadorCorreoOrderByFechaPublicacionDesc(String publicadorCorreo);

    /**
     * Ofertas en un estado dado y cuya fecha_limite es posterior a "ahora".
     * Ordenadas por fecha_publicacion descendente (el Pageable trae el Sort).
     * Usada por el Dashboard (UPBCONECT-37).
     */
    Page<Oferta> findByEstadoAndFechaLimiteAfter(EstadoOferta estado, LocalDateTime ahora, Pageable pageable);
}
