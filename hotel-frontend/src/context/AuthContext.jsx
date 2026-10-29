import { createContext, useContext, useState } from 'react';
import { TOKEN_KEY, USER_KEY } from '../api/axios';
import { authApi } from '../api/servicios';

const AuthContext = createContext(null);

function leerUsuario() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY));
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(() => (localStorage.getItem(TOKEN_KEY) ? leerUsuario() : null));

  const guardarSesion = (respuesta) => {
    localStorage.setItem(TOKEN_KEY, respuesta.token);
    localStorage.setItem(USER_KEY, JSON.stringify(respuesta.usuario));
    setUsuario(respuesta.usuario);
    return respuesta;
  };

  const login = async (email, password) => guardarSesion(await authApi.login({ email, password }));

  const registrar = async (datos) => guardarSesion(await authApi.registro(datos));

  const logout = () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    sessionStorage.removeItem('anil_reserva');
    setUsuario(null);
  };

  const rol = usuario?.rol;
  const valor = {
    usuario,
    login,
    registrar,
    logout,
    esAdmin: rol === 'ADMIN',
    esPersonal: rol === 'ADMIN' || rol === 'RECEPCIONISTA',
    esHuesped: rol === 'HUESPED',
  };

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
