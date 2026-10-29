package sv.edu.udb.hotel.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import sv.edu.udb.hotel.model.enums.EstadoReclamo;

@Schema(description = "Respuesta del personal a un reclamo")
public record RespuestaReclamoRequest(
        @Schema(example = "RESUELTO")
        @NotNull(message = "El estado es obligatorio (ABIERTO, EN_PROCESO o RESUELTO)")
        EstadoReclamo estado,

        @Schema(example = "Con gusto colocaremos una cuna en su suite.")
        @NotBlank(message = "La respuesta es obligatoria")
        @Size(min = 5, max = 500, message = "La respuesta debe tener entre 5 y 500 caracteres")
        String respuesta
) {
}
