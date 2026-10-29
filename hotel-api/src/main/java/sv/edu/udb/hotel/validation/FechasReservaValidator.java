package sv.edu.udb.hotel.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.temporal.ChronoUnit;

public class FechasReservaValidator implements ConstraintValidator<FechasReserva, RangoFechas> {

    private int maxNoches;

    @Override
    public void initialize(FechasReserva anotacion) {
        this.maxNoches = anotacion.maxNoches();
    }

    @Override
    public boolean isValid(RangoFechas dto, ConstraintValidatorContext context) {
        if (dto == null || dto.fechaEntrada() == null || dto.fechaSalida() == null) {
            return true; // los nulos los valida @NotNull
        }
        String mensaje = null;
        if (!dto.fechaSalida().isAfter(dto.fechaEntrada())) {
            mensaje = "La fecha de salida debe ser posterior a la fecha de entrada";
        } else if (ChronoUnit.DAYS.between(dto.fechaEntrada(), dto.fechaSalida()) > maxNoches) {
            mensaje = "La estadía no puede superar " + maxNoches + " noches";
        }
        if (mensaje == null) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(mensaje)
                .addPropertyNode("fechaSalida")
                .addConstraintViolation();
        return false;
    }
}
