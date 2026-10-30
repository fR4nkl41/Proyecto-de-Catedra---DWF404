package sv.edu.udb.hotel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.hotel.dto.request.BusquedaRequest;
import sv.edu.udb.hotel.dto.request.HabitacionRequest;
import sv.edu.udb.hotel.dto.response.DisponibilidadResponse;
import sv.edu.udb.hotel.dto.response.HabitacionResponse;
import sv.edu.udb.hotel.exception.RecursoNoEncontradoException;
import sv.edu.udb.hotel.exception.ReglaNegocioException;
import sv.edu.udb.hotel.model.Habitacion;
import sv.edu.udb.hotel.model.Servicio;
import sv.edu.udb.hotel.model.Sucursal;
import sv.edu.udb.hotel.model.TipoHabitacion;
import sv.edu.udb.hotel.model.enums.EstadoHabitacion;
import sv.edu.udb.hotel.model.enums.EstadoReserva;
import sv.edu.udb.hotel.repository.*;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HabitacionService {

    /** Estados de reserva que ocupan la habitación en sus fechas. */
    public static final Set<EstadoReserva> ESTADOS_ACTIVOS =
            Set.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA, EstadoReserva.EN_CURSO);

    private final HabitacionRepository habitacionRepository;
    private final SucursalRepository sucursalRepository;
    private final TipoHabitacionRepository tipoRepository;
    private final ServicioRepository servicioRepository;
    private final ReservaRepository reservaRepository;

    @Transactional(readOnly = true)
    public List<HabitacionResponse> listar(Long sucursalId, EstadoHabitacion estado) {
        return habitacionRepository.buscar(sucursalId, estado).stream().map(HabitacionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public HabitacionResponse obtener(Long id) {
        return HabitacionResponse.from(buscar(id));
    }

    /** Motor de búsqueda de disponibilidad por ciudad, fechas, huéspedes y tipo. */
    @Transactional(readOnly = true)
    public List<DisponibilidadResponse> buscarDisponibles(BusquedaRequest filtro) {
        String ciudad = filtro.ciudad() == null ? "" : filtro.ciudad().trim();
        long noches = ChronoUnit.DAYS.between(filtro.fechaEntrada(), filtro.fechaSalida());

        return habitacionRepository.buscarDisponibles(ciudad, filtro.fechaEntrada(), filtro.fechaSalida(),
                        filtro.huespedes(), filtro.tipoId(), ESTADOS_ACTIVOS, EstadoHabitacion.MANTENIMIENTO)
                .stream()
                .map(h -> new DisponibilidadResponse(HabitacionResponse.from(h), filtro.fechaEntrada(),
                        filtro.fechaSalida(), noches,
                        h.getTipo().getPrecioNoche().multiply(BigDecimal.valueOf(noches))))
                .toList();
    }

    @Transactional
    public HabitacionResponse crear(HabitacionRequest request) {
        Sucursal sucursal = buscarSucursal(request.sucursalId());
        String numero = request.numero().trim().toUpperCase();
        if (habitacionRepository.existsBySucursalIdAndNumeroIgnoreCase(sucursal.getId(), numero)) {
            throw ReglaNegocioException.conflicto("La sucursal ya tiene una habitación con el número " + numero + ".");
        }
        Habitacion habitacion = new Habitacion();
        copiarDatos(request, habitacion, sucursal, numero);
        return HabitacionResponse.from(habitacionRepository.save(habitacion));
    }

    @Transactional
    public HabitacionResponse actualizar(Long id, HabitacionRequest request) {
        Habitacion habitacion = buscar(id);
        Sucursal sucursal = buscarSucursal(request.sucursalId());
        String numero = request.numero().trim().toUpperCase();
        if (habitacionRepository.existsBySucursalIdAndNumeroIgnoreCaseAndIdNot(sucursal.getId(), numero, id)) {
            throw ReglaNegocioException.conflicto("La sucursal ya tiene otra habitación con el número " + numero + ".");
        }
        copiarDatos(request, habitacion, sucursal, numero);
        return HabitacionResponse.from(habitacion);
    }

    @Transactional
    public HabitacionResponse cambiarEstado(Long id, EstadoHabitacion estado) {
        Habitacion habitacion = buscar(id);
        habitacion.setEstado(estado);
        return HabitacionResponse.from(habitacion);
    }

    @Transactional
    public void eliminar(Long id) {
        Habitacion habitacion = buscar(id);
        if (reservaRepository.existsByHabitacionId(id)) {
            throw ReglaNegocioException.conflicto("No se puede eliminar la habitación porque tiene reservas registradas. "
                    + "Puedes ponerla en MANTENIMIENTO para que no aparezca en las búsquedas.");
        }
        habitacionRepository.delete(habitacion);
    }

    private void copiarDatos(HabitacionRequest request, Habitacion habitacion, Sucursal sucursal, String numero) {
        TipoHabitacion tipo = tipoRepository.findById(request.tipoId())
                .orElseThrow(() -> RecursoNoEncontradoException.de("el tipo de habitación", request.tipoId()));
        habitacion.setSucursal(sucursal);
        habitacion.setTipo(tipo);
        habitacion.setNumero(numero);
        habitacion.setPiso(request.piso());
        habitacion.setEstado(request.estado() == null ? EstadoHabitacion.DISPONIBLE : request.estado());

        Set<Servicio> servicios = new HashSet<>();
        if (request.servicioIds() != null) {
            for (Long servicioId : request.servicioIds()) {
                Servicio servicio = servicioRepository.findById(servicioId)
                        .orElseThrow(() -> RecursoNoEncontradoException.de("el servicio", servicioId));
                if (Boolean.TRUE.equals(servicio.getAdicional())) {
                    throw new ReglaNegocioException("El servicio \"" + servicio.getNombre()
                            + "\" es adicional (se cobra en la reserva) y no puede incluirse en la habitación.");
                }
                servicios.add(servicio);
            }
        }
        habitacion.getServicios().clear();
        habitacion.getServicios().addAll(servicios);
    }

    private Sucursal buscarSucursal(Long id) {
        return sucursalRepository.findById(id).orElseThrow(() -> RecursoNoEncontradoException.de("la sucursal", id));
    }

    private Habitacion buscar(Long id) {
        return habitacionRepository.findById(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("la habitación", id));
    }
}
