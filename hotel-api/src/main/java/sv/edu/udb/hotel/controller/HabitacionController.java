package sv.edu.udb.hotel.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springdoc.core.annotations.ParameterObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.hotel.dto.request.BusquedaRequest;
import sv.edu.udb.hotel.dto.request.EstadoHabitacionRequest;
import sv.edu.udb.hotel.dto.request.HabitacionRequest;
import sv.edu.udb.hotel.dto.response.DisponibilidadResponse;
import sv.edu.udb.hotel.dto.response.HabitacionResponse;
import sv.edu.udb.hotel.dto.response.MensajeResponse;
import sv.edu.udb.hotel.model.enums.EstadoHabitacion;
import sv.edu.udb.hotel.service.HabitacionService;

import java.util.List;

@RestController
@RequestMapping("/api/habitaciones")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "7. Habitaciones", description = "Habitaciones y motor de búsqueda de disponibilidad")
public class HabitacionController {

    private final HabitacionService habitacionService;

    @GetMapping("/disponibles")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Buscar habitaciones disponibles",
            description = "Filtra por ciudad, fechas, número de huéspedes y tipo. Excluye habitaciones en mantenimiento "
                    + "o con reservas activas en esas fechas.")
    public List<DisponibilidadResponse> buscarDisponibles(@Valid @ParameterObject @ModelAttribute BusquedaRequest filtro) {
        return habitacionService.buscarDisponibles(filtro);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    @Operation(summary = "Listar habitaciones", description = "Filtros opcionales por sucursal y estado")
    public List<HabitacionResponse> listar(@RequestParam(required = false) Long sucursalId,
                                           @RequestParam(required = false) EstadoHabitacion estado) {
        return habitacionService.listar(sucursalId, estado);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener detalle de una habitación")
    public HabitacionResponse obtener(@PathVariable Long id) {
        return habitacionService.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear habitación")
    public ResponseEntity<HabitacionResponse> crear(@Valid @RequestBody HabitacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(habitacionService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar habitación")
    public HabitacionResponse actualizar(@PathVariable Long id, @Valid @RequestBody HabitacionRequest request) {
        return habitacionService.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    @Operation(summary = "Cambiar estado (DISPONIBLE, OCUPADA, MANTENIMIENTO)")
    public HabitacionResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoHabitacionRequest request) {
        return habitacionService.cambiarEstado(id, request.estado());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar habitación", description = "Solo si nunca ha tenido reservas")
    public MensajeResponse eliminar(@PathVariable Long id) {
        habitacionService.eliminar(id);
        return new MensajeResponse("Habitación eliminada correctamente.");
    }
}
