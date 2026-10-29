const moneda = new Intl.NumberFormat('es-SV', { style: 'currency', currency: 'USD' });

export const dinero = (valor) => moneda.format(Number(valor || 0));

/** "2026-09-15" -> "15/09/2026" (sin problemas de zona horaria). */
export const fecha = (iso) => {
  if (!iso) return '—';
  const [a, m, d] = iso.substring(0, 10).split('-');
  return `${d}/${m}/${a}`;
};

export const fechaHora = (iso) => {
  if (!iso) return '—';
  return `${fecha(iso)} ${iso.substring(11, 16)}`;
};

/** Fecha local en formato AAAA-MM-DD. */
export const hoyISO = (sumarDias = 0) => {
  const d = new Date();
  d.setDate(d.getDate() + sumarDias);
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${mm}-${dd}`;
};

export const noches = (entrada, salida) => {
  if (!entrada || !salida) return 0;
  const diff = (new Date(salida) - new Date(entrada)) / 86400000;
  return diff > 0 ? Math.round(diff) : 0;
};

export const iniciales = (nombre = '') =>
  nombre.split(' ').filter(Boolean).slice(0, 2).map((p) => p[0].toUpperCase()).join('');

export const ROLES = {
  ADMIN: 'Administrador',
  RECEPCIONISTA: 'Recepcionista',
  HUESPED: 'Huésped',
};

export const POLITICA_CANCELACION =
  'Cancelación gratuita hasta 48 horas antes de la llegada (check-in 2:00 p.m.). Con menos anticipación se retiene el 50% del monto pagado.';
