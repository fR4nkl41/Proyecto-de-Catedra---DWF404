package sv.edu.udb.hotel.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Activar o desactivar una cuenta")
public record EstadoUsuarioRequest(
        @Schema(example = "false")
        @NotNull(message = "Indica si el usuario queda activo (true) o inactivo (false)")
        Boolean activo
) {
}
