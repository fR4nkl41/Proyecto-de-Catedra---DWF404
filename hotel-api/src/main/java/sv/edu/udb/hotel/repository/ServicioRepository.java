package sv.edu.udb.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.hotel.model.Servicio;

import java.util.List;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    List<Servicio> findAllByOrderByNombreAsc();

    List<Servicio> findByAdicionalOrderByNombreAsc(Boolean adicional);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
