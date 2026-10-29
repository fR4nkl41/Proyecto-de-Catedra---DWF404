package sv.edu.udb.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.hotel.model.Hotel;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    List<Hotel> findAllByOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
