import { Link, Navigate } from 'react-router-dom';
import EstadoBadge from '../../components/EstadoBadge';
import Icon from '../../components/Icon';
import Stepper from '../../components/Stepper';
import { useReserva } from '../../context/ReservaContext';
import { dinero, fecha, POLITICA_CANCELACION } from '../../utils/formato';
import MiniPatron from './MiniPatron';

export default function Detalle() {
  const { seleccion } = useReserva();
  if (!seleccion) return <Navigate to="/reservar/buscar" replace />;
  const h = seleccion.habitacion;

  return (
    <>
      <Stepper actual={1} />

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14, flexWrap: 'wrap', gap: 8 }}>
        <h1 className="page-title" style={{ marginBottom: 0 }}>{h.tipo} · Habitación {h.numero}</h1>
        <EstadoBadge estado="DISPONIBLE" />
      </div>

      <div className="detail-hero">
        <MiniPatron id="pdet" ancho={400} alto={200} tam={34} />
        <Icon name="bed" />
      </div>

      <div className="detail-grid">
        <div className="d-row"><span className="k"><Icon name="building" />Hotel</span><span className="v">{h.hotel}</span></div>
        <div className="d-row"><span className="k"><Icon name="pin" />Sucursal</span><span className="v">{h.sucursal}, {h.ciudad}</span></div>
        <div className="d-row"><span className="k"><Icon name="users" />Capacidad</span><span className="v">{h.capacidad} persona(s)</span></div>
        <div className="d-row"><span className="k"><Icon name="coin" />Precio por noche</span><span className="v" style={{ color: 'var(--jade-700)' }}>{dinero(h.precioNoche)}</span></div>
        <div className="d-row"><span className="k"><Icon name="calendar" />Fechas</span><span className="v">{fecha(seleccion.fechaEntrada)} – {fecha(seleccion.fechaSalida)}</span></div>
        <div className="d-row"><span className="k"><Icon name="moon" />Estadía</span><span className="v">{seleccion.noches} noche(s) · {dinero(seleccion.subtotalEstimado)}</span></div>
        <div className="d-row"><span className="k"><Icon name="check" />Servicios</span><span className="v">{h.servicios.map((s) => s.nombre).join(', ') || 'Ninguno'}</span></div>
        <div className="d-row"><span className="k"><Icon name="info" />Piso</span><span className="v">{h.piso}</span></div>
      </div>

      <div className="policy-note">
        <Icon name="alert" />
        <span><strong>Políticas de la habitación:</strong> {POLITICA_CANCELACION} No se permiten mascotas.</span>
      </div>

      <Link to="/reservar/huesped" className="btn btn-primary btn-block" style={{ marginBottom: 10 }}>Continuar con la reserva</Link>
      <Link to="/reservar/resultados" className="btn btn-outline-cool btn-block"><Icon name="arrow-left" />Volver a resultados</Link>
    </>
  );
}
