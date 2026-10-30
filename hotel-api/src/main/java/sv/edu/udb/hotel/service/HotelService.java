package sv.edu.udb.hotel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.hotel.dto.request.HotelRequest;
import sv.edu.udb.hotel.dto.response.HotelResponse;
import sv.edu.udb.hotel.exception.RecursoNoEncontradoException;
import sv.edu.udb.hotel.exception.ReglaNegocioException;
import sv.edu.udb.hotel.model.Hotel;
import sv.edu.udb.hotel.repository.HotelRepository;
import sv.edu.udb.hotel.repository.SucursalRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;
    private final SucursalRepository sucursalRepository;

    @Transactional(readOnly = true)
    public List<HotelResponse> listar() {
        return hotelRepository.findAllByOrderByNombreAsc().stream().map(HotelResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public HotelResponse obtener(Long id) {
        return HotelResponse.from(buscar(id));
    }

    @Transactional
    public HotelResponse crear(HotelRequest request) {
        if (hotelRepository.existsByNombreIgnoreCase(request.nombre().trim())) {
            throw ReglaNegocioException.conflicto("Ya existe un hotel con el nombre \"" + request.nombre().trim() + "\".");
        }
        Hotel hotel = new Hotel();
        copiarDatos(request, hotel);
        return HotelResponse.from(hotelRepository.save(hotel));
    }

    @Transactional
    public HotelResponse actualizar(Long id, HotelRequest request) {
        Hotel hotel = buscar(id);
        if (hotelRepository.existsByNombreIgnoreCaseAndIdNot(request.nombre().trim(), id)) {
            throw ReglaNegocioException.conflicto("Ya existe otro hotel con el nombre \"" + request.nombre().trim() + "\".");
        }
        copiarDatos(request, hotel);
        return HotelResponse.from(hotel);
    }

    @Transactional
    public void eliminar(Long id) {
        Hotel hotel = buscar(id);
        long sucursales = sucursalRepository.countByHotelId(id);
        if (sucursales > 0) {
            throw ReglaNegocioException.conflicto("No se puede eliminar el hotel porque tiene " + sucursales
                    + " sucursal(es) registradas. Elimínalas primero.");
        }
        hotelRepository.delete(hotel);
    }

    private void copiarDatos(HotelRequest request, Hotel hotel) {
        hotel.setNombre(request.nombre().trim());
        hotel.setDireccion(request.direccion().trim());
        hotel.setCiudad(request.ciudad().trim());
        hotel.setTelefono(request.telefono().trim());
        hotel.setEmail(request.email().trim().toLowerCase());
        hotel.setDescripcion(request.descripcion() == null ? null : request.descripcion().trim());
    }

    private Hotel buscar(Long id) {
        return hotelRepository.findById(id).orElseThrow(() -> RecursoNoEncontradoException.de("el hotel", id));
    }
}
