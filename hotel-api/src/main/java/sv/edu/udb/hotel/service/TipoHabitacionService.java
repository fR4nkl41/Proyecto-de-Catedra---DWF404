package sv.edu.udb.hotel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.hotel.dto.request.TipoHabitacionRequest;
import sv.edu.udb.hotel.dto.response.TipoHabitacionResponse;
import sv.edu.udb.hotel.exception.RecursoNoEncontradoException;
import sv.edu.udb.hotel.exception.ReglaNegocioException;
import sv.edu.udb.hotel.model.TipoHabitacion;
import sv.edu.udb.hotel.repository.HabitacionRepository;
import sv.edu.udb.hotel.repository.TipoHabitacionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoHabitacionService {

    private final TipoHabitacionRepository tipoRepository;
    private final HabitacionRepository habitacionRepository;

    @Transactional(readOnly = true)
    public List<TipoHabitacionResponse> listar() {
        return tipoRepository.findAllByOrderByPrecioNocheAsc().stream().map(TipoHabitacionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TipoHabitacionResponse obtener(Long id) {
        return TipoHabitacionResponse.from(buscar(id));
    }

    @Transactional
    public TipoHabitacionResponse crear(TipoHabitacionRequest request) {
        if (tipoRepository.existsByNombreIgnoreCase(request.nombre().trim())) {
            throw ReglaNegocioException.conflicto("Ya existe el tipo de habitación \"" + request.nombre().trim() + "\".");
        }
        TipoHabitacion tipo = new TipoHabitacion();
        copiarDatos(request, tipo);
        return TipoHabitacionResponse.from(tipoRepository.save(tipo));
    }

    @Transactional
    public TipoHabitacionResponse actualizar(Long id, TipoHabitacionRequest request) {
        TipoHabitacion tipo = buscar(id);
        if (tipoRepository.existsByNombreIgnoreCaseAndIdNot(request.nombre().trim(), id)) {
            throw ReglaNegocioException.conflicto("Ya existe otro tipo de habitación llamado \"" + request.nombre().trim() + "\".");
        }
        copiarDatos(request, tipo);
        return TipoHabitacionResponse.from(tipo);
    }

    @Transactional
    public void eliminar(Long id) {
        TipoHabitacion tipo = buscar(id);
        long habitaciones = habitacionRepository.countByTipoId(id);
        if (habitaciones > 0) {
            throw ReglaNegocioException.conflicto("No se puede eliminar el tipo porque " + habitaciones
                    + " habitación(es) lo utilizan.");
        }
        tipoRepository.delete(tipo);
    }

    private void copiarDatos(TipoHabitacionRequest request, TipoHabitacion tipo) {
        tipo.setNombre(request.nombre().trim());
        tipo.setDescripcion(request.descripcion() == null ? null : request.descripcion().trim());
        tipo.setCapacidadMaxima(request.capacidadMaxima());
        tipo.setPrecioNoche(request.precioNoche());
    }

    private TipoHabitacion buscar(Long id) {
        return tipoRepository.findById(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("el tipo de habitación", id));
    }
}
