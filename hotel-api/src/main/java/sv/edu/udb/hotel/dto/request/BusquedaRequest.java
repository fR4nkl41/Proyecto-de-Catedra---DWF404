package sv.edu.udb.hotel.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import sv.edu.udb.hotel.validation.FechasReserva;
import sv.edu.udb.hotel.validation.RangoFechas;

import java.time.LocalDate;

@FechasReserva
@Schema(description = "Filtros del buscador de habitaciones disponibles")
public record BusquedaRequest(
        @Schema(example = "San Salvador")
        @Size(max = 60, message = "La ciudad no puede superar 60 caracteres")
        String ciudad,

        @Schema(example = "2026-11-15")
        @NotNull(message = "La fecha de entrada es obligatoria")
        @FutureOrPresent(message = "La fecha de entrada no puede ser anterior a hoy")
        LocalDate fechaEntrada,

        @Schema(example = "2026-11-20")
        @NotNull(message = "La fecha de salida es obligatoria")
        LocalDate fechaSalida,

        @Schema(example = "2")
        @NotNull(message = "El número de huéspedes es obligatorio")
        @Min(value = 1, message = "Debe haber al menos 1 huésped")
        @Max(value = 10, message = "El máximo es 10 huéspedes")
        Integer huespedes,

        @Schema(example = "3")
        @Positive(message = "El tipo de habitación no es válido")
        Long tipoId
) implements RangoFechas {
}
