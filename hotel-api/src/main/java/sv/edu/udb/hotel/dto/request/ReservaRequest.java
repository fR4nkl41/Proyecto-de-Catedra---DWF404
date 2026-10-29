package sv.edu.udb.hotel.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import sv.edu.udb.hotel.validation.FechasReserva;
import sv.edu.udb.hotel.validation.RangoFechas;

import java.time.LocalDate;
import java.util.List;

@FechasReserva
@Schema(description = "Datos para crear o modificar una reserva")
public record ReservaRequest(
        @Schema(example = "4")
        @NotNull(message = "Selecciona la habitación")
        @Positive(message = "El ID de la habitación debe ser positivo")
        Long habitacionId,

        @Schema(example = "2026-11-15")
        @NotNull(message = "La fecha de entrada es obligatoria")
        @FutureOrPresent(message = "La fecha de entrada no puede ser anterior a hoy")
        LocalDate fechaEntrada,

        @Schema(example = "2026-11-20")
        @NotNull(message = "La fecha de salida es obligatoria")
        @Future(message = "La fecha de salida debe ser posterior a hoy")
        LocalDate fechaSalida,

        @Schema(example = "2")
        @NotNull(message = "El número de huéspedes es obligatorio")
        @Min(value = 1, message = "Debe haber al menos 1 huésped")
        @Max(value = 10, message = "El máximo es 10 huéspedes por reserva")
        Integer numeroHuespedes,

        @NotNull(message = "Los datos del huésped son obligatorios")
        @Valid
        HuespedRequest huesped,

        @Valid
        List<ServicioReservaRequest> servicios,

        @Schema(example = "Habitación en piso alto")
        @Size(max = 250, message = "Las solicitudes especiales no pueden superar 250 caracteres")
        String solicitudesEspeciales
) implements RangoFechas {
}
