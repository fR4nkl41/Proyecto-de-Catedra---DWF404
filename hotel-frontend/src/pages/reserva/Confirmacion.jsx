import { Navigate, useNavigate } from 'react-router-dom';
import Icon from '../../components/Icon';
import { useReserva } from '../../context/ReservaContext';
import { dinero, fecha } from '../../utils/formato';

const METODOS = { TARJETA: 'Tarjeta de crédito', TRANSFERENCIA: 'Transferencia bancaria', EFECTIVO: 'Efectivo' };

export default function Confirmacion() {
  const reserva = useReserva();
  const navigate = useNavigate();
  const r = reserva.reserva;
  if (!r) return <Navigate to="/" replace />;
  const pago = reserva.pago;

  const ir = (ruta) => {
    reserva.reiniciar();
    navigate(ruta);
  };

  return (
    <div className="confirm-wrap">
      <div className="confirm-check" style={pago ? undefined : { background: 'var(--amber-100)', color: 'var(--amber-700)' }}>
        <Icon name={pago ? 'check-plain' : 'info'} />
      </div>
      <h1>{pago ? 'Reserva confirmada' : 'Reserva registrada'}</h1>
      <p>
        {pago
          ? 'El pago se procesó correctamente. Guarda tu número de reserva.'
          : 'Tu reserva quedó pendiente de pago. Puedes pagarla desde "Mis reservas" o en la recepción del hotel.'}
      </p>

      <div className="receipt" style={{ textAlign: 'left' }}>
        <div className="r-row"><span className="k">Número de reserva</span><span className="v" style={{ color: 'var(--anil-700)' }}>#{r.codigo}</span></div>
        <div className="r-row"><span className="k">Hotel</span><span className="v">{r.hotel} · {r.sucursal}</span></div>
        <div className="r-row"><span className="k">Habitación</span><span className="v">{r.tipoHabitacion} {r.habitacionNumero}</span></div>
        <div className="r-row"><span className="k">Fechas</span><span className="v">{fecha(r.fechaEntrada)} – {fecha(r.fechaSalida)}</span></div>
        <div className="r-row"><span className="k">{pago ? 'Total pagado' : 'Total a pagar'}</span><span className="v">{dinero(r.precioTotal)}</span></div>
        {pago && <div className="r-row"><span className="k">Método de pago</span><span className="v">{METODOS[pago.metodoPago]} {pago.referencia ? `(${pago.referencia})` : ''}</span></div>}
      </div>

      <svg className="confirm-divider" viewBox="0 0 220 14">
        <defs>
          <pattern id="pconf" width="14" height="14" patternUnits="userSpaceOnUse">
            <path d="M7 1 L13 7 L7 13 L1 7 Z" fill="none" stroke="#33418C" strokeWidth="1" />
          </pattern>
        </defs>
        <rect width="220" height="14" fill="url(#pconf)" />
      </svg>

      <div className="confirm-actions">
        <button className="btn btn-primary" onClick={() => ir('/')}><Icon name="grid" />Ir al panel</button>
        <button className="btn btn-outline-cool" onClick={() => ir('/reservas')}><Icon name="calendar" />Ver reservas</button>
        <button className="btn btn-outline-cool" onClick={() => ir('/reservar/buscar')}><Icon name="search" />Nueva búsqueda</button>
      </div>
    </div>
  );
}
