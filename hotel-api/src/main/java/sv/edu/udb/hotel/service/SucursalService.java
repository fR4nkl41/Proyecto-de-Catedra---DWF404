package sv.edu.udb.hotel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.hotel.dto.request.SucursalRequest;
import sv.edu.udb.hotel.dto.response.SucursalResponse;
import sv.edu.udb.hotel.exception.RecursoNoEncontradoException;
import sv.edu.udb.hotel.exception.ReglaNegocioException;
import sv.edu.udb.hotel.model.Hotel;
import sv.edu.udb.hotel.model.Sucursal;
import sv.edu.udb.hotel.repository.HabitacionRepository;
import sv.edu.udb.hotel.repository.HotelRepository;
import sv.edu.udb.hotel.repository.SucursalRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SucursalService {

    private final SucursalRepository sucursalRepository;
    private final HotelRepository hotelRepository;
    private final HabitacionRepository habitacionRepository;

    @Transactional(readOnly = true)
    public List<SucursalResponse> listar(Long hotelId) {
        List<Sucursal> sucursales = hotelId == null
                ? sucursalRepository.listarTodas()
                : sucursalRepository.listarPorHotel(hotelId);
        return sucursales.stream().map(SucursalResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public SucursalResponse obtener(Long id) {
        return SucursalResponse.from(buscar(id));
    }

    @Transactional
    public SucursalResponse crear(SucursalRequest request) {
        Hotel hotel = buscarHotel(request.hotelId());
        if (sucursalRepository.existsByHotelIdAndNombreIgnoreCase(hotel.getId(), request.nombre().trim())) {
            throw ReglaNegocioException.conflicto("El hotel ya tiene una sucursal llamada \"" + request.nombre().trim() + "\".");
        }
        Sucursal sucursal = new Sucursal();
        copiarDatos(request, sucursal, hotel);
        return SucursalResponse.from(sucursalRepository.save(sucursal));
    }

    @Transactional
    public SucursalResponse actualizar(Long id, SucursalRequest request) {
        Sucursal sucursal = buscar(id);
        Hotel hotel = buscarHotel(request.hotelId());
        if (sucursalRepository.existsByHotelIdAndNombreIgnoreCaseAndIdNot(hotel.getId(), request.nombre().trim(), id)) {
            throw ReglaNegocioException.conflicto("El hotel ya tiene otra sucursal llamada \"" + request.nombre().trim() + "\".");
        }
        copiarDatos(request, sucursal, hotel);
        return SucursalResponse.from(sucursal);
    }

    @Transactional
    public void eliminar(Long id) {
        Sucursal sucursal = buscar(id);
        long habitaciones = habitacionRepository.countBySucursalId(id);
        if (habitaciones > 0) {
            throw ReglaNegocioException.conflicto("No se puede eliminar la sucursal porque tiene " + habitaciones
                    + " habitación(es) registradas.");
        }
        sucursalRepository.delete(sucursal);
    }

    private void copiarDatos(SucursalRequest request, Sucursal sucursal, Hotel hotel) {
        sucursal.setHotel(hotel);
        sucursal.setNombre(request.nombre().trim());
        sucursal.setDireccion(request.direccion().trim());
        sucursal.setCiudad(request.ciudad().trim());
        sucursal.setTelefono(request.telefono().trim());
    }

    private Hotel buscarHotel(Long id) {
        return hotelRepository.findById(id).orElseThrow(() -> RecursoNoEncontradoException.de("el hotel", id));
    }

    private Sucursal buscar(Long id) {
        return sucursalRepository.findById(id).orElseThrow(() -> RecursoNoEncontradoException.de("la sucursal", id));
    }
}
