import api from './axios';

// Cada función devuelve directamente el JSON de la respuesta
const datos = (promesa) => promesa.then((r) => r.data);

export const authApi = {
  login: (credenciales) => datos(api.post('/auth/login', credenciales)),
  registro: (usuario) => datos(api.post('/auth/registro', usuario)),
  perfil: () => datos(api.get('/auth/perfil')),
};

/** Genera las funciones CRUD estándar para un recurso. */
const crud = (ruta) => ({
  listar: (params) => datos(api.get(ruta, { params })),
  obtener: (id) => datos(api.get(`${ruta}/${id}`)),
  crear: (body) => datos(api.post(ruta, body)),
  actualizar: (id, body) => datos(api.put(`${ruta}/${id}`, body)),
  eliminar: (id) => datos(api.delete(`${ruta}/${id}`)),
});

export const hotelesApi = crud('/hoteles');
export const sucursalesApi = crud('/sucursales');
export const tiposApi = crud('/tipos-habitacion');
export const serviciosApi = crud('/servicios');

export const habitacionesApi = {
  ...crud('/habitaciones'),
  disponibles: (filtros) => datos(api.get('/habitaciones/disponibles', { params: filtros })),
  cambiarEstado: (id, estado) => datos(api.patch(`/habitaciones/${id}/estado`, { estado })),
};

export const usuariosApi = {
  ...crud('/usuarios'),
  cambiarEstado: (id, activo) => datos(api.patch(`/usuarios/${id}/estado`, { activo })),
};

export const huespedesApi = {
  ...crud('/huespedes'),
  miPerfil: () => datos(api.get('/huespedes/mi-perfil')),
  guardarMiPerfil: (body) => datos(api.put('/huespedes/mi-perfil', body)),
};

export const reservasApi = {
  ...crud('/reservas'),
  simularCancelacion: (id) => datos(api.get(`/reservas/${id}/cancelacion/simular`)),
  cancelar: (id, motivo) => datos(api.post(`/reservas/${id}/cancelar`, { motivo })),
  checkIn: (id) => datos(api.patch(`/reservas/${id}/check-in`)),
  checkOut: (id) => datos(api.patch(`/reservas/${id}/check-out`)),
  cancelaciones: () => datos(api.get('/reservas/cancelaciones')),
};

export const pagosApi = {
  pagar: (body) => datos(api.post('/pagos', body)),
  listar: () => datos(api.get('/pagos')),
  porReserva: (reservaId) => datos(api.get(`/pagos/reserva/${reservaId}`)),
};

export const reclamosApi = {
  ...crud('/reclamos'),
  responder: (id, body) => datos(api.patch(`/reclamos/${id}/respuesta`, body)),
};

export const estadisticasApi = {
  dashboard: () => datos(api.get('/estadisticas/dashboard')),
};
