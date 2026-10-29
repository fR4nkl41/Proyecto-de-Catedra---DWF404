import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { iniciales, ROLES } from '../utils/formato';
import Icon from './Icon';

function Logo() {
  return (
    <svg width="26" height="26" viewBox="0 0 28 28">
      <rect x="1" y="1" width="26" height="26" rx="7" fill="#4C5FA8" />
      <path d="M14 6 L22 14 L14 22 L6 14 Z" fill="none" stroke="#fff" strokeWidth="1.6" />
      <circle cx="14" cy="14" r="2.1" fill="#fff" />
    </svg>
  );
}

function Item({ to, icono, children, end }) {
  return (
    <NavLink to={to} end={end} className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
      <Icon name={icono} />
      {children}
    </NavLink>
  );
}

const hoy = new Date().toLocaleDateString('es-SV', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });

export default function Layout() {
  const { usuario, logout, esAdmin, esPersonal } = useAuth();
  const navigate = useNavigate();

  const salir = () => {
    logout();
    navigate('/login');
  };

  return (
    <div id="app-shell" className="active">
      <aside className="sidebar">
        <div className="sidebar-logo">
          <Logo />
          <span className="brand-wordmark">Añil</span>
        </div>

        <nav className="nav-group">
          <Item to="/" icono="grid" end>Panel de control</Item>
          <Item to="/reservar/buscar" icono="search">Buscar habitaciones</Item>
          <Item to="/reservas" icono="calendar">{esPersonal ? 'Reservas' : 'Mis reservas'}</Item>
          <Item to="/reclamos" icono="chat">{esPersonal ? 'Reclamos' : 'Mis reclamos'}</Item>

          {esPersonal && (
            <>
              <div className="nav-section">Operación</div>
              <Item to="/huespedes" icono="users">Huéspedes</Item>
              <Item to="/habitaciones" icono="bed">Habitaciones</Item>
              <Item to="/pagos" icono="coin">Pagos y cancelaciones</Item>
            </>
          )}

          {esAdmin && (
            <>
              <div className="nav-section">Administración</div>
              <Item to="/admin/hoteles" icono="building">Hoteles</Item>
              <Item to="/admin/sucursales" icono="pin">Sucursales</Item>
              <Item to="/admin/tipos" icono="tag">Tipos y tarifas</Item>
              <Item to="/admin/servicios" icono="check">Servicios</Item>
              <Item to="/admin/usuarios" icono="key">Usuarios</Item>
            </>
          )}
        </nav>

        <div className="sidebar-divider" />
        <div className="sidebar-user">
          <div className="avatar">{iniciales(usuario.nombre)}</div>
          <div className="who">
            <div className="name">{usuario.nombre}</div>
            <div className="role">{ROLES[usuario.rol]}</div>
          </div>
          <button className="icon-btn" title="Cerrar sesión" onClick={salir}>
            <Icon name="logout" />
          </button>
        </div>
      </aside>

      <div className="main">
        <div className="util-bar">
          <span style={{ textTransform: 'capitalize' }}>{hoy}</span>
          <div className="avatar">{iniciales(usuario.nombre)}</div>
        </div>
        <div className="content">
          <Outlet />
        </div>
      </div>
    </div>
  );
}
