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
import sv.edu.udb.hotel.dto.request.HuespedRequest;
import sv.edu.udb.hotel.dto.response.HuespedResponse;
import sv.edu.udb.hotel.dto.response.MensajeResponse;
import sv.edu.udb.hotel.service.HuespedService;

import java.util.List;

@RestController
@RequestMapping("/api/huespedes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "8. Huéspedes", description = "Registro de huéspedes (documento único)")
public class HuespedController {

    private final HuespedService huespedService;

    @GetMapping("/mi-perfil")
    @PreAuthorize("hasRole('HUESPED')")
    @Operation(summary = "Ver mis datos de huésped (rol HUESPED)")
    public HuespedResponse miPerfil() {
        return huespedService.miPerfil();
    }

    @PutMapping("/mi-perfil")
    @PreAuthorize("hasRole('HUESPED')")
    @Operation(summary = "Guardar mis datos de huésped (rol HUESPED)")
    public HuespedResponse guardarMiPerfil(@Valid @RequestBody HuespedRequest request) {
        return huespedService.guardarMiPerfil(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    @Operation(summary = "Listar o buscar huéspedes", description = "Busca por nombre, documento o correo")
    public List<HuespedResponse> listar(@RequestParam(required = false) String buscar) {
        return huespedService.listar(buscar);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    @Operation(summary = "Obtener un huésped")
    public HuespedResponse obtener(@PathVariable Long id) {
        return huespedService.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    @Operation(summary = "Registrar huésped en recepción")
    public ResponseEntity<HuespedResponse> crear(@Valid @RequestBody HuespedRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(huespedService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    @Operation(summary = "Actualizar huésped")
    public HuespedResponse actualizar(@PathVariable Long id, @Valid @RequestBody HuespedRequest request) {
        return huespedService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar huésped", description = "Solo si no tiene reservas ni reclamos")
    public MensajeResponse eliminar(@PathVariable Long id) {
        huespedService.eliminar(id);
        return new MensajeResponse("Huésped eliminado correctamente.");
    }
}
