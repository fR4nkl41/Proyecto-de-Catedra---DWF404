package sv.edu.udb.hotel.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sv.edu.udb.hotel.model.enums.EstadoReserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reserva")
@Getter
@Setter
@NoArgsConstructor
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_huesped", nullable = false)
    private Huesped huesped;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_habitacion", nullable = false)
    private Habitacion habitacion;

    /** Usuario que registró la reserva (huésped o recepcionista). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario registradoPor;

    @Column(name = "fecha_entrada", nullable = false)
    private LocalDate fechaEntrada;

    @Column(name = "fecha_salida", nullable = false)
    private LocalDate fechaSalida;

    @Column(name = "numero_noches", nullable = false)
    private Integer numeroNoches;

    @Column(name = "numero_huespedes", nullable = false)
    private Integer numeroHuespedes;

    @Column(name = "subtotal_habitacion", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotalHabitacion;

    @Column(name = "subtotal_servicios", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotalServicios = BigDecimal.ZERO;

    @Column(name = "precio_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_reserva", nullable = false, length = 20)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    @Column(name = "solicitudes_especiales", length = 250)
    private String solicitudesEspeciales;

    @Column(name = "fecha_reserva", nullable = false)
    private LocalDateTime fechaReserva;

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservaServicio> servicios = new ArrayList<>();

    @OneToOne(mappedBy = "reserva", cascade = CascadeType.REMOVE)
    private Pago pago;

    @OneToOne(mappedBy = "reserva", cascade = CascadeType.REMOVE)
    private Cancelacion cancelacion;

    @PrePersist
    void prePersist() {
        if (fechaReserva == null) {
            fechaReserva = LocalDateTime.now();
        }
    }
}
