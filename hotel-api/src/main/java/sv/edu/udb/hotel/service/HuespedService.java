package sv.edu.udb.hotel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.hotel.dto.request.HuespedRequest;
import sv.edu.udb.hotel.dto.response.HuespedResponse;
import sv.edu.udb.hotel.exception.RecursoNoEncontradoException;
import sv.edu.udb.hotel.exception.ReglaNegocioException;
import sv.edu.udb.hotel.model.Huesped;
import sv.edu.udb.hotel.model.Usuario;
import sv.edu.udb.hotel.repository.HuespedRepository;
import sv.edu.udb.hotel.repository.ReclamoRepository;
import sv.edu.udb.hotel.repository.ReservaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HuespedService {

    private final HuespedRepository huespedRepository;
    private final ReservaRepository reservaRepository;
    private final ReclamoRepository reclamoRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<HuespedResponse> listar(String texto) {
        String filtro = texto == null ? "" : texto.trim();
        return huespedRepository.buscar(filtro).stream().map(HuespedResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public HuespedResponse obtener(Long id) {
        return HuespedResponse.from(buscar(id));
    }

    @Transactional
    public HuespedResponse crear(HuespedRequest request) {
        String documento = normalizarDocumento(request.documento());
        if (huespedRepository.existsByDocumentoIgnoreCase(documento)) {
            throw ReglaNegocioException.conflicto("Ya existe un huésped registrado con el documento " + documento + ".");
        }
        Huesped huesped = new Huesped();
        copiarDatos(request, huesped);
        return HuespedResponse.from(huespedRepository.save(huesped));
    }

    @Transactional
    public HuespedResponse actualizar(Long id, HuespedRequest request) {
        Huesped huesped = buscar(id);
        validarDocumentoLibre(request.documento(), id);
        copiarDatos(request, huesped);
        return HuespedResponse.from(huesped);
    }

    @Transactional
    public void eliminar(Long id) {
        Huesped huesped = buscar(id);
        if (reservaRepository.existsByHuespedId(id) || reclamoRepository.existsByHuespedId(id)) {
            throw ReglaNegocioException.conflicto("No se puede eliminar el huésped porque tiene reservas o reclamos registrados.");
        }
        huespedRepository.delete(huesped);
    }

    /** Perfil de huésped del usuario que inició sesión (rol HUESPED). */
    @Transactional(readOnly = true)
    public HuespedResponse miPerfil() {
        Usuario usuario = authService.getUsuarioActual();
        return huespedRepository.findByUsuarioId(usuario.getId())
                .map(HuespedResponse::from)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Aún no has completado tus datos de huésped. Se guardarán al hacer tu primera reserva."));
    }

    @Transactional
    public HuespedResponse guardarMiPerfil(HuespedRequest request) {
        Usuario usuario = authService.getUsuarioActual();
        return HuespedResponse.from(guardarPerfilDeUsuario(usuario, request));
    }

    /**
     * Obtiene el huésped para una reserva:
     * - Si quien reserva es HUESPED, se crea o actualiza su propio perfil.
     * - Si es personal del hotel, se busca por documento; si no existe, se registra.
     */
    @Transactional
    public Huesped resolverHuespedParaReserva(Usuario usuario, HuespedRequest request) {
        if (!AuthService.esPersonal(usuario)) {
            return guardarPerfilDeUsuario(usuario, request);
        }
        String documento = normalizarDocumento(request.documento());
        Huesped huesped = huespedRepository.findByDocumentoIgnoreCase(documento).orElseGet(Huesped::new);
        copiarDatos(request, huesped);
        return huespedRepository.save(huesped);
    }

    private Huesped guardarPerfilDeUsuario(Usuario usuario, HuespedRequest request) {
        Huesped huesped = huespedRepository.findByUsuarioId(usuario.getId()).orElseGet(() -> {
            Huesped nuevo = new Huesped();
            nuevo.setUsuario(usuario);
            return nuevo;
        });
        validarDocumentoLibre(request.documento(), huesped.getId());
        copiarDatos(request, huesped);
        return huespedRepository.save(huesped);
    }

    private void validarDocumentoLibre(String documento, Long idActual) {
        String doc = normalizarDocumento(documento);
        boolean ocupado = idActual == null
                ? huespedRepository.existsByDocumentoIgnoreCase(doc)
                : huespedRepository.existsByDocumentoIgnoreCaseAndIdNot(doc, idActual);
        if (ocupado) {
            throw ReglaNegocioException.conflicto("El documento " + doc + " ya está registrado para otro huésped.");
        }
    }

    private void copiarDatos(HuespedRequest request, Huesped huesped) {
        huesped.setNombreCompleto(request.nombreCompleto().trim());
        huesped.setFechaNacimiento(request.fechaNacimiento());
        huesped.setTipoDocumento(request.tipoDocumento());
        huesped.setDocumento(normalizarDocumento(request.documento()));
        huesped.setTelefono(request.telefono().trim());
        huesped.setEmail(request.email().trim().toLowerCase());
    }

    private String normalizarDocumento(String documento) {
        return documento.trim().toUpperCase();
    }

    private Huesped buscar(Long id) {
        return huespedRepository.findById(id).orElseThrow(() -> RecursoNoEncontradoException.de("el huésped", id));
    }
}
