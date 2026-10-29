package sv.edu.udb.hotel.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.edu.udb.hotel.model.Habitacion;
import sv.edu.udb.hotel.model.enums.EstadoHabitacion;
import sv.edu.udb.hotel.model.enums.EstadoReserva;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {

    @Query("""
            select h from Habitacion h
            join fetch h.sucursal s join fetch s.hotel join fetch h.tipo
            where (:sucursalId is null or s.id = :sucursalId)
              and (:estado is null or h.estado = :estado)
            order by s.nombre, h.numero
            """)
    List<Habitacion> buscar(@Param("sucursalId") Long sucursalId, @Param("estado") EstadoHabitacion estado);

    /**
     * Motor de búsqueda de disponibilidad: habitaciones que no están en mantenimiento,
     * con capacidad suficiente y sin reservas activas que se crucen con las fechas.
     */
    @Query("""
            select h from Habitacion h
            join fetch h.sucursal s join fetch s.hotel join fetch h.tipo t
            where h.estado <> :excluido
              and t.capacidadMaxima >= :huespedes
              and (:tipoId is null or t.id = :tipoId)
              and lower(s.ciudad) like lower(concat('%', :ciudad, '%'))
              and not exists (
                  select r.id from Reserva r
                  where r.habitacion = h
                    and r.estado in :estadosActivos
                    and r.fechaEntrada < :salida
                    and r.fechaSalida > :entrada)
            order by t.precioNoche, s.nombre, h.numero
            """)
    List<Habitacion> buscarDisponibles(@Param("ciudad") String ciudad,
                                       @Param("entrada") LocalDate entrada,
                                       @Param("salida") LocalDate salida,
                                       @Param("huespedes") Integer huespedes,
                                       @Param("tipoId") Long tipoId,
                                       @Param("estadosActivos") Collection<EstadoReserva> estadosActivos,
                                       @Param("excluido") EstadoHabitacion excluido);

    /** Bloquea la fila de la habitación mientras se crea la reserva (evita reservas dobles). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from Habitacion h where h.id = :id")
    Optional<Habitacion> buscarParaReservar(@Param("id") Long id);

    boolean existsBySucursalIdAndNumeroIgnoreCase(Long sucursalId, String numero);

    boolean existsBySucursalIdAndNumeroIgnoreCaseAndIdNot(Long sucursalId, String numero, Long id);

    long countBySucursalId(Long sucursalId);

    long countByTipoId(Long tipoId);
}
