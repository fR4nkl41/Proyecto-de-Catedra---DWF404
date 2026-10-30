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
import sv.edu.udb.hotel.dto.request.TipoHabitacionRequest;
import sv.edu.udb.hotel.dto.response.MensajeResponse;
import sv.edu.udb.hotel.dto.response.TipoHabitacionResponse;
import sv.edu.udb.hotel.service.TipoHabitacionService;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-habitacion")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "5. Tipos de habitación", description = "Categorías y tarifas por noche")
public class TipoHabitacionController {

    private final TipoHabitacionService tipoService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar tipos de habitación")
    public List<TipoHabitacionResponse> listar() {
        return tipoService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener un tipo de habitación")
    public TipoHabitacionResponse obtener(@PathVariable Long id) {
        return tipoService.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear tipo de habitación")
    public ResponseEntity<TipoHabitacionResponse> crear(@Valid @RequestBody TipoHabitacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar tipo de habitación y su tarifa")
    public TipoHabitacionResponse actualizar(@PathVariable Long id, @Valid @RequestBody TipoHabitacionRequest request) {
        return tipoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar tipo de habitación")
    public MensajeResponse eliminar(@PathVariable Long id) {
        tipoService.eliminar(id);
        return new MensajeResponse("Tipo de habitación eliminado correctamente.");
    }
}
