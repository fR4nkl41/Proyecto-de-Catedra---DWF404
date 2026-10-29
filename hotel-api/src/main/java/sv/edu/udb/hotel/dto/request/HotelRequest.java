package sv.edu.udb.hotel.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import sv.edu.udb.hotel.validation.Patrones;
import sv.edu.udb.hotel.validation.SoloLetras;

@Schema(description = "Datos de un hotel")
public record HotelRequest(
        @Schema(example = "Hotel Real Intercontinental")
        @NotBlank(message = "El nombre del hotel es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String nombre,

        @Schema(example = "Boulevard de Los Héroes y Av. Sisimiles")
        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 200, message = "La dirección no puede superar 200 caracteres")
        String direccion,

        @Schema(example = "San Salvador")
        @NotBlank(message = "La ciudad es obligatoria")
        @Size(max = 60, message = "La ciudad no puede superar 60 caracteres")
        @SoloLetras(message = "La ciudad solo puede contener letras")
        String ciudad,

        @Schema(example = "2211-3333")
        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = Patrones.TELEFONO, message = "El teléfono debe tener el formato 2222-3333")
        String telefono,

        @Schema(example = "info@hotel.com")
        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "Ingresa un correo electrónico válido")
        @Size(max = 80, message = "El correo no puede superar 80 caracteres")
        String email,

        @Schema(example = "Hotel de negocios frente a Metrocentro.")
        @Size(max = 300, message = "La descripción no puede superar 300 caracteres")
        String descripcion
) {
}
