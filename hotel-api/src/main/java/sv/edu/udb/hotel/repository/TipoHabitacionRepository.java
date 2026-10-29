package sv.edu.udb.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.hotel.model.TipoHabitacion;

import java.util.List;

public interface TipoHabitacionRepository extends JpaRepository<TipoHabitacion, Long> {

    List<TipoHabitacion> findAllByOrderByPrecioNocheAsc();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
