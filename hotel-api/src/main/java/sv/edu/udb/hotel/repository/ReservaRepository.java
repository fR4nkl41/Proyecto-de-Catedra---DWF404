package sv.edu.udb.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.edu.udb.hotel.model.Reserva;
import sv.edu.udb.hotel.model.enums.EstadoReserva;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    String CONSULTA_BASE = """
            select r from Reserva r
            join fetch r.huesped hu
            join fetch r.habitacion h join fetch h.tipo join fetch h.sucursal s join fetch s.hotel
            """;

    @Query(CONSULTA_BASE + " where (:estado is null or r.estado = :estado) order by r.fechaReserva desc")
    List<Reserva> listar(@Param("estado") EstadoReserva estado);

    @Query(CONSULTA_BASE + """
             where hu.usuario.id = :usuarioId
               and (:estado is null or r.estado = :estado)
             order by r.fechaReserva desc
            """)
    List<Reserva> listarPorUsuarioHuesped(@Param("usuarioId") Long usuarioId, @Param("estado") EstadoReserva estado);

    @Query(CONSULTA_BASE + " where r.id = :id")
    Optional<Reserva> buscarDetalle(@Param("id") Long id);

    @Query("""
            select case when count(r) > 0 then true else false end from Reserva r
            where r.habitacion.id = :habitacionId
              and r.estado in :estadosActivos
              and r.fechaEntrada < :salida
              and r.fechaSalida > :entrada
              and (:excluirId is null or r.id <> :excluirId)
            """)
    boolean existeCruce(@Param("habitacionId") Long habitacionId,
                        @Param("entrada") LocalDate entrada,
                        @Param("salida") LocalDate salida,
                        @Param("estadosActivos") Collection<EstadoReserva> estadosActivos,
                        @Param("excluirId") Long excluirId);

    long countByEstadoIn(Collection<EstadoReserva> estados);

    /** Habitaciones ocupadas en un día concreto (para el gráfico de ocupación). */
    @Query("""
            select count(distinct r.habitacion.id) from Reserva r
            where r.estado in :estados
              and r.fechaEntrada <= :dia
              and r.fechaSalida > :dia
            """)
    long contarHabitacionesOcupadas(@Param("dia") LocalDate dia, @Param("estados") Collection<EstadoReserva> estados);

    @Query(CONSULTA_BASE + """
             where r.estado in :estados
               and r.fechaEntrada between :desde and :hasta
             order by r.fechaEntrada
            """)
    List<Reserva> proximasLlegadas(@Param("desde") LocalDate desde,
                                   @Param("hasta") LocalDate hasta,
                                   @Param("estados") Collection<EstadoReserva> estados);

    boolean existsByHuespedId(Long huespedId);

    boolean existsByHabitacionId(Long habitacionId);
}
