package sv.edu.udb.hotel.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import sv.edu.udb.hotel.model.enums.EstadoHabitacion;
import sv.edu.udb.hotel.validation.Patrones;

import java.util.List;

@Schema(description = "Datos de una habitación")
public record HabitacionRequest(
        @Schema(example = "1")
        @NotNull(message = "Selecciona la sucursal")
        @Positive(message = "El ID de la sucursal debe ser positivo")
        Long sucursalId,

        @Schema(example = "2")
        @NotNull(message = "Selecciona el tipo de habitación")
        @Positive(message = "El ID del tipo debe ser positivo")
        Long tipoId,

        @Schema(example = "101")
        @NotBlank(message = "El número de habitación es obligatorio")
        @Pattern(regexp = Patrones.NUMERO_HABITACION, message = "El número debe tener de 1 a 4 dígitos y una letra opcional (ej. 101 o 12B)")
        String numero,

        @Schema(example = "1")
        @NotNull(message = "El piso es obligatorio")
        @Min(value = 0, message = "El piso no puede ser negativo")
        @Max(value = 60, message = "El piso no puede ser mayor a 60")
        Integer piso,

        @Schema(example = "DISPONIBLE")
        EstadoHabitacion estado,

        @Schema(description = "IDs de los servicios incluidos", example = "[1, 2]")
        List<@NotNull @Positive Long> servicioIds
) {
}
