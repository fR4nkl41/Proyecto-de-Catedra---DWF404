import { hotelesApi } from '../../api/servicios';
import BotonesForm from '../../components/BotonesForm';
import Cargando from '../../components/Cargando';
import Confirmar from '../../components/Confirmar';
import Field from '../../components/Field';
import Icon from '../../components/Icon';
import Modal from '../../components/Modal';
import Vacio from '../../components/Vacio';
import useCrud from '../../hooks/useCrud';

const NUEVO = { nombre: '', direccion: '', ciudad: '', telefono: '', email: '', descripcion: '' };

export default function Hoteles() {
  const crud = useCrud(hotelesApi, 'Hotel');
  const f = crud.formulario;
  const cambiar = (e) => crud.setFormulario({ ...f, [e.target.name]: e.target.value });

  const enviar = (e) => {
    e.preventDefault();
    const { id, totalSucursales, ...body } = f;
    crud.guardar(id, body);
  };

  return (
    <>
      <div className="page-head">
        <div>
          <h1 className="page-title">Hoteles</h1>
          <p className="page-sub">Hoteles y cadenas administradas en el sistema.</p>
        </div>
        <button className="btn btn-primary" onClick={() => crud.abrir(NUEVO)}><Icon name="plus" />Nuevo hotel</button>
      </div>

      {!crud.lista ? <Cargando /> : (
        <div className="table-wrap">
          {crud.lista.length === 0 ? <Vacio mensaje="No hay hoteles registrados." icono="building" /> : (
            <table className="data">
              <thead><tr><th>Nombre</th><th>Ciudad</th><th>Contacto</th><th>Sucursales</th><th /></tr></thead>
              <tbody>
                {crud.lista.map((h) => (
                  <tr key={h.id}>
                    <td><strong>{h.nombre}</strong><div className="muted">{h.direccion}</div></td>
                    <td>{h.ciudad}</td>
                    <td>{h.telefono}<div className="muted">{h.email}</div></td>
                    <td>{h.totalSucursales}</td>
                    <td className="actions">
                      <button className="btn-icon" title="Editar" onClick={() => crud.abrir({ ...h, descripcion: h.descripcion || '' })}><Icon name="pencil" /></button>
                      <button className="btn-icon danger" title="Eliminar" onClick={() => crud.setAEliminar(h)}><Icon name="trash" /></button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {f && (
        <Modal titulo={f.id ? 'Editar hotel' : 'Nuevo hotel'} onCerrar={crud.cerrar}
          pie={<BotonesForm onCerrar={crud.cerrar} guardando={crud.guardando} formId="form-hotel" />}>
          <form id="form-hotel" onSubmit={enviar} noValidate>
            <Field label="Nombre" error={crud.errores.nombre} required><input name="nombre" value={f.nombre} onChange={cambiar} /></Field>
            <Field label="Dirección" error={crud.errores.direccion} required><input name="direccion" value={f.direccion} onChange={cambiar} /></Field>
            <div className="field-row">
              <Field label="Ciudad" error={crud.errores.ciudad} required><input name="ciudad" value={f.ciudad} onChange={cambiar} /></Field>
              <Field label="Teléfono" error={crud.errores.telefono} required><input name="telefono" value={f.telefono} onChange={cambiar} placeholder="2222-3333" /></Field>
            </div>
            <Field label="Correo electrónico" error={crud.errores.email} required><input type="email" name="email" value={f.email} onChange={cambiar} /></Field>
            <Field label="Descripción" error={crud.errores.descripcion}><textarea name="descripcion" value={f.descripcion} onChange={cambiar} maxLength={300} /></Field>
          </form>
        </Modal>
      )}

      {crud.aEliminar && (
        <Confirmar mensaje={`¿Eliminar el hotel "${crud.aEliminar.nombre}"?`} onConfirmar={crud.eliminar}
          onCerrar={() => crud.setAEliminar(null)} cargando={crud.guardando} />
      )}
    </>
  );
}
