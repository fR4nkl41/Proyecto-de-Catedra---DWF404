package sv.edu.udb.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.edu.udb.hotel.model.Cancelacion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface CancelacionRepository extends JpaRepository<Cancelacion, Long> {

    @Query("""
            select c from Cancelacion c
            join fetch c.reserva r join fetch r.huesped
            order by c.fechaCancelacion desc
            """)
    List<Cancelacion> listar();

    long countByFechaCancelacionBetween(LocalDateTime desde, LocalDateTime hasta);

    @Query("""
            select coalesce(sum(c.montoPenalizacion), 0) from Cancelacion c
            where c.fechaCancelacion >= :desde and c.fechaCancelacion < :hasta
            """)
    BigDecimal sumarPenalizaciones(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
