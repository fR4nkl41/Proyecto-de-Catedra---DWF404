import { useEffect, useState } from 'react';
import { mensajeError } from '../api/axios';
import { habitacionesApi, serviciosApi, sucursalesApi, tiposApi } from '../api/servicios';
import BotonesForm from '../components/BotonesForm';
import Cargando from '../components/Cargando';
import Confirmar from '../components/Confirmar';
import EstadoBadge from '../components/EstadoBadge';
import Field from '../components/Field';
import Icon from '../components/Icon';
import Modal from '../components/Modal';
import Vacio from '../components/Vacio';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import useCrud from '../hooks/useCrud';
import { dinero } from '../utils/formato';

const ESTADOS = ['DISPONIBLE', 'OCUPADA', 'MANTENIMIENTO'];
const NUEVA = { sucursalId: '', tipoId: '', numero: '', piso: 1, estado: 'DISPONIBLE', servicioIds: [] };

export default function Habitaciones() {
  const { esAdmin } = useAuth();
  const toast = useToast();
  const [filtros, setFiltros] = useState({ sucursalId: '', estado: '' });
  const params = Object.fromEntries(Object.entries(filtros).filter(([, v]) => v));
  const crud = useCrud(habitacionesApi, 'Habitación', params);
  const [sucursales, setSucursales] = useState([]);
  const [tipos, setTipos] = useState([]);
  const [incluidos, setIncluidos] = useState([]);
  const f = crud.formulario;

  useEffect(() => {
    sucursalesApi.listar().then(setSucursales).catch(() => {});
    tiposApi.listar().then(setTipos).catch(() => {});
    serviciosApi.listar({ adicional: false }).then(setIncluidos).catch(() => {});
  }, []);

  const cambiar = (e) => crud.setFormulario({ ...f, [e.target.name]: e.target.value });

  const alternarServicio = (id) => {
    const ids = f.servicioIds.includes(id) ? f.servicioIds.filter((x) => x !== id) : [...f.servicioIds, id];
    crud.setFormulario({ ...f, servicioIds: ids });
  };

  const enviar = (e) => {
    e.preventDefault();
    const { id, sucursalId, tipoId, numero, piso, estado, servicioIds } = f;
    crud.guardar(id, {
      sucursalId: sucursalId ? Number(sucursalId) : null,
      tipoId: tipoId ? Number(tipoId) : null,
      numero,
      piso: piso === '' ? null : piso,
      estado,
      servicioIds,
    });
  };

  const cambiarEstado = async (h, estado) => {
    try {
      await habitacionesApi.cambiarEstado(h.id, estado);
      toast.exito(`Habitación ${h.numero} ahora está en estado ${estado.toLowerCase()}.`);
      crud.cargar();
    } catch (err) {
      toast.error(mensajeError(err));
    }
  };

  const editar = (h) =>
    crud.abrir({ id: h.id, sucursalId: h.sucursalId, tipoId: h.tipoId, numero: h.numero, piso: h.piso, estado: h.estado,
      servicioIds: h.servicios.map((s) => s.id) });

  return (
    <>
      <div className="page-head">
        <div>
          <h1 className="page-title">Habitaciones</h1>
          <p className="page-sub">Estado de las habitaciones de todas las sucursales.</p>
        </div>
        {esAdmin && <button className="btn btn-primary" onClick={() => crud.abrir(NUEVA)}><Icon name="plus" />Nueva habitación</button>}
      </div>

      <div className="toolbar">
        <select value={filtros.sucursalId} onChange={(e) => setFiltros({ ...filtros, sucursalId: e.target.value })}>
          <option value="">Todas las sucursales</option>
          {sucursales.map((s) => <option key={s.id} value={s.id}>{s.hotel} · {s.nombre}</option>)}
        </select>
        <select value={filtros.estado} onChange={(e) => setFiltros({ ...filtros, estado: e.target.value })}>
          <option value="">Todos los estados</option>
          {ESTADOS.map((e) => <option key={e} value={e}>{e}</option>)}
        </select>
      </div>

      {!crud.lista ? <Cargando /> : (
        <div className="table-wrap">
          {crud.lista.length === 0 ? <Vacio mensaje="No hay habitaciones con esos filtros." icono="bed" /> : (
            <table className="data">
              <thead><tr><th>Número</th><th>Hotel / sucursal</th><th>Tipo</th><th>Precio</th><th>Estado</th><th /></tr></thead>
              <tbody>
                {crud.lista.map((h) => (
                  <tr key={h.id}>
                    <td><strong>{h.numero}</strong><div className="muted">Piso {h.piso}</div></td>
                    <td>{h.hotel}<div className="muted">{h.sucursal} · {h.ciudad}</div></td>
                    <td>{h.tipo}<div className="muted">{h.capacidad} pers. · {h.servicios.map((s) => s.nombre).join(', ')}</div></td>
                    <td>{dinero(h.precioNoche)}</td>
                    <td>
                      <select value={h.estado} onChange={(e) => cambiarEstado(h, e.target.value)} style={{ width: 'auto', padding: '6px 30px 6px 10px', fontSize: 13 }}>
                        {ESTADOS.map((e) => <option key={e} value={e}>{e}</option>)}
                      </select>
                    </td>
                    <td className="actions">
                      {esAdmin ? (
                        <>
                          <button className="btn-icon" title="Editar" onClick={() => editar(h)}><Icon name="pencil" /></button>
                          <button className="btn-icon danger" title="Eliminar" onClick={() => crud.setAEliminar(h)}><Icon name="trash" /></button>
                        </>
                      ) : <EstadoBadge estado={h.estado} />}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {f && (
        <Modal titulo={f.id ? 'Editar habitación' : 'Nueva habitación'} onCerrar={crud.cerrar}
          pie={<BotonesForm onCerrar={crud.cerrar} guardando={crud.guardando} formId="form-habitacion" />}>
          <form id="form-habitacion" onSubmit={enviar} noValidate>
            <Field label="Sucursal" error={crud.errores.sucursalId} required>
              <select name="sucursalId" value={f.sucursalId} onChange={cambiar}>
                <option value="">Selecciona una sucursal</option>
                {sucursales.map((s) => <option key={s.id} value={s.id}>{s.hotel} · {s.nombre}</option>)}
              </select>
            </Field>
            <Field label="Tipo de habitación" error={crud.errores.tipoId} required>
              <select name="tipoId" value={f.tipoId} onChange={cambiar}>
                <option value="">Selecciona un tipo</option>
                {tipos.map((t) => <option key={t.id} value={t.id}>{t.nombre} · {dinero(t.precioNoche)}/noche</option>)}
              </select>
            </Field>
            <div className="field-row">
              <Field label="Número" error={crud.errores.numero} required><input name="numero" value={f.numero} onChange={cambiar} placeholder="101" /></Field>
              <Field label="Piso" error={crud.errores.piso} required><input type="number" name="piso" value={f.piso} onChange={cambiar} min="0" max="60" /></Field>
              <Field label="Estado" error={crud.errores.estado}>
                <select name="estado" value={f.estado} onChange={cambiar}>
                  {ESTADOS.map((e) => <option key={e} value={e}>{e}</option>)}
                </select>
              </Field>
            </div>
            <Field label="Servicios incluidos" error={crud.errores.servicioIds}>
              <div className="checks">
                {incluidos.map((s) => (
                  <label key={s.id}>
                    <input type="checkbox" checked={f.servicioIds.includes(s.id)} onChange={() => alternarServicio(s.id)} />
                    {s.nombre}
                  </label>
                ))}
              </div>
            </Field>
          </form>
        </Modal>
      )}

      {crud.aEliminar && (
        <Confirmar mensaje={`¿Eliminar la habitación ${crud.aEliminar.numero} de ${crud.aEliminar.sucursal}?`} onConfirmar={crud.eliminar}
          onCerrar={() => crud.setAEliminar(null)} cargando={crud.guardando} />
      )}
    </>
  );
}
