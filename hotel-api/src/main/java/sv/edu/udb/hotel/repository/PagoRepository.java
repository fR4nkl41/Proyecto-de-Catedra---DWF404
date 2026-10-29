package sv.edu.udb.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.edu.udb.hotel.model.Pago;
import sv.edu.udb.hotel.model.enums.EstadoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findByReservaId(Long reservaId);

    @Query("""
            select p from Pago p
            join fetch p.reserva r join fetch r.huesped
            order by p.fechaPago desc
            """)
    List<Pago> listar();

    @Query("""
            select coalesce(sum(p.monto), 0) from Pago p
            where p.estado in :estados
              and p.fechaPago >= :desde and p.fechaPago < :hasta
            """)
    BigDecimal sumarIngresos(@Param("desde") LocalDateTime desde,
                             @Param("hasta") LocalDateTime hasta,
                             @Param("estados") Collection<EstadoPago> estados);
}
