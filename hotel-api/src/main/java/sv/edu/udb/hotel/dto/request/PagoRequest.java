package sv.edu.udb.hotel.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import sv.edu.udb.hotel.model.enums.MetodoPago;
import sv.edu.udb.hotel.validation.SoloLetras;

@Schema(description = "Pago simulado de una reserva. El monto lo calcula el servidor.")
public record PagoRequest(
        @Schema(example = "2")
        @NotNull(message = "La reserva es obligatoria")
        @Positive(message = "El ID de la reserva debe ser positivo")
        Long reservaId,

        @Schema(example = "TARJETA")
        @NotNull(message = "Selecciona el método de pago (TARJETA, EFECTIVO o TRANSFERENCIA)")
        MetodoPago metodoPago,

        @Schema(description = "Solo para TARJETA (sin espacios)", example = "4242424242424242")
        @Pattern(regexp = "^\\d{13,19}$", message = "El número de tarjeta debe tener entre 13 y 19 dígitos")
        String numeroTarjeta,

        @Schema(description = "Solo para TARJETA", example = "Carlos Perez")
        @Size(max = 100, message = "El nombre del titular no puede superar 100 caracteres")
        @SoloLetras(message = "El nombre del titular solo puede contener letras")
        String titular,

        @Schema(description = "Solo para TARJETA, formato MM/AA", example = "12/28")
        @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "La fecha de vencimiento debe tener el formato MM/AA")
        String vencimiento,

        @Schema(description = "Solo para TARJETA", example = "123")
        @Pattern(regexp = "^\\d{3,4}$", message = "El CVV debe tener 3 o 4 dígitos")
        String cvv,

        @Schema(description = "Solo para TRANSFERENCIA", example = "TRF-778812")
        @Pattern(regexp = "^[A-Za-z0-9-]{6,20}$", message = "La referencia debe tener de 6 a 20 letras, números o guiones")
        String referenciaTransferencia
) {
}
