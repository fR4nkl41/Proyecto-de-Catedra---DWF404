package sv.edu.udb.hotel.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import sv.edu.udb.hotel.validation.Patrones;
import sv.edu.udb.hotel.validation.SoloLetras;

@Schema(description = "Datos de una sucursal")
public record SucursalRequest(
        @Schema(example = "1")
        @NotNull(message = "Selecciona el hotel al que pertenece la sucursal")
        @Positive(message = "El ID del hotel debe ser un número positivo")
        Long hotelId,

        @Schema(example = "Sede Los Héroes")
        @NotBlank(message = "El nombre de la sucursal es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String nombre,

        @Schema(example = "Boulevard de Los Héroes #1200")
        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 200, message = "La dirección no puede superar 200 caracteres")
        String direccion,

        @Schema(example = "San Salvador")
        @NotBlank(message = "La ciudad es obligatoria")
        @Size(max = 60, message = "La ciudad no puede superar 60 caracteres")
        @SoloLetras(message = "La ciudad solo puede contener letras")
        String ciudad,

        @Schema(example = "2211-3334")
        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = Patrones.TELEFONO, message = "El teléfono debe tener el formato 2222-3333")
        String telefono
) {
}
