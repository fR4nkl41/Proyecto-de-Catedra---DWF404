package sv.edu.udb.hotel.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos de un reclamo")
public record ReclamoRequest(
        @Schema(description = "Reserva relacionada (opcional)", example = "1")
        @Positive(message = "La reserva no es válida")
        Long reservaId,

        @Schema(description = "Solo lo envía el personal del hotel. El huésped usa su propio perfil.", example = "3")
        @Positive(message = "El huésped no es válido")
        Long huespedId,

        @Schema(example = "Solicitud de cuna")
        @NotBlank(message = "El asunto es obligatorio")
        @Size(min = 3, max = 100, message = "El asunto debe tener entre 3 y 100 caracteres")
        String asunto,

        @Schema(example = "Quisiera saber si es posible agregar una cuna para bebé.")
        @NotBlank(message = "La descripción es obligatoria")
        @Size(min = 10, max = 500, message = "La descripción debe tener entre 10 y 500 caracteres")
        String descripcion
) {
}
