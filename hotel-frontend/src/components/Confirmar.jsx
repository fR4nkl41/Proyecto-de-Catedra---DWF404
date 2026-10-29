import Modal from './Modal';

/** Diálogo de confirmación para acciones como eliminar o cancelar. */
export default function Confirmar({ titulo = 'Confirmar acción', mensaje, textoBoton = 'Eliminar', peligro = true, onConfirmar, onCerrar, cargando }) {
  return (
    <Modal
      titulo={titulo}
      onCerrar={onCerrar}
      pie={
        <>
          <button className="btn btn-outline-cool" onClick={onCerrar} disabled={cargando}>Volver</button>
          <button className={`btn ${peligro ? 'btn-danger' : 'btn-primary'}`} onClick={onConfirmar} disabled={cargando}>
            {cargando ? 'Procesando...' : textoBoton}
          </button>
        </>
      }
    >
      <p style={{ fontSize: 14.5, lineHeight: 1.55 }}>{mensaje}</p>
    </Modal>
  );
}
