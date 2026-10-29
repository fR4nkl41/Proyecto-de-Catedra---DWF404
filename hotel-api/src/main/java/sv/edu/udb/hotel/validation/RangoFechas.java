package sv.edu.udb.hotel.validation;

/** Lo implementan los DTO que tienen fecha de entrada y fecha de salida. */
public interface RangoFechas {

    java.time.LocalDate fechaEntrada();

    java.time.LocalDate fechaSalida();
}
