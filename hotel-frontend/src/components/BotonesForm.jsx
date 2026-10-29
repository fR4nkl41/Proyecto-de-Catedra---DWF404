/** Botones Cancelar / Guardar del pie de los modales de formulario. */
export default function BotonesForm({ onCerrar, guardando, formId }) {
  return (
    <>
      <button type="button" className="btn btn-outline-cool" onClick={onCerrar} disabled={guardando}>Cancelar</button>
      <button type="submit" form={formId} className="btn btn-primary" disabled={guardando}>
        {guardando ? 'Guardando...' : 'Guardar'}
      </button>
    </>
  );
}
