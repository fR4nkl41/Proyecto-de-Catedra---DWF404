import Icon from './Icon';

export default function Vacio({ mensaje = 'No hay registros para mostrar.', icono = 'list' }) {
  return (
    <div className="empty">
      <Icon name={icono} />
      <p>{mensaje}</p>
    </div>
  );
}
