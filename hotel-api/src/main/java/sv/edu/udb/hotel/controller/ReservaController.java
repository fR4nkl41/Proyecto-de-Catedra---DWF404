package sv.edu.udb.hotel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.hotel.dto.request.CancelacionRequest;
import sv.edu.udb.hotel.dto.request.ReservaRequest;
import sv.edu.udb.hotel.dto.response.CancelacionResponse;
import sv.edu.udb.hotel.dto.response.MensajeResponse;
import sv.edu.udb.hotel.dto.response.ReservaResponse;
import sv.edu.udb.hotel.model.enums.EstadoReserva;
import sv.edu.udb.hotel.service.ReservaService;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "9. Reservas", description = "Flujo de reserva, cancelaciones con penalización, check-in y check-out")
public class ReservaController {

    private final ReservaService reservaService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar reservas", description = "El personal ve todas; el huésped solo las suyas")
    public List<ReservaResponse> listar(@RequestParam(required = false) EstadoReserva estado) {
        return reservaService.listar(estado);
    }

    @GetMapping("/cancelaciones")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    @Operation(summary = "Historial de cancelaciones")
    public List<CancelacionResponse> listarCancelaciones() {
        return reservaService.listarCancelaciones();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Detalle de una reserva")
    public ReservaResponse obtener(@PathVariable Long id) {
        return reservaService.obtener(id);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Crear reserva", description = "Queda PENDIENTE hasta registrar el pago. El total lo calcula el servidor.")
    @ApiResponse(responseCode = "201", description = "Reserva creada")
    @ApiResponse(responseCode = "409", description = "La habitación ya está ocupada en esas fechas")
    public ResponseEntity<ReservaResponse> crear(@Valid @RequestBody ReservaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Modificar reserva", description = "Solo reservas PENDIENTES")
    public ReservaResponse actualizar(@PathVariable Long id, @Valid @RequestBody ReservaRequest request) {
        return reservaService.actualizar(id, request);
    }

    @GetMapping("/{id}/cancelacion/simular")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Calcular penalización antes de cancelar")
    public CancelacionResponse simularCancelacion(@PathVariable Long id) {
        return reservaService.simularCancelacion(id);
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancelar reserva", description = "Aplica la política de cancelación y calcula el reembolso")
    public CancelacionResponse cancelar(@PathVariable Long id, @Valid @RequestBody CancelacionRequest request) {
        return reservaService.cancelar(id, request);
    }

    @PatchMapping("/{id}/check-in")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    @Operation(summary = "Registrar llegada del huésped", description = "CONFIRMADA -> EN_CURSO; la habitación pasa a OCUPADA")
    public ReservaResponse checkIn(@PathVariable Long id) {
        return reservaService.registrarCheckIn(id);
    }

    @PatchMapping("/{id}/check-out")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    @Operation(summary = "Registrar salida del huésped", description = "EN_CURSO -> FINALIZADA; la habitación queda DISPONIBLE")
    public ReservaResponse checkOut(@PathVariable Long id) {
        return reservaService.registrarCheckOut(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar reserva", description = "Solo PENDIENTES sin pago o CANCELADAS")
    public MensajeResponse eliminar(@PathVariable Long id) {
        reservaService.eliminar(id);
        return new MensajeResponse("Reserva eliminada correctamente.");
    }
}
