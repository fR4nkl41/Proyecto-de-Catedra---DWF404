import Icon from './Icon';

const ESTILOS = {
  // reservas
  PENDIENTE: ['badge-amber', 'Pendiente'],
  CONFIRMADA: ['badge-jade', 'Confirmada'],
  EN_CURSO: ['badge-anil', 'En curso'],
  FINALIZADA: ['badge-gray', 'Finalizada'],
  CANCELADA: ['badge-clay', 'Cancelada'],
  // habitaciones
  DISPONIBLE: ['badge-jade', 'Disponible'],
  OCUPADA: ['badge-anil', 'Ocupada'],
  MANTENIMIENTO: ['badge-amber', 'Mantenimiento'],
  // pagos
  COMPLETADO: ['badge-jade', 'Pagado'],
  REEMBOLSADO: ['badge-gray', 'Reembolsado'],
  REEMBOLSO_PARCIAL: ['badge-amber', 'Reembolso parcial'],
  // reclamos
  ABIERTO: ['badge-amber', 'Abierto'],
  EN_PROCESO: ['badge-anil', 'En proceso'],
  RESUELTO: ['badge-jade', 'Resuelto'],
  // usuarios
  ACTIVO: ['badge-jade', 'Activo'],
  INACTIVO: ['badge-clay', 'Inactivo'],
};

export default function EstadoBadge({ estado }) {
  const [clase, texto] = ESTILOS[estado] || ['badge-gray', estado];
  const conCheck = ['CONFIRMADA', 'DISPONIBLE', 'COMPLETADO', 'RESUELTO', 'ACTIVO'].includes(estado);
  return (
    <span className={`badge ${clase}`}>
      {conCheck && <Icon name="check-plain" />}
      {texto}
    </span>
  );
}
