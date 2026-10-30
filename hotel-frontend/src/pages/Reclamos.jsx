import { useEffect, useState } from 'react';
import { erroresCampos, mensajeError } from '../api/axios';
import { huespedesApi, reclamosApi, reservasApi } from '../api/servicios';
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
import { fechaHora } from '../utils/formato';

const NUEVO = { huespedId: '', reservaId: '', asunto: '', descripcion: '' };

function Responder({ reclamo, onCerrar, onGuardado }) {
  const toast = useToast();
  const [form, setForm] = useState({ estado: reclamo.estado === 'ABIERTO' ? 'EN_PROCESO' : reclamo.estado, respuesta: reclamo.respuesta || '' });
  const [errores, setErrores] = useState({});
  const [guardando, setGuardando] = useState(false);

  const enviar = async (e) => {
    e.preventDefault();
    setGuardando(true);
    try {
      await reclamosApi.responder(reclamo.id, form);
      toast.exito('La respuesta fue registrada y el huésped podrá verla.');
      onGuardado();
    } catch (err) {
      setErrores(erroresCampos(err));
      toast.error(mensajeError(err));
    } finally {
      setGuardando(false);
    }
  };

  return (
    <Modal titulo={`Responder: ${reclamo.asunto}`} onCerrar={onCerrar}
      pie={<BotonesForm onCerrar={onCerrar} guardando={guardando} formId="form-respuesta" />}>
      <div className="alert alert-info"><Icon name="chat" /><span><strong>{reclamo.huesped}:</strong> {reclamo.descripcion}</span></div>
      <form id="form-respuesta" onSubmit={enviar} noValidate>
        <Field label="Estado" error={errores.estado} required>
          <select value={form.estado} onChange={(e) => setForm({ ...form, estado: e.target.value })}>
            <option value="ABIERTO">Abierto</option>
            <option value="EN_PROCESO">En proceso</option>
            <option value="RESUELTO">Resuelto</option>
          </select>
        </Field>
        <Field label="Respuesta" error={errores.respuesta} required>
          <textarea value={form.respuesta} onChange={(e) => setForm({ ...form, respuesta: e.target.value })} maxLength={500} />
        </Field>
      </form>
    </Modal>
  );
}

export default function Reclamos() {
  const { esPersonal, esAdmin } = useAuth();
  const [estado, setEstado] = useState('');
  const crud = useCrud(reclamosApi, 'Reclamo', estado ? { estado } : undefined);
  const [reservas, setReservas] = useState([]);
  const [huespedes, setHuespedes] = useState([]);
  const [aResponder, setAResponder] = useState(null);
  const f = crud.formulario;

  useEffect(() => {
    reservasApi.listar().then(setReservas).catch(() => {});
    if (esPersonal) huespedesApi.listar().then(setHuespedes).catch(() => {});
  }, [esPersonal]);

  const cambiar = (e) => crud.setFormulario({ ...f, [e.target.name]: e.target.value });

  const enviar = (e) => {
    e.preventDefault();
    crud.guardar(f.id, {
      asunto: f.asunto,
      descripcion: f.descripcion,
      reservaId: f.reservaId ? Number(f.reservaId) : null,
      huespedId: esPersonal && f.huespedId ? Number(f.huespedId) : null,
    });
  };

  // Al personal solo se le muestran las reservas del huésped elegido
  const reservasDisponibles = esPersonal
    ? reservas.filter((r) => String(r.huespedId) === String(f?.huespedId))
    : reservas;

  return (
    <>
      <div className="page-head">
        <div>
          <h1 className="page-title">{esPersonal ? 'Reclamos' : 'Mis reclamos'}</h1>
          <p className="page-sub">{esPersonal ? 'Quejas y solicitudes de los huéspedes.' : 'Envía una queja o solicitud y da seguimiento a la respuesta del hotel.'}</p>
        </div>
        <button className="btn btn-primary" onClick={() => crud.abrir(NUEVO)}><Icon name="plus" />Nuevo reclamo</button>
      </div>

      {esPersonal && (
        <div className="toolbar">
          <select value={estado} onChange={(e) => setEstado(e.target.value)}>
            <option value="">Todos los estados</option>
            <option value="ABIERTO">Abiertos</option>
            <option value="EN_PROCESO">En proceso</option>
            <option value="RESUELTO">Resueltos</option>
          </select>
        </div>
      )}

      {!crud.lista ? <Cargando /> : (
        <div className="table-wrap">
          {crud.lista.length === 0 ? <Vacio mensaje="No hay reclamos registrados." icono="chat" /> : (
            <table className="data">
              <thead><tr><th>Asunto</th>{esPersonal && <th>Huésped</th>}<th>Reserva</th><th>Fecha</th><th>Respuesta</th><th>Estado</th><th /></tr></thead>
              <tbody>
                {crud.lista.map((r) => (
                  <tr key={r.id}>
                    <td><strong>{r.asunto}</strong><div className="muted">{r.descripcion}</div></td>
                    {esPersonal && <td>{r.huesped}</td>}
                    <td>{r.codigoReserva || '—'}</td>
                    <td>{fechaHora(r.fechaCreacion)}</td>
                    <td style={{ maxWidth: 260 }}>{r.respuesta || <span className="muted">Sin respuesta aún</span>}</td>
                    <td><EstadoBadge estado={r.estado} /></td>
                    <td className="actions">
                      {esPersonal && <button className="btn btn-outline-cool btn-sm" onClick={() => setAResponder(r)}>Responder</button>}
                      {r.estado === 'ABIERTO' && (
                        <button className="btn-icon" title="Editar"
                          onClick={() => crud.abrir({ id: r.id, huespedId: r.huespedId, reservaId: r.reservaId || '', asunto: r.asunto, descripcion: r.descripcion })}>
                          <Icon name="pencil" />
                        </button>
                      )}
                      {esAdmin && <button className="btn-icon danger" title="Eliminar" onClick={() => crud.setAEliminar(r)}><Icon name="trash" /></button>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {f && (
        <Modal titulo={f.id ? 'Editar reclamo' : 'Nuevo reclamo'} onCerrar={crud.cerrar}
          pie={<BotonesForm onCerrar={crud.cerrar} guardando={crud.guardando} formId="form-reclamo" />}>
          <form id="form-reclamo" onSubmit={enviar} noValidate>
            {esPersonal && !f.id && (
              <Field label="Huésped" error={crud.errores.huespedId} required>
                <select name="huespedId" value={f.huespedId} onChange={cambiar}>
                  <option value="">Selecciona el huésped</option>
                  {huespedes.map((h) => <option key={h.id} value={h.id}>{h.nombreCompleto} ({h.documento})</option>)}
                </select>
              </Field>
            )}
            <Field label="Reserva relacionada (opcional)" error={crud.errores.reservaId}>
              <select name="reservaId" value={f.reservaId} onChange={cambiar}>
                <option value="">Ninguna</option>
                {reservasDisponibles.map((r) => <option key={r.id} value={r.id}>{r.codigo} · {r.hotel}</option>)}
              </select>
            </Field>
            <Field label="Asunto" error={crud.errores.asunto} required><input name="asunto" value={f.asunto} onChange={cambiar} maxLength={100} /></Field>
            <Field label="Descripción" error={crud.errores.descripcion} required>
              <textarea name="descripcion" value={f.descripcion} onChange={cambiar} maxLength={500} placeholder="Describe tu queja o solicitud (mínimo 10 caracteres)" />
            </Field>
          </form>
        </Modal>
      )}

      {aResponder && (
        <Responder reclamo={aResponder} onCerrar={() => setAResponder(null)} onGuardado={() => { setAResponder(null); crud.cargar(); }} />
      )}

      {crud.aEliminar && (
        <Confirmar mensaje={`¿Eliminar el reclamo "${crud.aEliminar.asunto}"?`} onConfirmar={crud.eliminar}
          onCerrar={() => crud.setAEliminar(null)} cargando={crud.guardando} />
      )}
    </>
  );
}
