package sv.edu.udb.hotel.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import sv.edu.udb.hotel.dto.request.HuespedRequest;
import sv.edu.udb.hotel.model.enums.TipoDocumento;

public class DocumentoValidoValidator implements ConstraintValidator<DocumentoValido, HuespedRequest> {

    private static final String REGEX_DUI = "^\\d{8}-\\d$";
    private static final String REGEX_PASAPORTE = "^[A-Z0-9]{6,12}$";

    @Override
    public boolean isValid(HuespedRequest dto, ConstraintValidatorContext context) {
        if (dto == null || dto.tipoDocumento() == null || dto.documento() == null || dto.documento().isBlank()) {
            return true;
        }
        String documento = dto.documento().trim();
        boolean dui = dto.tipoDocumento() == TipoDocumento.DUI;
        boolean valido = dui ? documento.matches(REGEX_DUI) : documento.matches(REGEX_PASAPORTE);
        if (valido) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(dui
                        ? "El DUI debe tener el formato 00000000-0"
                        : "El pasaporte debe tener de 6 a 12 letras mayúsculas o números")
                .addPropertyNode("documento")
                .addConstraintViolation();
        return false;
    }
}
