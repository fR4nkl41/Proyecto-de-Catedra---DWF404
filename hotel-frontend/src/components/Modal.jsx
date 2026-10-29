import Icon from './Icon';

export default function Modal({ titulo, onCerrar, children, pie, ancho }) {
  return (
    <div className="modal-backdrop" onMouseDown={(e) => e.target === e.currentTarget && onCerrar()}>
      <div className={`modal ${ancho ? 'wide' : ''}`} role="dialog" aria-modal="true">
        <div className="modal-head">
          <h2>{titulo}</h2>
          <button className="btn-icon" onClick={onCerrar} title="Cerrar">
            <Icon name="x" />
          </button>
        </div>
        <div className="modal-body">{children}</div>
        {pie && <div className="modal-foot">{pie}</div>}
      </div>
    </div>
  );
}
