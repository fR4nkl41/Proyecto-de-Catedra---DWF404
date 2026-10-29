package sv.edu.udb.hotel.exception;

/** Se lanza cuando un registro solicitado no existe (HTTP 404). */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public static RecursoNoEncontradoException de(String recurso, Long id) {
        return new RecursoNoEncontradoException("No se encontró " + recurso + " con ID " + id + ".");
    }
}
