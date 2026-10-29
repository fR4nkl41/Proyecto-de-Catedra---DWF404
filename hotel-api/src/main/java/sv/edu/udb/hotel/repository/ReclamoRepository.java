package sv.edu.udb.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.edu.udb.hotel.model.Reclamo;
import sv.edu.udb.hotel.model.enums.EstadoReclamo;

import java.util.List;

public interface ReclamoRepository extends JpaRepository<Reclamo, Long> {

    @Query("""
            select rc from Reclamo rc join fetch rc.huesped left join fetch rc.reserva
            where (:estado is null or rc.estado = :estado)
            order by rc.fechaCreacion desc
            """)
    List<Reclamo> listar(@Param("estado") EstadoReclamo estado);

    @Query("""
            select rc from Reclamo rc join fetch rc.huesped h left join fetch rc.reserva
            where h.usuario.id = :usuarioId
            order by rc.fechaCreacion desc
            """)
    List<Reclamo> listarPorUsuarioHuesped(@Param("usuarioId") Long usuarioId);

    long countByEstado(EstadoReclamo estado);

    boolean existsByReservaId(Long reservaId);

    boolean existsByHuespedId(Long huespedId);
}
