package sv.edu.udb.hotel.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Se lanza cuando una operación no cumple una regla del negocio hotelero. */
@Getter
public class ReglaNegocioException extends RuntimeException {

    private final HttpStatus estado;

    public ReglaNegocioException(String mensaje) {
        this(mensaje, HttpStatus.BAD_REQUEST);
    }

    public ReglaNegocioException(String mensaje, HttpStatus estado) {
        super(mensaje);
        this.estado = estado;
    }

    /** Conflicto con datos existentes (HTTP 409), por ejemplo un registro duplicado. */
    public static ReglaNegocioException conflicto(String mensaje) {
        return new ReglaNegocioException(mensaje, HttpStatus.CONFLICT);
    }

    /** El usuario no tiene permiso sobre ese registro (HTTP 403). */
    public static ReglaNegocioException prohibido(String mensaje) {
        return new ReglaNegocioException(mensaje, HttpStatus.FORBIDDEN);
    }
}
