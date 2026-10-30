import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { mensajeError } from '../api/axios';
import { estadisticasApi, reservasApi } from '../api/servicios';
import Cargando from '../components/Cargando';
import EstadoBadge from '../components/EstadoBadge';
import Icon from '../components/Icon';
import Vacio from '../components/Vacio';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { dinero, fecha, iniciales } from '../utils/formato';

function FilaLlegada({ r }) {
  return (
    <div className="arrivals-row">
      <div className="guest-avatar">{iniciales(r.huespedNombre)}</div>
      <div className="who">
        <div className="n">{r.huespedNombre}</div>
        <div className="r">{r.tipoHabitacion} {r.habitacionNumero} · {r.hotel}</div>
      </div>
      <div className="when">{fecha(r.fechaEntrada)}</div>
      <EstadoBadge estado={r.estado} />
    </div>
  );
}

function PanelPersonal({ nombre }) {
  const toast = useToast();
  const [datos, setDatos] = useState(null);

  useEffect(() => {
    estadisticasApi.dashboard().then(setDatos).catch((e) => toast.error(mensajeError(e)));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (!datos) return <Cargando />;

  const tarjetas = [
    ['building', datos.totalHoteles, `Hoteles registrados (${datos.totalSucursales} sucursales)`],
    ['bed', datos.totalHabitaciones, 'Habitaciones totales'],
    ['calendar', datos.reservasActivas, 'Reservas activas'],
    ['coin', dinero(datos.ingresosMes), 'Ingresos del mes'],
  ];

  return (
    <>
      <h1 className="page-title">Panel de control</h1>
      <p className="page-sub">Hola, {nombre}. Esto es lo que pasa hoy en tus hoteles.</p>

      <div className="stat-grid">
        {tarjetas.map(([icono, valor, texto]) => (
          <div className="stat-card" key={texto}>
            <div className="stat-icon"><Icon name={icono} /></div>
            <h3>{valor}</h3>
            <p>{texto}</p>
          </div>
        ))}
      </div>

      <div className="panel">
        <div className="panel-head">
          <h3>Ocupación semanal</h3>
          <span className="hint">Porcentaje de habitaciones ocupadas (lunes a domingo)</span>
        </div>
        <div className="chart-bars">
          {datos.ocupacionSemanal.map((d) => (
            <div className="bar-col" key={d.fecha} title={`${d.habitacionesOcupadas} habitación(es) · ${fecha(d.fecha)}`}>
              <span className="bar-val">{d.porcentaje}%</span>
              <div className="bar" style={{ height: `${Math.max(d.porcentaje, 2)}%` }} />
              <span className="bar-label">{d.dia}</span>
            </div>
          ))}
        </div>
      </div>

      <div className="panel">
        <div className="panel-head">
          <h3>Próximas llegadas</h3>
          <span className="hint">Siguientes 3 días</span>
        </div>
        {datos.proximasLlegadas.length === 0 ? (
          <Vacio mensaje="No hay llegadas programadas para los próximos 3 días." icono="calendar" />
        ) : (
          datos.proximasLlegadas.map((r) => <FilaLlegada key={r.id} r={r} />)
        )}
      </div>

      <div className="toolbar">
        <Link to="/reservar/buscar" className="btn btn-primary"><Icon name="search" />Buscar habitaciones</Link>
        <Link to="/reservas" className="btn btn-outline-cool"><Icon name="calendar" />Ver reservas</Link>
        <span className="muted">
          Cancelaciones del mes: <strong>{datos.cancelacionesMes}</strong> · Reclamos abiertos: <strong>{datos.reclamosAbiertos}</strong>
        </span>
      </div>
    </>
  );
}

function PanelHuesped({ nombre }) {
  const toast = useToast();
  const [reservas, setReservas] = useState(null);

  useEffect(() => {
    reservasApi.listar().then(setReservas).catch((e) => toast.error(mensajeError(e)));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (!reservas) return <Cargando />;

  const activas = reservas.filter((r) => ['PENDIENTE', 'CONFIRMADA', 'EN_CURSO'].includes(r.estado));
  const pendientes = activas.filter((r) => r.estado === 'PENDIENTE').length;

  return (
    <>
      <h1 className="page-title">Hola, {nombre}</h1>
      <p className="page-sub">Aquí puedes ver tus próximas estadías y hacer nuevas reservas.</p>

      <div className="stat-grid">
        <div className="stat-card"><div className="stat-icon"><Icon name="calendar" /></div><h3>{activas.length}</h3><p>Reservas activas</p></div>
        <div className="stat-card"><div className="stat-icon"><Icon name="card" /></div><h3>{pendientes}</h3><p>Pendientes de pago</p></div>
        <div className="stat-card"><div className="stat-icon"><Icon name="moon" /></div><h3>{reservas.filter((r) => r.estado === 'FINALIZADA').length}</h3><p>Estadías finalizadas</p></div>
        <div className="stat-card"><div className="stat-icon"><Icon name="list" /></div><h3>{reservas.length}</h3><p>Reservas en total</p></div>
      </div>

      <div className="panel">
        <div className="panel-head">
          <h3>Mis próximas estadías</h3>
          <Link to="/reservas" className="btn-text">Ver todas</Link>
        </div>
        {activas.length === 0 ? (
          <Vacio mensaje="Aún no tienes reservas activas." icono="bed" />
        ) : (
          activas.map((r) => <FilaLlegada key={r.id} r={r} />)
        )}
      </div>

      <Link to="/reservar/buscar" className="btn btn-primary"><Icon name="search" />Buscar habitaciones</Link>
    </>
  );
}

export default function Dashboard() {
  const { usuario, esPersonal } = useAuth();
  return esPersonal ? <PanelPersonal nombre={usuario.nombre} /> : <PanelHuesped nombre={usuario.nombre} />;
}
