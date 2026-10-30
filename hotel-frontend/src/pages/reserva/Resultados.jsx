import { Link, Navigate, useNavigate } from 'react-router-dom';
import EstadoBadge from '../../components/EstadoBadge';
import Icon from '../../components/Icon';
import Vacio from '../../components/Vacio';
import { useReserva } from '../../context/ReservaContext';
import { dinero, fecha } from '../../utils/formato';
import MiniPatron from './MiniPatron';

export default function Resultados() {
  const { busqueda, resultados, actualizar } = useReserva();
  const navigate = useNavigate();

  if (!busqueda) return <Navigate to="/reservar/buscar" replace />;

  const seleccionar = (resultado) => {
    actualizar({ seleccion: resultado, reserva: null, pago: null, errores: {} });
    navigate('/reservar/detalle');
  };

  return (
    <>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', flexWrap: 'wrap', gap: 8 }}>
        <h1 className="page-title">Resultados</h1>
        <Link to="/reservar/buscar" className="btn-text"><Icon name="pencil" style={{ width: 14, height: 14 }} />Modificar búsqueda</Link>
      </div>
      <p className="page-sub">
        {resultados.length} habitación(es) disponible(s){busqueda.ciudad ? ` en ${busqueda.ciudad}` : ''} · {fecha(busqueda.fechaEntrada)} al {fecha(busqueda.fechaSalida)} · {busqueda.huespedes} huésped(es)
      </p>

      {resultados.length === 0 && (
        <div className="panel">
          <Vacio mensaje="No hay habitaciones disponibles con esos filtros. Prueba con otras fechas, otra ciudad o menos huéspedes." icono="search" />
        </div>
      )}

      {resultados.map((r, i) => {
        const h = r.habitacion;
        const servicios = h.servicios.map((s) => s.nombre).join(', ');
        return (
          <div className="hotel-card" key={h.id}>
            <div className="hotel-thumb">
              <MiniPatron id={`p${i}`} />
              <Icon name="building" />
            </div>
            <div className="info">
              <h3>{h.hotel}</h3>
              <div className="meta">
                <Icon name="bed" />
                {h.tipo} {h.numero} · {h.capacidad} persona(s){servicios ? ` · ${servicios}` : ''}
              </div>
              <div className="meta"><Icon name="pin" />{h.sucursal} · {h.ciudad}</div>
              <EstadoBadge estado="DISPONIBLE" />
            </div>
            <div className="price-col">
              <div className="price">{dinero(h.precioNoche)}<small>/noche</small></div>
              <div className="muted">{r.noches} noche(s): {dinero(r.subtotalEstimado)}</div>
              <button className="btn btn-success btn-sm" style={{ marginTop: 8 }} onClick={() => seleccionar(r)}>Seleccionar</button>
            </div>
          </div>
        );
      })}

      <Link to="/reservar/buscar" className="btn btn-outline-cool"><Icon name="arrow-left" />Nueva búsqueda</Link>
    </>
  );
}
