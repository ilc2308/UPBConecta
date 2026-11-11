package co.edu.upb.upbconecta.repository;

import co.edu.upb.upbconecta.domain.EstadoOferta;
import co.edu.upb.upbconecta.domain.Oferta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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

    /** Oferta con su fila bloqueada hasta el fin de la transaccion. (UPBCONECT-41 y 43) */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Oferta o where o.id = :id")
    Optional<Oferta> findConBloqueoById(@Param("id") Long id);

    /** Trabajos asignados a un estudiante, el mas proximo a vencer primero. (UPBCONECT-43) */
    List<Oferta> findByEstudianteAsignadoCorreoAndEstadoInOrderByFechaLimiteAsc(
            String correo, Collection<EstadoOferta> estados);

    /** Trabajos asignados y finalizados de un estudiante (UPBCONECT-44). */
    List<Oferta> findByEstudianteAsignadoCorreoAndEstadoInOrderByFechaAsignacionDesc(
            String correo, Collection<EstadoOferta> estados);

    /**
     * Busqueda por palabra clave sobre titulo, actividad y condiciones, sin distinguir
     * mayusculas ni tildes. Mantiene la regla de ofertas ABIERTAS y vigentes. (UPBCONECT-38)
     * El patron ya llega con los comodines % y con los caracteres especiales escapados con '\'.
     * Se usa cast(... as string) para que Hibernate 7 infiera el tipo String en el operando de LIKE.
     */
    @Query("""
            select o from Oferta o
            where o.estado = :estado
              and o.fechaLimite > :ahora
              and (
                   cast(function('translate', lower(o.titulo), 'áàäâéèëêíìïîóòöôúùüûñ', 'aaaaeeeeiiiioooouuuun') as string) like :patron escape '\\'
                or cast(function('translate', lower(o.actividad), 'áàäâéèëêíìïîóòöôúùüûñ', 'aaaaeeeeiiiioooouuuun') as string) like :patron escape '\\'
                or cast(function('translate', lower(o.condiciones), 'áàäâéèëêíìïîóòöôúùüûñ', 'aaaaeeeeiiiioooouuuun') as string) like :patron escape '\\'
              )
            """)
    Page<Oferta> buscarActivas(@Param("estado") EstadoOferta estado,
                               @Param("ahora") LocalDateTime ahora,
                               @Param("patron") String patron,
                               Pageable pageable);
}
