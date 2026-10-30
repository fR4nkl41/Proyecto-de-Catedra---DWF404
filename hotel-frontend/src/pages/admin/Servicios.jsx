import { serviciosApi } from '../../api/servicios';
import BotonesForm from '../../components/BotonesForm';
import Cargando from '../../components/Cargando';
import Confirmar from '../../components/Confirmar';
import Field from '../../components/Field';
import Icon from '../../components/Icon';
import Modal from '../../components/Modal';
import Vacio from '../../components/Vacio';
import useCrud from '../../hooks/useCrud';
import { dinero } from '../../utils/formato';

const NUEVO = { nombre: '', descripcion: '', costo: '0', adicional: 'false' };

export default function Servicios() {
  const crud = useCrud(serviciosApi, 'Servicio');
  const f = crud.formulario;
  const cambiar = (e) => crud.setFormulario({ ...f, [e.target.name]: e.target.value });

  const enviar = (e) => {
    e.preventDefault();
    const { id, ...body } = f;
    crud.guardar(id, { ...body, costo: body.costo === '' ? null : body.costo, adicional: body.adicional === 'true' });
  };

  return (
    <>
      <div className="page-head">
        <div>
          <h1 className="page-title">Servicios</h1>
          <p className="page-sub">Incluidos en las habitaciones o adicionales que se cobran en la reserva (alimentación, transporte, lavandería...).</p>
        </div>
        <button className="btn btn-primary" onClick={() => crud.abrir(NUEVO)}><Icon name="plus" />Nuevo servicio</button>
      </div>

      {!crud.lista ? <Cargando /> : (
        <div className="table-wrap">
          {crud.lista.length === 0 ? <Vacio mensaje="No hay servicios registrados." /> : (
            <table className="data">
              <thead><tr><th>Servicio</th><th>Tipo</th><th>Costo</th><th /></tr></thead>
              <tbody>
                {crud.lista.map((s) => (
                  <tr key={s.id}>
                    <td><strong>{s.nombre}</strong><div className="muted">{s.descripcion}</div></td>
                    <td>{s.adicional ? <span className="badge badge-amber">Adicional</span> : <span className="badge badge-anil">Incluido</span>}</td>
                    <td>{s.adicional ? dinero(s.costo) : '—'}</td>
                    <td className="actions">
                      <button className="btn-icon" title="Editar"
                        onClick={() => crud.abrir({ ...s, descripcion: s.descripcion || '', costo: String(s.costo), adicional: String(s.adicional) })}><Icon name="pencil" /></button>
                      <button className="btn-icon danger" title="Eliminar" onClick={() => crud.setAEliminar(s)}><Icon name="trash" /></button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {f && (
        <Modal titulo={f.id ? 'Editar servicio' : 'Nuevo servicio'} onCerrar={crud.cerrar}
          pie={<BotonesForm onCerrar={crud.cerrar} guardando={crud.guardando} formId="form-servicio" />}>
          <form id="form-servicio" onSubmit={enviar} noValidate>
            <Field label="Nombre" error={crud.errores.nombre} required><input name="nombre" value={f.nombre} onChange={cambiar} /></Field>
            <Field label="Descripción" error={crud.errores.descripcion}><textarea name="descripcion" value={f.descripcion} onChange={cambiar} maxLength={250} /></Field>
            <div className="field-row">
              <Field label="Tipo" error={crud.errores.adicional} required>
                <select name="adicional" value={f.adicional} onChange={cambiar}>
                  <option value="false">Incluido en la habitación</option>
                  <option value="true">Adicional (se cobra en la reserva)</option>
                </select>
              </Field>
              <Field label="Costo ($)" error={crud.errores.costo} required>
                <input type="number" name="costo" value={f.costo} onChange={cambiar} min="0" step="0.01" />
              </Field>
            </div>
          </form>
        </Modal>
      )}

      {crud.aEliminar && (
        <Confirmar mensaje={`¿Eliminar el servicio "${crud.aEliminar.nombre}"?`} onConfirmar={crud.eliminar}
          onCerrar={() => crud.setAEliminar(null)} cargando={crud.guardando} />
      )}
    </>
  );
}
