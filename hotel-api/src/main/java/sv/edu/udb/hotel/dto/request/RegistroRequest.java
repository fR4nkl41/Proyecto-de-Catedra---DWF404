package sv.edu.udb.hotel.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import sv.edu.udb.hotel.validation.Patrones;
import sv.edu.udb.hotel.validation.SoloLetras;

@Schema(description = "Datos para crear una cuenta de huésped")
public record RegistroRequest(
        @Schema(example = "María Rodríguez")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        @SoloLetras(message = "El nombre solo puede contener letras y espacios")
        String nombre,

        @Schema(example = "maria@ejemplo.com")
        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "Ingresa un correo electrónico válido")
        @Size(max = 80, message = "El correo no puede superar 80 caracteres")
        String email,

        @Schema(example = "Maria2026")
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 50, message = "La contraseña debe tener entre 6 y 50 caracteres")
        @Pattern(regexp = Patrones.PASSWORD, message = "La contraseña debe contener al menos una letra y un número")
        String password
) {
}
