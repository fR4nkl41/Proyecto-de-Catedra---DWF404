package sv.edu.udb.hotel.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import sv.edu.udb.hotel.model.enums.TipoDocumento;
import sv.edu.udb.hotel.validation.DocumentoValido;
import sv.edu.udb.hotel.validation.MayorDeEdad;
import sv.edu.udb.hotel.validation.Patrones;
import sv.edu.udb.hotel.validation.SoloLetras;

import java.time.LocalDate;

@DocumentoValido
@Schema(description = "Datos personales del huésped")
public record HuespedRequest(
        @Schema(example = "Carlos Pérez")
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        @SoloLetras(message = "El nombre solo puede contener letras y espacios")
        String nombreCompleto,

        @Schema(example = "1990-05-15")
        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
        @MayorDeEdad
        LocalDate fechaNacimiento,

        @Schema(example = "DUI")
        @NotNull(message = "El tipo de documento es obligatorio (DUI o PASAPORTE)")
        TipoDocumento tipoDocumento,

        @Schema(example = "01234567-8")
        @NotBlank(message = "El número de documento es obligatorio")
        @Size(max = 20, message = "El documento no puede superar 20 caracteres")
        String documento,

        @Schema(example = "7777-8888")
        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = Patrones.TELEFONO, message = "El teléfono debe tener el formato 7777-8888")
        String telefono,

        @Schema(example = "carlos@mail.com")
        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "Ingresa un correo electrónico válido")
        @Size(max = 80, message = "El correo no puede superar 80 caracteres")
        String email
) {
}
