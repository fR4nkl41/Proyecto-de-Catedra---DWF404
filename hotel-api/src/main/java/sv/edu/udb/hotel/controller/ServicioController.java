package sv.edu.udb.hotel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.hotel.dto.request.ServicioRequest;
import sv.edu.udb.hotel.dto.response.MensajeResponse;
import sv.edu.udb.hotel.dto.response.ServicioResponse;
import sv.edu.udb.hotel.service.ServicioService;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "6. Servicios", description = "Servicios incluidos y adicionales (alimentación, transporte, lavandería...)")
public class ServicioController {

    private final ServicioService servicioService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar servicios", description = "adicional=true para los que se contratan en la reserva")
    public List<ServicioResponse> listar(@Parameter(example = "true") @RequestParam(required = false) Boolean adicional) {
        return servicioService.listar(adicional);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener un servicio")
    public ServicioResponse obtener(@PathVariable Long id) {
        return servicioService.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear servicio")
    public ResponseEntity<ServicioResponse> crear(@Valid @RequestBody ServicioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar servicio")
    public ServicioResponse actualizar(@PathVariable Long id, @Valid @RequestBody ServicioRequest request) {
        return servicioService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar servicio")
    public MensajeResponse eliminar(@PathVariable Long id) {
        servicioService.eliminar(id);
        return new MensajeResponse("Servicio eliminado correctamente.");
    }
}
