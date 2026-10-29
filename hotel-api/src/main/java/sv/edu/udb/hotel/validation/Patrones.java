package sv.edu.udb.hotel.validation;

/** Expresiones regulares reutilizadas en las validaciones de los DTO. */
public final class Patrones {

    /** Teléfono de El Salvador: 2222-3333 / 77778888, opcionalmente con +503. */
    public static final String TELEFONO = "^(\\+503 ?)?[267]\\d{3}-?\\d{4}$";

    /** Contraseña: al menos una letra y un número. */
    public static final String PASSWORD = "^(?=.*[A-Za-z])(?=.*\\d).+$";

    /** Número de habitación: 1 a 4 dígitos y una letra opcional (101, 12B). */
    public static final String NUMERO_HABITACION = "^\\d{1,4}[A-Za-z]?$";

    private Patrones() {
    }
}
