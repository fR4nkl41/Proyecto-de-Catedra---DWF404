package sv.edu.udb.hotel;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sv.edu.udb.hotel.dto.request.HuespedRequest;
import sv.edu.udb.hotel.dto.request.ReservaRequest;
import sv.edu.udb.hotel.model.enums.TipoDocumento;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas unitarias de las validaciones del lado del servidor (sin base de datos). */
class ValidacionesTest {

    private static Validator validator;

    @BeforeAll
    static void iniciar() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private HuespedRequest huespedValido() {
        return new HuespedRequest("Carlos Pérez", LocalDate.of(1990, 5, 15), TipoDocumento.DUI,
                "01234567-8", "7777-8888", "carlos@mail.com");
    }

    @Test
    void huespedValidoNoTieneErrores() {
        assertTrue(validator.validate(huespedValido()).isEmpty());
    }

    @Test
    void duiConFormatoIncorrectoEsRechazado() {
        HuespedRequest h = new HuespedRequest("Carlos Pérez", LocalDate.of(1990, 5, 15), TipoDocumento.DUI,
                "12345678", "7777-8888", "carlos@mail.com");
        Set<ConstraintViolation<HuespedRequest>> errores = validator.validate(h);
        assertTrue(errores.stream().anyMatch(e -> e.getPropertyPath().toString().equals("documento")));
    }

    @Test
    void menorDeEdadEsRechazado() {
        HuespedRequest h = new HuespedRequest("Carlos Pérez", LocalDate.now().minusYears(15), TipoDocumento.DUI,
                "01234567-8", "7777-8888", "carlos@mail.com");
        assertFalse(validator.validate(h).isEmpty());
    }

    @Test
    void nombreConNumerosEsRechazado() {
        HuespedRequest h = new HuespedRequest("Carlos 123", LocalDate.of(1990, 5, 15), TipoDocumento.DUI,
                "01234567-8", "7777-8888", "carlos@mail.com");
        assertFalse(validator.validate(h).isEmpty());
    }

    @Test
    void fechaDeEntradaPasadaEsRechazada() {
        ReservaRequest r = new ReservaRequest(1L, LocalDate.now().minusDays(1), LocalDate.now().plusDays(2), 2,
                huespedValido(), List.of(), null);
        assertTrue(validator.validate(r).stream().anyMatch(e -> e.getPropertyPath().toString().equals("fechaEntrada")));
    }

    @Test
    void salidaAntesDeEntradaEsRechazada() {
        ReservaRequest r = new ReservaRequest(1L, LocalDate.now().plusDays(5), LocalDate.now().plusDays(3), 2,
                huespedValido(), List.of(), null);
        assertTrue(validator.validate(r).stream().anyMatch(e -> e.getPropertyPath().toString().equals("fechaSalida")));
    }

    @Test
    void reservaValidaNoTieneErrores() {
        ReservaRequest r = new ReservaRequest(1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(4), 2,
                huespedValido(), List.of(), "Cuna para bebé");
        assertTrue(validator.validate(r).isEmpty());
    }
}
