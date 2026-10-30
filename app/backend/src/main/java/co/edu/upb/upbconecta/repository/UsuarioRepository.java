package co.edu.upb.upbconecta.repository;

import co.edu.upb.upbconecta.model.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, UUID> {
    Optional<UsuarioEntity> findByMicrosoftOid(String microsoftOid);
    boolean existsByMicrosoftOidOrCorreo(String microsoftOid, String correo);
}
