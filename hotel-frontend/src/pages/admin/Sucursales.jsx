import { useEffect, useState } from 'react';
import { hotelesApi, sucursalesApi } from '../../api/servicios';
import BotonesForm from '../../components/BotonesForm';
import Cargando from '../../components/Cargando';
import Confirmar from '../../components/Confirmar';
import Field from '../../components/Field';
import Icon from '../../components/Icon';
import Modal from '../../components/Modal';
import Vacio from '../../components/Vacio';
import useCrud from '../../hooks/useCrud';

const NUEVO = { hotelId: '', nombre: '', direccion: '', ciudad: '', telefono: '' };

export default function Sucursales() {
  const [hoteles, setHoteles] = useState([]);
  const [hotelFiltro, setHotelFiltro] = useState('');
  const crud = useCrud(sucursalesApi, 'Sucursal', hotelFiltro ? { hotelId: hotelFiltro } : undefined);
  const f = crud.formulario;

  useEffect(() => {
    hotelesApi.listar().then(setHoteles).catch(() => {});
  }, []);

  const cambiar = (e) => crud.setFormulario({ ...f, [e.target.name]: e.target.value });

  const enviar = (e) => {
    e.preventDefault();
    const { id, hotel, ...body } = f;
    crud.guardar(id, { ...body, hotelId: body.hotelId ? Number(body.hotelId) : null });
  };

  return (
    <>
      <div className="page-head">
        <div>
          <h1 className="page-title">Sucursales</h1>
          <p className="page-sub">Sedes físicas de cada hotel, donde se ubican las habitaciones.</p>
        </div>
        <button className="btn btn-primary" onClick={() => crud.abrir({ ...NUEVO, hotelId: hotelFiltro })}><Icon name="plus" />Nueva sucursal</button>
      </div>

      <div className="toolbar">
        <select value={hotelFiltro} onChange={(e) => setHotelFiltro(e.target.value)}>
          <option value="">Todos los hoteles</option>
          {hoteles.map((h) => <option key={h.id} value={h.id}>{h.nombre}</option>)}
        </select>
      </div>

      {!crud.lista ? <Cargando /> : (
        <div className="table-wrap">
          {crud.lista.length === 0 ? <Vacio mensaje="No hay sucursales registradas." icono="pin" /> : (
            <table className="data">
              <thead><tr><th>Sucursal</th><th>Hotel</th><th>Ciudad</th><th>Teléfono</th><th /></tr></thead>
              <tbody>
                {crud.lista.map((s) => (
                  <tr key={s.id}>
                    <td><strong>{s.nombre}</strong><div className="muted">{s.direccion}</div></td>
                    <td>{s.hotel}</td>
                    <td>{s.ciudad}</td>
                    <td>{s.telefono}</td>
                    <td className="actions">
                      <button className="btn-icon" title="Editar" onClick={() => crud.abrir({ ...s })}><Icon name="pencil" /></button>
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
        <Modal titulo={f.id ? 'Editar sucursal' : 'Nueva sucursal'} onCerrar={crud.cerrar}
          pie={<BotonesForm onCerrar={crud.cerrar} guardando={crud.guardando} formId="form-sucursal" />}>
          <form id="form-sucursal" onSubmit={enviar} noValidate>
            <Field label="Hotel" error={crud.errores.hotelId} required>
              <select name="hotelId" value={f.hotelId} onChange={cambiar}>
                <option value="">Selecciona un hotel</option>
                {hoteles.map((h) => <option key={h.id} value={h.id}>{h.nombre}</option>)}
              </select>
            </Field>
            <Field label="Nombre de la sucursal" error={crud.errores.nombre} required><input name="nombre" value={f.nombre} onChange={cambiar} placeholder="Sede Los Héroes" /></Field>
            <Field label="Dirección" error={crud.errores.direccion} required><input name="direccion" value={f.direccion} onChange={cambiar} /></Field>
            <div className="field-row">
              <Field label="Ciudad" error={crud.errores.ciudad} required><input name="ciudad" value={f.ciudad} onChange={cambiar} /></Field>
              <Field label="Teléfono" error={crud.errores.telefono} required><input name="telefono" value={f.telefono} onChange={cambiar} placeholder="2222-3333" /></Field>
            </div>
          </form>
        </Modal>
      )}

      {crud.aEliminar && (
        <Confirmar mensaje={`¿Eliminar la sucursal "${crud.aEliminar.nombre}"?`} onConfirmar={crud.eliminar}
          onCerrar={() => crud.setAEliminar(null)} cargando={crud.guardando} />
      )}
    </>
  );
}
