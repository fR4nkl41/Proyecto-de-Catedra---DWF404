package sv.edu.udb.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.hotel.model.ReservaServicio;

public interface ReservaServicioRepository extends JpaRepository<ReservaServicio, Long> {

    boolean existsByServicioId(Long servicioId);
}
