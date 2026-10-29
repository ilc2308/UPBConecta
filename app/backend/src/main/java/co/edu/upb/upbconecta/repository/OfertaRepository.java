package co.edu.upb.upbconecta.repository;

import co.edu.upb.upbconecta.domain.Oferta;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a la tabla "oferta". Spring Data genera las consultas a partir de los nombres de los metodos. */
public interface OfertaRepository extends JpaRepository<Oferta, Long> {
}
