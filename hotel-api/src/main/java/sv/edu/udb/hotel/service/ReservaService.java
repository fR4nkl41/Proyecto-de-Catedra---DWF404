package sv.edu.udb.hotel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.hotel.dto.request.CancelacionRequest;
import sv.edu.udb.hotel.dto.request.ReservaRequest;
import sv.edu.udb.hotel.dto.request.ServicioReservaRequest;
import sv.edu.udb.hotel.dto.response.CancelacionResponse;
import sv.edu.udb.hotel.dto.response.ReservaResponse;
import sv.edu.udb.hotel.exception.RecursoNoEncontradoException;
import sv.edu.udb.hotel.exception.ReglaNegocioException;
import sv.edu.udb.hotel.model.*;
import sv.edu.udb.hotel.model.enums.EstadoHabitacion;
import sv.edu.udb.hotel.model.enums.EstadoPago;
import sv.edu.udb.hotel.model.enums.EstadoReserva;
import sv.edu.udb.hotel.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Lógica principal de reservas: creación, modificación, cancelación con
 * penalización, check-in y check-out.
 */
@Service
@RequiredArgsConstructor
public class ReservaService {

    /** Hora de check-in usada para calcular la anticipación de una cancelación. */
    public static final int HORA_CHECK_IN = 14;
    /** Horas de anticipación para cancelar sin cargo. */
    public static final int HORAS_CANCELACION_GRATIS = 48;
    /** Porcentaje del monto pagado que se retiene si se cancela con menos anticipación. */
    public static final BigDecimal PORCENTAJE_PENALIZACION = new BigDecimal("50.00");

    public static final String POLITICA_CANCELACION = "Cancelación gratuita hasta " + HORAS_CANCELACION_GRATIS
            + " horas antes de la llegada (check-in 2:00 p.m.). Con menos anticipación se retiene el "
            + PORCENTAJE_PENALIZACION.stripTrailingZeros().toPlainString() + "% del monto pagado.";

    private final ReservaRepository reservaRepository;
    private final HabitacionRepository habitacionRepository;
    private final ServicioRepository servicioRepository;
    private final CancelacionRepository cancelacionRepository;
    private final ReclamoRepository reclamoRepository;
    private final HuespedService huespedService;
    private final AuthService authService;

    // ------------------------------------------------------------------ consultas

    @Transactional(readOnly = true)
    public List<ReservaResponse> listar(EstadoReserva estado) {
        Usuario usuario = authService.getUsuarioActual();
        List<Reserva> reservas = AuthService.esPersonal(usuario)
                ? reservaRepository.listar(estado)
                : reservaRepository.listarPorUsuarioHuesped(usuario.getId(), estado);
        return reservas.stream().map(ReservaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ReservaResponse obtener(Long id) {
        Reserva reserva = buscarDetalle(id);
        validarAcceso(reserva, authService.getUsuarioActual());
        return ReservaResponse.from(reserva);
    }

    @Transactional(readOnly = true)
    public List<CancelacionResponse> listarCancelaciones() {
        return cancelacionRepository.listar().stream()
                .map(c -> CancelacionResponse.from(c, POLITICA_CANCELACION))
                .toList();
    }

    // ------------------------------------------------------------------ crear / modificar

    @Transactional
    public ReservaResponse crear(ReservaRequest request) {
        Usuario usuario = authService.getUsuarioActual();
        Habitacion habitacion = bloquearHabitacion(request.habitacionId());
        validarHabitacion(habitacion, request, null);

        Huesped huesped = huespedService.resolverHuespedParaReserva(usuario, request.huesped());

        Reserva reserva = new Reserva();
        reserva.setCodigo("TMP-" + UUID.randomUUID().toString().substring(0, 12));
        reserva.setHuesped(huesped);
        reserva.setRegistradoPor(usuario);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        aplicarDatos(reserva, habitacion, request);

        reservaRepository.save(reserva);
        // El código definitivo usa el ID generado: RES-2026-0007
        reserva.setCodigo(String.format("RES-%d-%04d", LocalDate.now().getYear(), reserva.getId()));
        return ReservaResponse.from(reserva);
    }

    /** Solo se pueden modificar reservas PENDIENTES (aún sin pago). */
    @Transactional
    public ReservaResponse actualizar(Long id, ReservaRequest request) {
        Usuario usuario = authService.getUsuarioActual();
        Reserva reserva = buscarDetalle(id);
        validarAcceso(reserva, usuario);
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ReglaNegocioException("Solo se pueden modificar reservas en estado PENDIENTE. "
                    + "Esta reserva está " + reserva.getEstado() + ".");
        }
        Habitacion habitacion = bloquearHabitacion(request.habitacionId());
        validarHabitacion(habitacion, request, reserva.getId());

        reserva.setHuesped(huespedService.resolverHuespedParaReserva(usuario, request.huesped()));
        aplicarDatos(reserva, habitacion, request);
        return ReservaResponse.from(reserva);
    }

    /** Elimina definitivamente una reserva (solo ADMIN, solo pendientes sin pago o canceladas). */
    @Transactional
    public void eliminar(Long id) {
        Reserva reserva = buscarDetalle(id);
        boolean pendienteSinPago = reserva.getEstado() == EstadoReserva.PENDIENTE && reserva.getPago() == null;
        if (!pendienteSinPago && reserva.getEstado() != EstadoReserva.CANCELADA) {
            throw new ReglaNegocioException("Solo se pueden eliminar reservas PENDIENTES sin pago o CANCELADAS. "
                    + "Para las demás use la opción de cancelar.");
        }
        if (reclamoRepository.existsByReservaId(id)) {
            throw ReglaNegocioException.conflicto("No se puede eliminar la reserva porque tiene reclamos asociados.");
        }
        reservaRepository.delete(reserva);
    }

    // ------------------------------------------------------------------ cancelación

    /** Calcula la penalización sin cancelar, para mostrarla antes de confirmar. */
    @Transactional(readOnly = true)
    public CancelacionResponse simularCancelacion(Long id) {
        Reserva reserva = buscarDetalle(id);
        validarAcceso(reserva, authService.getUsuarioActual());
        validarCancelable(reserva);
        Cancelacion calculo = calcularCancelacion(reserva, null, "Simulación");
        return new CancelacionResponse(null, reserva.getId(), reserva.getCodigo(),
                reserva.getHuesped().getNombreCompleto(), null, null, calculo.getHorasAnticipacion(),
                calculo.getPorcentajePenalizacion(), calculo.getMontoPenalizacion(), calculo.getMontoReembolso(),
                POLITICA_CANCELACION);
    }

    @Transactional
    public CancelacionResponse cancelar(Long id, CancelacionRequest request) {
        Usuario usuario = authService.getUsuarioActual();
        Reserva reserva = buscarDetalle(id);
        validarAcceso(reserva, usuario);
        validarCancelable(reserva);

        Cancelacion cancelacion = calcularCancelacion(reserva, usuario, request.motivo().trim());
        cancelacionRepository.save(cancelacion);

        Pago pago = reserva.getPago();
        if (pago != null && pago.getEstado() == EstadoPago.COMPLETADO) {
            pago.setEstado(cancelacion.getMontoPenalizacion().signum() == 0
                    ? EstadoPago.REEMBOLSADO
                    : EstadoPago.REEMBOLSO_PARCIAL);
        }
        reserva.setEstado(EstadoReserva.CANCELADA);
        reserva.setCancelacion(cancelacion);
        return CancelacionResponse.from(cancelacion, POLITICA_CANCELACION);
    }

    // ------------------------------------------------------------------ check-in / check-out

    @Transactional
    public ReservaResponse registrarCheckIn(Long id) {
        Reserva reserva = buscarDetalle(id);
        LocalDate hoy = LocalDate.now();
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new ReglaNegocioException("Solo se puede hacer check-in de reservas CONFIRMADAS (pagadas). "
                    + "Estado actual: " + reserva.getEstado() + ".");
        }
        if (hoy.isBefore(reserva.getFechaEntrada())) {
            throw new ReglaNegocioException("El check-in está disponible a partir del " + reserva.getFechaEntrada() + ".");
        }
        if (!hoy.isBefore(reserva.getFechaSalida())) {
            throw new ReglaNegocioException("La fecha de salida de esta reserva ya pasó; no se puede registrar el check-in.");
        }
        reserva.getHabitacion().setEstado(EstadoHabitacion.OCUPADA);
        reserva.setEstado(EstadoReserva.EN_CURSO);
        return ReservaResponse.from(reserva);
    }

    @Transactional
    public ReservaResponse registrarCheckOut(Long id) {
        Reserva reserva = buscarDetalle(id);
        if (reserva.getEstado() != EstadoReserva.EN_CURSO) {
            throw new ReglaNegocioException("Solo se puede hacer check-out de reservas EN_CURSO. "
                    + "Estado actual: " + reserva.getEstado() + ".");
        }
        reserva.getHabitacion().setEstado(EstadoHabitacion.DISPONIBLE);
        reserva.setEstado(EstadoReserva.FINALIZADA);
        return ReservaResponse.from(reserva);
    }

    // ------------------------------------------------------------------ utilidades

    /** Un huésped solo puede ver y operar sus propias reservas. */
    public void validarAcceso(Reserva reserva, Usuario usuario) {
        if (AuthService.esPersonal(usuario)) {
            return;
        }
        Usuario dueno = reserva.getHuesped().getUsuario();
        if (dueno == null || !dueno.getId().equals(usuario.getId())) {
            throw ReglaNegocioException.prohibido("No tienes acceso a esta reserva.");
        }
    }

    public Reserva buscarDetalle(Long id) {
        return reservaRepository.buscarDetalle(id).orElseThrow(() -> RecursoNoEncontradoException.de("la reserva", id));
    }

    private Habitacion bloquearHabitacion(Long habitacionId) {
        return habitacionRepository.buscarParaReservar(habitacionId)
                .orElseThrow(() -> RecursoNoEncontradoException.de("la habitación", habitacionId));
    }

    private void validarHabitacion(Habitacion habitacion, ReservaRequest request, Long reservaExcluida) {
        if (habitacion.getEstado() == EstadoHabitacion.MANTENIMIENTO) {
            throw new ReglaNegocioException("La habitación " + habitacion.getNumero()
                    + " está en mantenimiento y no se puede reservar.");
        }
        Integer capacidad = habitacion.getTipo().getCapacidadMaxima();
        if (request.numeroHuespedes() > capacidad) {
            throw new ReglaNegocioException("La habitación " + habitacion.getNumero() + " (" + habitacion.getTipo().getNombre()
                    + ") admite máximo " + capacidad + " persona(s).");
        }
        boolean ocupada = reservaRepository.existeCruce(habitacion.getId(), request.fechaEntrada(), request.fechaSalida(),
                HabitacionService.ESTADOS_ACTIVOS, reservaExcluida);
        if (ocupada) {
            throw ReglaNegocioException.conflicto("La habitación ya está reservada en esas fechas. "
                    + "Elige otras fechas u otra habitación.");
        }
    }

    /** Calcula noches, subtotales y total. El precio siempre lo calcula el servidor. */
    private void aplicarDatos(Reserva reserva, Habitacion habitacion, ReservaRequest request) {
        int noches = (int) ChronoUnit.DAYS.between(request.fechaEntrada(), request.fechaSalida());
        BigDecimal subtotalHabitacion = habitacion.getTipo().getPrecioNoche().multiply(BigDecimal.valueOf(noches));

        reserva.setHabitacion(habitacion);
        reserva.setFechaEntrada(request.fechaEntrada());
        reserva.setFechaSalida(request.fechaSalida());
        reserva.setNumeroNoches(noches);
        reserva.setNumeroHuespedes(request.numeroHuespedes());
        reserva.setSolicitudesEspeciales(request.solicitudesEspeciales() == null || request.solicitudesEspeciales().isBlank()
                ? null : request.solicitudesEspeciales().trim());
        reserva.setSubtotalHabitacion(subtotalHabitacion);

        actualizarServicios(reserva, request.servicios() == null ? List.of() : request.servicios());

        BigDecimal subtotalServicios = reserva.getServicios().stream()
                .map(ReservaServicio::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        reserva.setSubtotalServicios(subtotalServicios);
        reserva.setPrecioTotal(subtotalHabitacion.add(subtotalServicios));
    }

    /** Sincroniza los servicios adicionales sin borrar y volver a insertar los que ya existen. */
    private void actualizarServicios(Reserva reserva, List<ServicioReservaRequest> solicitados) {
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (ServicioReservaRequest item : solicitados) {
            if (cantidades.containsKey(item.servicioId())) {
                throw new ReglaNegocioException("El servicio con ID " + item.servicioId() + " está repetido en la reserva.");
            }
            cantidades.put(item.servicioId(), item.cantidad());
        }

        // Quitar los que ya no se solicitan
        reserva.getServicios().removeIf(rs -> !cantidades.containsKey(rs.getServicio().getId()));

        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {
            Servicio servicio = servicioRepository.findById(entrada.getKey())
                    .orElseThrow(() -> RecursoNoEncontradoException.de("el servicio", entrada.getKey()));
            if (!Boolean.TRUE.equals(servicio.getAdicional())) {
                throw new ReglaNegocioException("El servicio \"" + servicio.getNombre()
                        + "\" ya está incluido en las habitaciones y no se cobra aparte.");
            }
            ReservaServicio existente = reserva.getServicios().stream()
                    .filter(rs -> rs.getServicio().getId().equals(servicio.getId()))
                    .findFirst()
                    .orElse(null);
            if (existente == null) {
                existente = new ReservaServicio();
                existente.setReserva(reserva);
                existente.setServicio(servicio);
                reserva.getServicios().add(existente);
            }
            existente.setCantidad(entrada.getValue());
            existente.setPrecioUnitario(servicio.getCosto());
        }
    }

    private void validarCancelable(Reserva reserva) {
        if (reserva.getEstado() != EstadoReserva.PENDIENTE && reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new ReglaNegocioException("Solo se pueden cancelar reservas PENDIENTES o CONFIRMADAS. "
                    + "Esta reserva está " + reserva.getEstado() + ".");
        }
        if (LocalDateTime.now().isAfter(reserva.getFechaEntrada().atTime(HORA_CHECK_IN, 0))) {
            throw new ReglaNegocioException("La hora de llegada de esta reserva ya pasó, por lo que no puede cancelarse "
                    + "en línea. Comunícate con la recepción del hotel.");
        }
    }

    /** Aplica la política de cancelación sobre el monto efectivamente pagado. */
    private Cancelacion calcularCancelacion(Reserva reserva, Usuario usuario, String motivo) {
        long horas = Duration.between(LocalDateTime.now(), reserva.getFechaEntrada().atTime(HORA_CHECK_IN, 0)).toHours();
        BigDecimal porcentaje = horas >= HORAS_CANCELACION_GRATIS ? BigDecimal.ZERO.setScale(2) : PORCENTAJE_PENALIZACION;

        Pago pago = reserva.getPago();
        BigDecimal pagado = pago != null && pago.getEstado() == EstadoPago.COMPLETADO ? pago.getMonto() : BigDecimal.ZERO;
        BigDecimal penalizacion = pagado.multiply(porcentaje).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        if (pagado.signum() == 0) {
            porcentaje = BigDecimal.ZERO.setScale(2); // sin pago no hay nada que retener
        }

        Cancelacion cancelacion = new Cancelacion();
        cancelacion.setReserva(reserva);
        cancelacion.setCanceladoPor(usuario);
        cancelacion.setMotivo(motivo);
        cancelacion.setHorasAnticipacion((int) Math.max(horas, 0));
        cancelacion.setPorcentajePenalizacion(porcentaje);
        cancelacion.setMontoPenalizacion(penalizacion);
        cancelacion.setMontoReembolso(pagado.subtract(penalizacion));
        return cancelacion;
    }
}
