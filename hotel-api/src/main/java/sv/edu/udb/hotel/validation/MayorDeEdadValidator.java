package sv.edu.udb.hotel.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class MayorDeEdadValidator implements ConstraintValidator<MayorDeEdad, LocalDate> {

    @Override
    public boolean isValid(LocalDate fecha, ConstraintValidatorContext context) {
        if (fecha == null) {
            return true;
        }
        int edad = Period.between(fecha, LocalDate.now()).getYears();
        return edad >= 18 && edad <= 120;
    }
}

