package sv.edu.udb.hotel.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SoloLetrasValidator implements ConstraintValidator<SoloLetras, String> {

    private static final String REGEX = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ .'-]+$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Si viene vacío lo controla @NotBlank
        if (value == null || value.isBlank()) {
            return true;
        }
        return value.trim().matches(REGEX);
    }
}
