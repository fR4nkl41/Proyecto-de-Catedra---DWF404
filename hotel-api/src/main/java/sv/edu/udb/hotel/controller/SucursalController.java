package sv.edu.udb.hotel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.hotel.dto.request.SucursalRequest;
import sv.edu.udb.hotel.dto.response.MensajeResponse;
import sv.edu.udb.hotel.dto.response.SucursalResponse;
import sv.edu.udb.hotel.service.SucursalService;

import java.util.List;

@RestController
@RequestMapping("/api/sucursales")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "4. Sucursales", description = "Sedes de cada hotel")
public class SucursalController {

    private final SucursalService sucursalService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar sucursales", description = "Filtro opcional por hotel")
    public List<SucursalResponse> listar(@RequestParam(required = false) Long hotelId) {
        return sucursalService.listar(hotelId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener una sucursal")
    public SucursalResponse obtener(@PathVariable Long id) {
        return sucursalService.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear sucursal")
    public ResponseEntity<SucursalResponse> crear(@Valid @RequestBody SucursalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sucursalService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar sucursal")
    public SucursalResponse actualizar(@PathVariable Long id, @Valid @RequestBody SucursalRequest request) {
        return sucursalService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar sucursal", description = "Solo si no tiene habitaciones")
    public MensajeResponse eliminar(@PathVariable Long id) {
        sucursalService.eliminar(id);
        return new MensajeResponse("Sucursal eliminada correctamente.");
    }
}
