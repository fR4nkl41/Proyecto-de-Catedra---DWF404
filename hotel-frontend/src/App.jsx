import { Navigate, Route, Routes } from 'react-router-dom';
import { IconSprite } from './components/Icon';
import Layout from './components/Layout';
import RutaProtegida from './components/RutaProtegida';
import Login from './pages/Login';
import Registro from './pages/Registro';
import Dashboard from './pages/Dashboard';
import Reservas from './pages/Reservas';
import Reclamos from './pages/Reclamos';
import Huespedes from './pages/Huespedes';
import Habitaciones from './pages/Habitaciones';
import PagosCancelaciones from './pages/PagosCancelaciones';
import Buscar from './pages/reserva/Buscar';
import Resultados from './pages/reserva/Resultados';
import Detalle from './pages/reserva/Detalle';
import DatosHuesped from './pages/reserva/DatosHuesped';
import Resumen from './pages/reserva/Resumen';
import Pago from './pages/reserva/Pago';
import Confirmacion from './pages/reserva/Confirmacion';
import Hoteles from './pages/admin/Hoteles';
import Sucursales from './pages/admin/Sucursales';
import TiposHabitacion from './pages/admin/TiposHabitacion';
import Servicios from './pages/admin/Servicios';
import Usuarios from './pages/admin/Usuarios';

const PERSONAL = ['ADMIN', 'RECEPCIONISTA'];

export default function App() {
  return (
    <>
      <IconSprite />
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/registro" element={<Registro />} />

        <Route element={<RutaProtegida />}>
          <Route element={<Layout />}>
            <Route path="/" element={<Dashboard />} />
            <Route path="/reservas" element={<Reservas />} />
            <Route path="/reclamos" element={<Reclamos />} />

            {/* Proceso de reserva */}
            <Route path="/reservar/buscar" element={<Buscar />} />
            <Route path="/reservar/resultados" element={<Resultados />} />
            <Route path="/reservar/detalle" element={<Detalle />} />
            <Route path="/reservar/huesped" element={<DatosHuesped />} />
            <Route path="/reservar/resumen" element={<Resumen />} />
            <Route path="/reservar/pago" element={<Pago />} />
            <Route path="/reservar/confirmacion" element={<Confirmacion />} />

            <Route element={<RutaProtegida roles={PERSONAL} />}>
              <Route path="/huespedes" element={<Huespedes />} />
              <Route path="/habitaciones" element={<Habitaciones />} />
              <Route path="/pagos" element={<PagosCancelaciones />} />
            </Route>

            <Route element={<RutaProtegida roles={['ADMIN']} />}>
              <Route path="/admin/hoteles" element={<Hoteles />} />
              <Route path="/admin/sucursales" element={<Sucursales />} />
              <Route path="/admin/tipos" element={<TiposHabitacion />} />
              <Route path="/admin/servicios" element={<Servicios />} />
              <Route path="/admin/usuarios" element={<Usuarios />} />
            </Route>
          </Route>
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
}
