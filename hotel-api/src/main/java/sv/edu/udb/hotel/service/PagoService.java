package sv.edu.udb.hotel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.hotel.dto.request.PagoRequest;
import sv.edu.udb.hotel.dto.response.PagoResponse;
import sv.edu.udb.hotel.exception.RecursoNoEncontradoException;
import sv.edu.udb.hotel.exception.ReglaNegocioException;
import sv.edu.udb.hotel.exception.ValidacionException;
import sv.edu.udb.hotel.model.Pago;
import sv.edu.udb.hotel.model.Reserva;
import sv.edu.udb.hotel.model.Usuario;
import sv.edu.udb.hotel.model.enums.EstadoPago;
import sv.edu.udb.hotel.model.enums.EstadoReserva;
import sv.edu.udb.hotel.repository.PagoRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pagos simulados (no hay pasarela real). Al registrar el pago la reserva
 * pasa de PENDIENTE a CONFIRMADA.
 */
@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final ReservaService reservaService;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<PagoResponse> listar() {
        return pagoRepository.listar().stream().map(PagoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PagoResponse obtenerPorReserva(Long reservaId) {
        Reserva reserva = reservaService.buscarDetalle(reservaId);
        reservaService.validarAcceso(reserva, authService.getUsuarioActual());
        Pago pago = pagoRepository.findByReservaId(reservaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La reserva " + reserva.getCodigo()
                        + " aún no tiene pagos registrados."));
        return PagoResponse.from(pago);
    }

    @Transactional
    public PagoResponse pagar(PagoRequest request) {
        Usuario usuario = authService.getUsuarioActual();
        Reserva reserva = reservaService.buscarDetalle(request.reservaId());
        reservaService.validarAcceso(reserva, usuario);

        if (reserva.getPago() != null) {
            throw ReglaNegocioException.conflicto("La reserva " + reserva.getCodigo() + " ya tiene un pago registrado.");
        }
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ReglaNegocioException("Solo se pueden pagar reservas PENDIENTES. Esta reserva está "
                    + reserva.getEstado() + ".");
        }
        if (reserva.getFechaEntrada().isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("La fecha de entrada de la reserva ya pasó. Modifica las fechas antes de pagar.");
        }

        Pago pago = new Pago();
        pago.setReserva(reserva);
        pago.setMetodoPago(request.metodoPago());
        pago.setMonto(reserva.getPrecioTotal());   // el monto lo define el servidor
        pago.setEstado(EstadoPago.COMPLETADO);
        pago.setReferencia(validarDatosSegunMetodo(request, usuario));
        pagoRepository.save(pago);

        reserva.setPago(pago);
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        return PagoResponse.from(pago);
    }

    /** Valida los datos obligatorios de cada método y devuelve la referencia que se guardará. */
    private String validarDatosSegunMetodo(PagoRequest request, Usuario usuario) {
        Map<String, String> errores = new LinkedHashMap<>();
        String referencia;

        switch (request.metodoPago()) {
            case TARJETA -> {
                if (vacio(request.numeroTarjeta())) {
                    errores.put("numeroTarjeta", "El número de tarjeta es obligatorio");
                } else if (!esLuhnValido(request.numeroTarjeta())) {
                    errores.put("numeroTarjeta", "El número de tarjeta no es válido");
                }
                if (vacio(request.titular())) {
                    errores.put("titular", "El nombre del titular es obligatorio");
                }
                if (vacio(request.vencimiento())) {
                    errores.put("vencimiento", "La fecha de vencimiento es obligatoria");
                } else if (estaVencida(request.vencimiento())) {
                    errores.put("vencimiento", "La tarjeta está vencida");
                }
                if (vacio(request.cvv())) {
                    errores.put("cvv", "El CVV es obligatorio");
                }
                referencia = vacio(request.numeroTarjeta()) ? null
                        : "**** " + request.numeroTarjeta().substring(request.numeroTarjeta().length() - 4);
            }
            case TRANSFERENCIA -> {
                if (vacio(request.referenciaTransferencia())) {
                    errores.put("referenciaTransferencia", "El número de referencia de la transferencia es obligatorio");
                }
                referencia = vacio(request.referenciaTransferencia()) ? null
                        : request.referenciaTransferencia().trim().toUpperCase();
            }
            case EFECTIVO -> {
                if (!AuthService.esPersonal(usuario)) {
                    throw new ReglaNegocioException("El pago en efectivo se registra en la recepción del hotel. "
                            + "Tu reserva queda PENDIENTE hasta que pagues al llegar.");
                }
                String texto = "Recepción - " + usuario.getNombre();
                referencia = texto.length() > 40 ? texto.substring(0, 40) : texto;
            }
            default -> throw new ReglaNegocioException("Método de pago no soportado.");
        }

        if (!errores.isEmpty()) {
            throw new ValidacionException("Completa los datos requeridos para el pago con "
                    + request.metodoPago().name().toLowerCase() + ".", errores);
        }
        return referencia;
    }

    private boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private boolean estaVencida(String vencimiento) {
        String[] partes = vencimiento.split("/");
        YearMonth fecha = YearMonth.of(2000 + Integer.parseInt(partes[1]), Integer.parseInt(partes[0]));
        return fecha.isBefore(YearMonth.now());
    }

    /** Algoritmo de Luhn: verifica que el número de tarjeta sea válido. */
    private boolean esLuhnValido(String numero) {
        int suma = 0;
        boolean duplicar = false;
        for (int i = numero.length() - 1; i >= 0; i--) {
            int digito = numero.charAt(i) - '0';
            if (duplicar) {
                digito *= 2;
                if (digito > 9) {
                    digito -= 9;
                }
            }
            suma += digito;
            duplicar = !duplicar;
        }
        return suma % 10 == 0;
    }
}
