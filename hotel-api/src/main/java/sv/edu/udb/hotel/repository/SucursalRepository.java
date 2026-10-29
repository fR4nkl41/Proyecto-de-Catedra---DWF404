package sv.edu.udb.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.edu.udb.hotel.model.Sucursal;

import java.util.List;

public interface SucursalRepository extends JpaRepository<Sucursal, Long> {

    @Query("select s from Sucursal s join fetch s.hotel h order by h.nombre, s.nombre")
    List<Sucursal> listarTodas();

    @Query("select s from Sucursal s join fetch s.hotel h where h.id = :hotelId order by s.nombre")
    List<Sucursal> listarPorHotel(@Param("hotelId") Long hotelId);

    boolean existsByHotelIdAndNombreIgnoreCase(Long hotelId, String nombre);

    boolean existsByHotelIdAndNombreIgnoreCaseAndIdNot(Long hotelId, String nombre, Long id);

    long countByHotelId(Long hotelId);
}
