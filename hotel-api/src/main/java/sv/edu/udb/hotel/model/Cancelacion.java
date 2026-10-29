package sv.edu.udb.hotel.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cancelacion")
@Getter
@Setter
@NoArgsConstructor
public class Cancelacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cancelacion")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_reserva", nullable = false, unique = true)
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario canceladoPor;

    @Column(name = "fecha_cancelacion", nullable = false)
    private LocalDateTime fechaCancelacion;

    @Column(name = "motivo", nullable = false, length = 250)
    private String motivo;

    @Column(name = "horas_anticipacion", nullable = false)
    private Integer horasAnticipacion;

    @Column(name = "porcentaje_penalizacion", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajePenalizacion;

    @Column(name = "monto_penalizacion", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoPenalizacion;

    @Column(name = "monto_reembolso", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoReembolso;

    @PrePersist
    void prePersist() {
        if (fechaCancelacion == null) {
            fechaCancelacion = LocalDateTime.now();
        }
    }
}
