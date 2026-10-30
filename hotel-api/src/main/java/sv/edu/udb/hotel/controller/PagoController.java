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
import sv.edu.udb.hotel.dto.request.HotelRequest;
import sv.edu.udb.hotel.dto.response.HotelResponse;
import sv.edu.udb.hotel.dto.response.MensajeResponse;
import sv.edu.udb.hotel.service.HotelService;

import java.util.List;

@RestController
@RequestMapping("/api/hoteles")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "3. Hoteles", description = "Consulta para todos los roles; administración solo ADMIN")
public class HotelController {

    private final HotelService hotelService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar hoteles")
    public List<HotelResponse> listar() {
        return hotelService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener un hotel")
    public HotelResponse obtener(@PathVariable Long id) {
        return hotelService.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear hotel")
    public ResponseEntity<HotelResponse> crear(@Valid @RequestBody HotelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hotelService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar hotel")
    public HotelResponse actualizar(@PathVariable Long id, @Valid @RequestBody HotelRequest request) {
        return hotelService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar hotel", description = "Solo si no tiene sucursales")
    public MensajeResponse eliminar(@PathVariable Long id) {
        hotelService.eliminar(id);
        return new MensajeResponse("Hotel eliminado correctamente.");
    }
}
