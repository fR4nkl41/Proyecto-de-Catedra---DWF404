package sv.edu.udb.hotel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.hotel.dto.response.DashboardResponse;
import sv.edu.udb.hotel.service.EstadisticaService;

@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "12. Estadísticas", description = "Panel de control del personal")
public class EstadisticaController {

    private final EstadisticaService estadisticaService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    @Operation(summary = "Métricas generales, ocupación semanal y próximas llegadas")
    public DashboardResponse dashboard() {
        return estadisticaService.dashboard();
    }
}
