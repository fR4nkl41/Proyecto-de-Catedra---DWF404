package sv.edu.udb.hotel.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import sv.edu.udb.hotel.model.enums.Rol;
import sv.edu.udb.hotel.validation.Patrones;
import sv.edu.udb.hotel.validation.SoloLetras;

@Schema(description = "Datos de un usuario gestionado por el administrador")
public record UsuarioRequest(
        @Schema(example = "Pedro Hernández")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        @SoloLetras(message = "El nombre solo puede contener letras y espacios")
        String nombre,

        @Schema(example = "pedro@hotel.com")
        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "Ingresa un correo electrónico válido")
        @Size(max = 80, message = "El correo no puede superar 80 caracteres")
        String email,

        @Schema(description = "Obligatoria al crear. Al editar, déjala vacía para conservar la actual.", example = "Pedro2026")
        @Size(min = 6, max = 50, message = "La contraseña debe tener entre 6 y 50 caracteres")
        @Pattern(regexp = Patrones.PASSWORD, message = "La contraseña debe contener al menos una letra y un número")
        String password,

        @Schema(example = "RECEPCIONISTA")
        @NotNull(message = "El rol es obligatorio")
        Rol rol,

        @Schema(example = "true")
        Boolean activo
) {
}
