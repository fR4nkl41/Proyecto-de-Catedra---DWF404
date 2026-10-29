package sv.edu.udb.hotel.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import sv.edu.udb.hotel.model.enums.EstadoHabitacion;

@Schema(description = "Cambio de estado de una habitación")
public record EstadoHabitacionRequest(
        @Schema(example = "MANTENIMIENTO")
        @NotNull(message = "El estado es obligatorio (DISPONIBLE, OCUPADA o MANTENIMIENTO)")
        EstadoHabitacion estado
) {
}
