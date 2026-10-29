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
import sv.edu.udb.hotel.dto.request.ReclamoRequest;
import sv.edu.udb.hotel.dto.request.RespuestaReclamoRequest;
import sv.edu.udb.hotel.dto.response.MensajeResponse;
import sv.edu.udb.hotel.dto.response.ReclamoResponse;
import sv.edu.udb.hotel.model.enums.EstadoReclamo;
import sv.edu.udb.hotel.service.ReclamoService;

import java.util.List;

@RestController
@RequestMapping("/api/reclamos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "11. Reclamos", description = "Quejas y solicitudes de los huéspedes")
public class ReclamoController {

    private final ReclamoService reclamoService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar reclamos", description = "El personal ve todos; el huésped solo los suyos")
    public List<ReclamoResponse> listar(@RequestParam(required = false) EstadoReclamo estado) {
        return reclamoService.listar(estado);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener un reclamo")
    public ReclamoResponse obtener(@PathVariable Long id) {
        return reclamoService.obtener(id);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Crear reclamo")
    public ResponseEntity<ReclamoResponse> crear(@Valid @RequestBody ReclamoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reclamoService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Editar reclamo", description = "Solo mientras esté ABIERTO")
    public ReclamoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ReclamoRequest request) {
        return reclamoService.actualizar(id, request);
    }

    @PatchMapping("/{id}/respuesta")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    @Operation(summary = "Responder reclamo y cambiar su estado")
    public ReclamoResponse responder(@PathVariable Long id, @Valid @RequestBody RespuestaReclamoRequest request) {
        return reclamoService.responder(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar reclamo")
    public MensajeResponse eliminar(@PathVariable Long id) {
        reclamoService.eliminar(id);
        return new MensajeResponse("Reclamo eliminado correctamente.");
    }
}
