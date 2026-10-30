import { tiposApi } from '../../api/servicios';
import BotonesForm from '../../components/BotonesForm';
import Cargando from '../../components/Cargando';
import Confirmar from '../../components/Confirmar';
import Field from '../../components/Field';
import Icon from '../../components/Icon';
import Modal from '../../components/Modal';
import Vacio from '../../components/Vacio';
import useCrud from '../../hooks/useCrud';
import { dinero } from '../../utils/formato';

const NUEVO = { nombre: '', descripcion: '', capacidadMaxima: 2, precioNoche: '' };

export default function TiposHabitacion() {
  const crud = useCrud(tiposApi, 'Tipo de habitación');
  const f = crud.formulario;
  const cambiar = (e) => crud.setFormulario({ ...f, [e.target.name]: e.target.value });

  const enviar = (e) => {
    e.preventDefault();
    const { id, ...body } = f;
    crud.guardar(id, {
      ...body,
      capacidadMaxima: body.capacidadMaxima === '' ? null : body.capacidadMaxima,
      precioNoche: body.precioNoche === '' ? null : body.precioNoche,
    });
  };

  return (
    <>
      <div className="page-head">
        <div>
          <h1 className="page-title">Tipos de habitación y tarifas</h1>
          <p className="page-sub">La tarifa por noche se aplica a todas las habitaciones del tipo.</p>
        </div>
        <button className="btn btn-primary" onClick={() => crud.abrir(NUEVO)}><Icon name="plus" />Nuevo tipo</button>
      </div>

      {!crud.lista ? <Cargando /> : (
        <div className="table-wrap">
          {crud.lista.length === 0 ? <Vacio mensaje="No hay tipos registrados." icono="tag" /> : (
            <table className="data">
              <thead><tr><th>Tipo</th><th>Capacidad</th><th>Precio por noche</th><th /></tr></thead>
              <tbody>
                {crud.lista.map((t) => (
                  <tr key={t.id}>
                    <td><strong>{t.nombre}</strong><div className="muted">{t.descripcion}</div></td>
                    <td>{t.capacidadMaxima} persona(s)</td>
                    <td>{dinero(t.precioNoche)}</td>
                    <td className="actions">
                      <button className="btn-icon" title="Editar" onClick={() => crud.abrir({ ...t, descripcion: t.descripcion || '' })}><Icon name="pencil" /></button>
                      <button className="btn-icon danger" title="Eliminar" onClick={() => crud.setAEliminar(t)}><Icon name="trash" /></button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {f && (
        <Modal titulo={f.id ? 'Editar tipo de habitación' : 'Nuevo tipo de habitación'} onCerrar={crud.cerrar}
          pie={<BotonesForm onCerrar={crud.cerrar} guardando={crud.guardando} formId="form-tipo" />}>
          <form id="form-tipo" onSubmit={enviar} noValidate>
            <Field label="Nombre" error={crud.errores.nombre} required><input name="nombre" value={f.nombre} onChange={cambiar} placeholder="Suite" /></Field>
            <Field label="Descripción" error={crud.errores.descripcion}><textarea name="descripcion" value={f.descripcion} onChange={cambiar} maxLength={250} /></Field>
            <div className="field-row">
              <Field label="Capacidad máxima" error={crud.errores.capacidadMaxima} required>
                <input type="number" name="capacidadMaxima" value={f.capacidadMaxima} onChange={cambiar} min="1" max="10" />
              </Field>
              <Field label="Precio por noche ($)" error={crud.errores.precioNoche} required>
                <input type="number" name="precioNoche" value={f.precioNoche} onChange={cambiar} min="1" step="0.01" />
              </Field>
            </div>
          </form>
        </Modal>
      )}

      {crud.aEliminar && (
        <Confirmar mensaje={`¿Eliminar el tipo "${crud.aEliminar.nombre}"?`} onConfirmar={crud.eliminar}
          onCerrar={() => crud.setAEliminar(null)} cargando={crud.guardando} />
      )}
    </>
  );
}
