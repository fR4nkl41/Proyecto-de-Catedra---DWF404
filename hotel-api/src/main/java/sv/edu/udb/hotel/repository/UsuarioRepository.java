package sv.edu.udb.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.hotel.model.Usuario;
import sv.edu.udb.hotel.model.enums.Rol;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    List<Usuario> findByRolOrderByNombreAsc(Rol rol);

    List<Usuario> findAllByOrderByNombreAsc();
}
