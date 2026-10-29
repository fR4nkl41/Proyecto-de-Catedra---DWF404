package sv.edu.udb.hotel.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Datos de un tipo de habitación y su tarifa")
public record TipoHabitacionRequest(
        @Schema(example = "Suite")
        @NotBlank(message = "El nombre del tipo es obligatorio")
        @Size(max = 50, message = "El nombre no puede superar 50 caracteres")
        String nombre,

        @Schema(example = "Suite amplia con sala de estar y jacuzzi.")
        @Size(max = 250, message = "La descripción no puede superar 250 caracteres")
        String descripcion,

        @Schema(example = "4")
        @NotNull(message = "La capacidad máxima es obligatoria")
        @Min(value = 1, message = "La capacidad mínima es 1 persona")
        @Max(value = 10, message = "La capacidad máxima es 10 personas")
        Integer capacidadMaxima,

        @Schema(example = "250.00")
        @NotNull(message = "El precio por noche es obligatorio")
        @DecimalMin(value = "1.00", message = "El precio por noche debe ser mayor a $0.99")
        @Digits(integer = 8, fraction = 2, message = "El precio debe tener máximo 2 decimales")
        BigDecimal precioNoche
) {
}
