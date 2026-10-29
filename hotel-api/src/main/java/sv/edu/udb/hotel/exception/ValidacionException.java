package sv.edu.udb.hotel.exception;

import lombok.Getter;

import java.util.Map;

/** Errores de validación detectados en la lógica de negocio, indicados por campo (HTTP 400). */
@Getter
public class ValidacionException extends RuntimeException {

    private final Map<String, String> errores;

    public ValidacionException(String mensaje, Map<String, String> errores) {
        super(mensaje);
        this.errores = errores;
    }
}
