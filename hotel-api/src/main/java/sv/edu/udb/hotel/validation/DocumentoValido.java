package sv.edu.udb.hotel.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Valida el número de documento según su tipo:
 * DUI = 00000000-0 | PASAPORTE = 6 a 12 letras mayúsculas o números.
 */
@Documented
@Constraint(validatedBy = DocumentoValidoValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DocumentoValido {

    String message() default "El número de documento no tiene un formato válido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
