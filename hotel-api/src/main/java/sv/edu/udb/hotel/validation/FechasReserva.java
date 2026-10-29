package sv.edu.udb.hotel.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Valida que la fecha de salida sea posterior a la de entrada
 * y que la estadía no supere el máximo de noches permitido.
 */
@Documented
@Constraint(validatedBy = FechasReservaValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FechasReserva {

    String message() default "La fecha de salida debe ser posterior a la fecha de entrada";

    int maxNoches() default 30;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
