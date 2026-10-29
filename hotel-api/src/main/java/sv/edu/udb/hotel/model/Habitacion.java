package sv.edu.udb.hotel.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sv.edu.udb.hotel.model.enums.EstadoHabitacion;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "habitacion")
@Getter
@Setter
@NoArgsConstructor
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_habitacion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_sucursal", nullable = false)
    private Sucursal sucursal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo", nullable = false)
    private TipoHabitacion tipo;

    @Column(name = "numero", nullable = false, length = 10)
    private String numero;

    @Column(name = "piso", nullable = false)
    private Integer piso;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoHabitacion estado = EstadoHabitacion.DISPONIBLE;

    /** Servicios incluidos en la habitación (tabla habitacion_servicio). */
    @ManyToMany
    @JoinTable(name = "habitacion_servicio",
            joinColumns = @JoinColumn(name = "id_habitacion"),
            inverseJoinColumns = @JoinColumn(name = "id_servicio"))
    private Set<Servicio> servicios = new HashSet<>();
}
