package sv.edu.udb.hotel.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import sv.edu.udb.hotel.dto.response.ErrorResponse;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejo centralizado de errores. Todas las respuestas de error tienen
 * el mismo formato (ErrorResponse) y mensajes claros en español.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Errores de validación de @Valid en el cuerpo JSON o en filtros de búsqueda. */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(BindException ex, HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            String mensaje = error.isBindingFailure()
                    ? "El valor ingresado no tiene el tipo de dato correcto"
                    : error.getDefaultMessage();
            errores.putIfAbsent(error.getField(), mensaje);
        }
        ex.getBindingResult().getGlobalErrors()
                .forEach(error -> errores.putIfAbsent("general", error.getDefaultMessage()));
        return construir(HttpStatus.BAD_REQUEST, "Datos inválidos",
                "Revisa los campos marcados: hay datos vacíos o con formato incorrecto.", request, errores);
    }

    /** Validaciones sobre parámetros (@RequestParam, @PathVariable). */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> manejarValidacionParametros(HandlerMethodValidationException ex,
                                                                     HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getParameterValidationResults().forEach(resultado -> {
            String nombre = resultado.getMethodParameter().getParameterName();
            resultado.getResolvableErrors()
                    .forEach(error -> errores.putIfAbsent(nombre != null ? nombre : "parametro", error.getDefaultMessage()));
        });
        return construir(HttpStatus.BAD_REQUEST, "Datos inválidos",
                "Algunos parámetros de la solicitud no son válidos.", request, errores);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> manejarConstraint(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> {
            String ruta = v.getPropertyPath().toString();
            String campo = ruta.contains(".") ? ruta.substring(ruta.lastIndexOf('.') + 1) : ruta;
            errores.putIfAbsent(campo, v.getMessage());
        });
        return construir(HttpStatus.BAD_REQUEST, "Datos inválidos",
                "Algunos datos no cumplen las reglas de validación.", request, errores);
    }

    /** JSON mal formado o tipo de dato incorrecto (ej. texto en un campo numérico). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarJsonInvalido(HttpMessageNotReadableException ex,
                                                             HttpServletRequest request) {
        Map<String, String> errores = null;
        String mensaje = "El formato de los datos enviados no es válido. Verifica el JSON de la solicitud.";

        if (ex.getCause() instanceof MismatchedInputException mie && !mie.getPath().isEmpty()) {
            String campo = mie.getPath().get(mie.getPath().size() - 1).getFieldName();
            String esperado = describirTipo(mie.getTargetType());
            String detalle = "Tipo de dato incorrecto. Se esperaba " + esperado;
            if (mie instanceof InvalidFormatException ife && ife.getTargetType() != null && ife.getTargetType().isEnum()) {
                detalle = "Valor no permitido. Opciones válidas: " + Arrays.stream(ife.getTargetType().getEnumConstants())
                        .map(Object::toString).collect(Collectors.joining(", "));
            }
            errores = Map.of(campo != null ? campo : "campo", detalle);
            mensaje = "El campo '" + campo + "' tiene un valor con formato incorrecto.";
        }
        return construir(HttpStatus.BAD_REQUEST, "Formato inválido", mensaje, request, errores);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> manejarTipoParametro(MethodArgumentTypeMismatchException ex,
                                                              HttpServletRequest request) {
        String esperado = describirTipo(ex.getRequiredType());
        return construir(HttpStatus.BAD_REQUEST, "Parámetro inválido",
                "El parámetro '" + ex.getName() + "' debe ser " + esperado + ".", request,
                Map.of(ex.getName(), "Se esperaba " + esperado));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> manejarParametroFaltante(MissingServletRequestParameterException ex,
                                                                  HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, "Parámetro requerido",
                "Falta el parámetro obligatorio '" + ex.getParameterName() + "'.", request,
                Map.of(ex.getParameterName(), "Este campo es obligatorio"));
    }

    @ExceptionHandler(ValidacionException.class)
    public ResponseEntity<ErrorResponse> manejarValidacionNegocio(ValidacionException ex, HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, "Datos inválidos", ex.getMessage(), request, ex.getErrores());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(RecursoNoEncontradoException ex, HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, "No encontrado", ex.getMessage(), request, null);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> manejarReglaNegocio(ReglaNegocioException ex, HttpServletRequest request) {
        String titulo = switch (ex.getEstado()) {
            case CONFLICT -> "Conflicto con datos existentes";
            case FORBIDDEN -> "Acceso denegado";
            default -> "Operación no permitida";
        };
        return construir(ex.getEstado(), titulo, ex.getMessage(), request, null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> manejarCredenciales(BadCredentialsException ex, HttpServletRequest request) {
        return construir(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas",
                "El correo o la contraseña son incorrectos. Verifica tus datos e intenta de nuevo.", request, null);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> manejarCuentaDesactivada(DisabledException ex, HttpServletRequest request) {
        return construir(HttpStatus.FORBIDDEN, "Cuenta desactivada",
                "Tu cuenta está desactivada. Contacta al administrador para reactivarla.", request, null);
    }

    /** Lanzada por @PreAuthorize cuando el rol no tiene permiso. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> manejarAccesoDenegado(AccessDeniedException ex, HttpServletRequest request) {
        return construir(HttpStatus.FORBIDDEN, "Acceso denegado",
                "No tienes permisos para realizar esta acción con tu rol actual.", request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> manejarIntegridad(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violación de integridad: {}", ex.getMostSpecificCause().getMessage());
        return construir(HttpStatus.CONFLICT, "Conflicto con datos existentes",
                "No se pudo completar la operación porque el registro está relacionado con otros datos o ya existe.",
                request, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> manejarRutaInexistente(NoResourceFoundException ex, HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, "Ruta no encontrada",
                "La ruta solicitada no existe. Consulta la documentación en /swagger-ui.html", request, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> manejarMetodo(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return construir(HttpStatus.METHOD_NOT_ALLOWED, "Método no permitido",
                "El método " + ex.getMethod() + " no está permitido en esta ruta.", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarGeneral(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado en {}", request.getRequestURI(), ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrió un error inesperado. Intenta de nuevo y, si persiste, contacta al administrador.",
                request, null);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus estado, String error, String mensaje,
                                                    HttpServletRequest request, Map<String, String> errores) {
        ErrorResponse cuerpo = new ErrorResponse(LocalDateTime.now(), estado.value(), error, mensaje,
                request.getRequestURI(), errores);
        return ResponseEntity.status(estado).body(cuerpo);
    }

    private String describirTipo(Class<?> tipo) {
        if (tipo == null) {
            return "un valor válido";
        }
        if (Number.class.isAssignableFrom(tipo) || tipo == int.class || tipo == long.class) {
            return "un número";
        }
        if (tipo == java.time.LocalDate.class) {
            return "una fecha con formato AAAA-MM-DD";
        }
        if (tipo == Boolean.class || tipo == boolean.class) {
            return "true o false";
        }
        if (tipo.isEnum()) {
            return "uno de: " + Arrays.stream(tipo.getEnumConstants()).map(Object::toString).collect(Collectors.joining(", "));
        }
        return "un valor de tipo " + tipo.getSimpleName();
    }
}
