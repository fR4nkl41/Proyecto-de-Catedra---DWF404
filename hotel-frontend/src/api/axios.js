import axios from 'axios';

export const TOKEN_KEY = 'anil_token';
export const USER_KEY = 'anil_usuario';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
});

// Agrega el token JWT a cada petición
api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Si el token expiró o no es válido, se cierra la sesión
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const esLogin = error.config?.url?.includes('/auth/login');
    if (error.response?.status === 401 && !esLogin && localStorage.getItem(TOKEN_KEY)) {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
      sessionStorage.setItem('anil_aviso', error.response.data?.mensaje || 'Tu sesión expiró. Inicia sesión nuevamente.');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

/** Mensaje amigable a partir de un error de Axios. */
export function mensajeError(error) {
  if (!error.response) {
    return 'No se pudo conectar con el servidor. Verifica que la API esté en ejecución en http://localhost:8080.';
  }
  return error.response.data?.mensaje || 'Ocurrió un error inesperado. Intenta de nuevo.';
}

/** Errores por campo devueltos por las validaciones del servidor. */
export function erroresCampos(error) {
  return error.response?.data?.errores || {};
}

export default api;
