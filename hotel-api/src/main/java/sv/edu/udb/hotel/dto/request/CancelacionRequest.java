package sv.edu.udb.hotel.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Motivo de la cancelación")
public record CancelacionRequest(
        @Schema(example = "Cambio de planes de viaje")
        @NotBlank(message = "El motivo de la cancelación es obligatorio")
        @Size(min = 5, max = 250, message = "El motivo debe tener entre 5 y 250 caracteres")
        String motivo
) {
}
