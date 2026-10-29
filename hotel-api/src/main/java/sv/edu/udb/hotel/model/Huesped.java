package sv.edu.udb.hotel.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sv.edu.udb.hotel.model.enums.TipoDocumento;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "huesped")
@Getter
@Setter
@NoArgsConstructor
public class Huesped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_huesped")
    private Long id;

    /** Cuenta de usuario vinculada (solo si el huésped se registró en el sistema). */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "nombre_completo", nullable = false, length = 100)
    private String nombreCompleto;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 10)
    private TipoDocumento tipoDocumento;

    @Column(name = "documento", nullable = false, unique = true, length = 20)
    private String documento;

    @Column(name = "telefono", nullable = false, length = 15)
    private String telefono;

    @Column(name = "email", nullable = false, length = 80)
    private String email;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }
}
