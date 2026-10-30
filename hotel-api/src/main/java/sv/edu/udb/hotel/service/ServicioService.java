package sv.edu.udb.hotel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.hotel.dto.request.ServicioRequest;
import sv.edu.udb.hotel.dto.response.ServicioResponse;
import sv.edu.udb.hotel.exception.RecursoNoEncontradoException;
import sv.edu.udb.hotel.exception.ReglaNegocioException;
import sv.edu.udb.hotel.model.Servicio;
import sv.edu.udb.hotel.repository.ReservaServicioRepository;
import sv.edu.udb.hotel.repository.ServicioRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final ReservaServicioRepository reservaServicioRepository;

    @Transactional(readOnly = true)
    public List<ServicioResponse> listar(Boolean adicional) {
        List<Servicio> servicios = adicional == null
                ? servicioRepository.findAllByOrderByNombreAsc()
                : servicioRepository.findByAdicionalOrderByNombreAsc(adicional);
        return servicios.stream().map(ServicioResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ServicioResponse obtener(Long id) {
        return ServicioResponse.from(buscar(id));
    }

    @Transactional
    public ServicioResponse crear(ServicioRequest request) {
        if (servicioRepository.existsByNombreIgnoreCase(request.nombre().trim())) {
            throw ReglaNegocioException.conflicto("Ya existe el servicio \"" + request.nombre().trim() + "\".");
        }
        Servicio servicio = new Servicio();
        copiarDatos(request, servicio);
        return ServicioResponse.from(servicioRepository.save(servicio));
    }

    @Transactional
    public ServicioResponse actualizar(Long id, ServicioRequest request) {
        Servicio servicio = buscar(id);
        if (servicioRepository.existsByNombreIgnoreCaseAndIdNot(request.nombre().trim(), id)) {
            throw ReglaNegocioException.conflicto("Ya existe otro servicio llamado \"" + request.nombre().trim() + "\".");
        }
        copiarDatos(request, servicio);
        return ServicioResponse.from(servicio);
    }

    @Transactional
    public void eliminar(Long id) {
        Servicio servicio = buscar(id);
        if (reservaServicioRepository.existsByServicioId(id)) {
            throw ReglaNegocioException.conflicto("No se puede eliminar el servicio porque ya fue contratado en reservas.");
        }
        servicioRepository.delete(servicio);
    }

    private void copiarDatos(ServicioRequest request, Servicio servicio) {
        if (request.adicional() && request.costo().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("Un servicio adicional debe tener un costo mayor a $0.00.");
        }
        servicio.setNombre(request.nombre().trim());
        servicio.setDescripcion(request.descripcion() == null ? null : request.descripcion().trim());
        servicio.setCosto(request.costo());
        servicio.setAdicional(request.adicional());
    }

    private Servicio buscar(Long id) {
        return servicioRepository.findById(id).orElseThrow(() -> RecursoNoEncontradoException.de("el servicio", id));
    }
}
