import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/** Solo deja pasar a usuarios autenticados y, si se indica, con ciertos roles. */
export default function RutaProtegida({ roles }) {
  const { usuario } = useAuth();
  if (!usuario) {
    return <Navigate to="/login" replace />;
  }
  if (roles && !roles.includes(usuario.rol)) {
    return <Navigate to="/" replace />;
  }
  return <Outlet />;
}
