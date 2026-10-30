package sv.edu.udb.hotel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.hotel.dto.response.DashboardResponse;
import sv.edu.udb.hotel.dto.response.ReservaResponse;
import sv.edu.udb.hotel.model.enums.EstadoPago;
import sv.edu.udb.hotel.model.enums.EstadoReclamo;
import sv.edu.udb.hotel.model.enums.EstadoReserva;
import sv.edu.udb.hotel.repository.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Estadísticas básicas: ocupación, ingresos, reservas activas y cancelaciones. */
@Service
@RequiredArgsConstructor
public class EstadisticaService {

    private static final Set<EstadoReserva> ESTADOS_OCUPACION =
            Set.of(EstadoReserva.CONFIRMADA, EstadoReserva.EN_CURSO, EstadoReserva.FINALIZADA);

    private final HotelRepository hotelRepository;
    private final SucursalRepository sucursalRepository;
    private final HabitacionRepository habitacionRepository;
    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;
    private final CancelacionRepository cancelacionRepository;
    private final ReclamoRepository reclamoRepository;

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioMes = hoy.withDayOfMonth(1).atStartOfDay();
        LocalDateTime inicioMesSiguiente = inicioMes.plusMonths(1);

        long totalHabitaciones = habitacionRepository.count();

        // Ingresos del mes: pagos vigentes + penalizaciones retenidas por cancelaciones
        BigDecimal ingresos = pagoRepository.sumarIngresos(inicioMes, inicioMesSiguiente, Set.of(EstadoPago.COMPLETADO))
                .add(cancelacionRepository.sumarPenalizaciones(inicioMes, inicioMesSiguiente));

        // Ocupación de lunes a domingo de la semana actual
        List<DashboardResponse.OcupacionDia> ocupacion = new ArrayList<>();
        LocalDate lunes = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Locale es = Locale.forLanguageTag("es");
        for (int i = 0; i < 7; i++) {
            LocalDate dia = lunes.plusDays(i);
            long ocupadas = reservaRepository.contarHabitacionesOcupadas(dia, ESTADOS_OCUPACION);
            int porcentaje = totalHabitaciones == 0 ? 0 : (int) Math.round(ocupadas * 100.0 / totalHabitaciones);
            String nombre = dia.getDayOfWeek().getDisplayName(TextStyle.SHORT, es).replace(".", "");
            ocupacion.add(new DashboardResponse.OcupacionDia(
                    Character.toUpperCase(nombre.charAt(0)) + nombre.substring(1), dia.toString(), ocupadas, porcentaje));
        }

        List<ReservaResponse> llegadas = reservaRepository.proximasLlegadas(hoy, hoy.plusDays(3),
                        Set.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA))
                .stream().limit(6).map(ReservaResponse::from).toList();

        return new DashboardResponse(
                hotelRepository.count(),
                sucursalRepository.count(),
                totalHabitaciones,
                reservaRepository.countByEstadoIn(HabitacionService.ESTADOS_ACTIVOS),
                ingresos,
                cancelacionRepository.countByFechaCancelacionBetween(inicioMes, inicioMesSiguiente),
                reclamoRepository.countByEstado(EstadoReclamo.ABIERTO),
                ocupacion,
                llegadas);
    }
}
