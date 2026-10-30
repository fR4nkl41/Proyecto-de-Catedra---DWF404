package sv.edu.udb.hotel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.hotel.dto.request.ReclamoRequest;
import sv.edu.udb.hotel.dto.request.RespuestaReclamoRequest;
import sv.edu.udb.hotel.dto.response.ReclamoResponse;
import sv.edu.udb.hotel.exception.RecursoNoEncontradoException;
import sv.edu.udb.hotel.exception.ReglaNegocioException;
import sv.edu.udb.hotel.model.Huesped;
import sv.edu.udb.hotel.model.Reclamo;
import sv.edu.udb.hotel.model.Reserva;
import sv.edu.udb.hotel.model.Usuario;
import sv.edu.udb.hotel.model.enums.EstadoReclamo;
import sv.edu.udb.hotel.repository.HuespedRepository;
import sv.edu.udb.hotel.repository.ReclamoRepository;
import sv.edu.udb.hotel.repository.ReservaRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReclamoService {

    private final ReclamoRepository reclamoRepository;
    private final HuespedRepository huespedRepository;
    private final ReservaRepository reservaRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<ReclamoResponse> listar(EstadoReclamo estado) {
        Usuario usuario = authService.getUsuarioActual();
        List<Reclamo> reclamos = AuthService.esPersonal(usuario)
                ? reclamoRepository.listar(estado)
                : reclamoRepository.listarPorUsuarioHuesped(usuario.getId());
        return reclamos.stream().map(ReclamoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ReclamoResponse obtener(Long id) {
        Reclamo reclamo = buscar(id);
        validarAcceso(reclamo, authService.getUsuarioActual());
        return ReclamoResponse.from(reclamo);
    }

    @Transactional
    public ReclamoResponse crear(ReclamoRequest request) {
        Usuario usuario = authService.getUsuarioActual();
        Huesped huesped;
        if (AuthService.esPersonal(usuario)) {
            if (request.huespedId() == null) {
                throw new ReglaNegocioException("Selecciona el huésped que presenta el reclamo.");
            }
            huesped = huespedRepository.findById(request.huespedId())
                    .orElseThrow(() -> RecursoNoEncontradoException.de("el huésped", request.huespedId()));
        } else {
            huesped = huespedRepository.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new ReglaNegocioException(
                            "Primero completa tus datos de huésped realizando una reserva."));
        }
        Reclamo reclamo = new Reclamo();
        reclamo.setHuesped(huesped);
        reclamo.setEstado(EstadoReclamo.ABIERTO);
        copiarDatos(request, reclamo, huesped);
        return ReclamoResponse.from(reclamoRepository.save(reclamo));
    }

    /** El huésped puede editar su reclamo mientras siga ABIERTO. */
    @Transactional
    public ReclamoResponse actualizar(Long id, ReclamoRequest request) {
        Reclamo reclamo = buscar(id);
        validarAcceso(reclamo, authService.getUsuarioActual());
        if (reclamo.getEstado() != EstadoReclamo.ABIERTO) {
            throw new ReglaNegocioException("Solo se pueden editar reclamos ABIERTOS.");
        }
        copiarDatos(request, reclamo, reclamo.getHuesped());
        return ReclamoResponse.from(reclamo);
    }

    @Transactional
    public ReclamoResponse responder(Long id, RespuestaReclamoRequest request) {
        Reclamo reclamo = buscar(id);
        reclamo.setEstado(request.estado());
        reclamo.setRespuesta(request.respuesta().trim());
        reclamo.setFechaRespuesta(LocalDateTime.now());
        return ReclamoResponse.from(reclamo);
    }

    @Transactional
    public void eliminar(Long id) {
        reclamoRepository.delete(buscar(id));
    }

    private void copiarDatos(ReclamoRequest request, Reclamo reclamo, Huesped huesped) {
        Reserva reserva = null;
        if (request.reservaId() != null) {
            reserva = reservaRepository.findById(request.reservaId())
                    .orElseThrow(() -> RecursoNoEncontradoException.de("la reserva", request.reservaId()));
            if (!reserva.getHuesped().getId().equals(huesped.getId())) {
                throw new ReglaNegocioException("La reserva " + reserva.getCodigo() + " no pertenece a este huésped.");
            }
        }
        reclamo.setReserva(reserva);
        reclamo.setAsunto(request.asunto().trim());
        reclamo.setDescripcion(request.descripcion().trim());
    }

    private void validarAcceso(Reclamo reclamo, Usuario usuario) {
        if (AuthService.esPersonal(usuario)) {
            return;
        }
        Usuario dueno = reclamo.getHuesped().getUsuario();
        if (dueno == null || !dueno.getId().equals(usuario.getId())) {
            throw ReglaNegocioException.prohibido("No tienes acceso a este reclamo.");
        }
    }

    private Reclamo buscar(Long id) {
        return reclamoRepository.findById(id).orElseThrow(() -> RecursoNoEncontradoException.de("el reclamo", id));
    }
}
