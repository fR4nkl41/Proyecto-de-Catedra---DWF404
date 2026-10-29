package sv.edu.udb.hotel.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/** Valida que el texto contenga solo letras (incluye tildes y ñ) y espacios. */
@Documented
@Constraint(validatedBy = SoloLetrasValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface SoloLetras {

    String message() default "Solo se permiten letras y espacios";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
