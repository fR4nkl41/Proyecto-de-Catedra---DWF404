package sv.edu.udb.hotel.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Datos de un servicio")
public record ServicioRequest(
        @Schema(example = "Traslado al aeropuerto")
        @NotBlank(message = "El nombre del servicio es obligatorio")
        @Size(max = 80, message = "El nombre no puede superar 80 caracteres")
        String nombre,

        @Schema(example = "Transporte desde o hacia el aeropuerto.")
        @Size(max = 250, message = "La descripción no puede superar 250 caracteres")
        String descripcion,

        @Schema(example = "25.00")
        @NotNull(message = "El costo es obligatorio (use 0 si está incluido)")
        @DecimalMin(value = "0.00", message = "El costo no puede ser negativo")
        @Digits(integer = 8, fraction = 2, message = "El costo debe tener máximo 2 decimales")
        BigDecimal costo,

        @Schema(description = "true = se contrata en la reserva; false = incluido en la habitación", example = "true")
        @NotNull(message = "Indica si el servicio es adicional o incluido")
        Boolean adicional
) {
}
