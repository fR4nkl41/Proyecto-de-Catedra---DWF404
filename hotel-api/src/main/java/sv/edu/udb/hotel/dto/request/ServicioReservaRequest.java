package sv.edu.udb.hotel.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Servicio adicional que se agrega a la reserva")
public record ServicioReservaRequest(
        @Schema(example = "5")
        @NotNull(message = "El servicio es obligatorio")
        @Positive(message = "El ID del servicio debe ser positivo")
        Long servicioId,

        @Schema(example = "1")
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad mínima es 1")
        @Max(value = 10, message = "La cantidad máxima es 10")
        Integer cantidad
) {
}
