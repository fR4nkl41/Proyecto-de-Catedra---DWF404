package sv.edu.udb.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.edu.udb.hotel.model.Huesped;

import java.util.List;
import java.util.Optional;

public interface HuespedRepository extends JpaRepository<Huesped, Long> {

    Optional<Huesped> findByUsuarioId(Long usuarioId);

    Optional<Huesped> findByDocumentoIgnoreCase(String documento);

    boolean existsByDocumentoIgnoreCaseAndIdNot(String documento, Long id);

    boolean existsByDocumentoIgnoreCase(String documento);

    @Query("""
            select h from Huesped h
            where lower(h.nombreCompleto) like lower(concat('%', :texto, '%'))
               or lower(h.documento) like lower(concat('%', :texto, '%'))
               or lower(h.email) like lower(concat('%', :texto, '%'))
            order by h.nombreCompleto
            """)
    List<Huesped> buscar(@Param("texto") String texto);
}
