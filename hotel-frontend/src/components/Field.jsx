import Icon from './Icon';

/**
 * Campo de formulario con etiqueta y mensaje de error.
 * "error" viene de las validaciones del servidor (respuesta.errores[campo]).
 */
export default function Field({ label, icon, error, required, children, className = '' }) {
  return (
    <div className={`field ${error ? 'has-error' : ''} ${className}`}>
      {label && (
        <label className="f-label">
          {icon && <Icon name={icon} />}
          {label} {required && <span className="req">*</span>}
        </label>
      )}
      {children}
      {error && <span className="field-error">{error}</span>}
    </div>
  );
}
