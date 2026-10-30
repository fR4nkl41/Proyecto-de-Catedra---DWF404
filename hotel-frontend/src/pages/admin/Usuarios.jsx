import { useState } from 'react';
import { mensajeError } from '../../api/axios';
import { usuariosApi } from '../../api/servicios';
import BotonesForm from '../../components/BotonesForm';
import Cargando from '../../components/Cargando';
import Confirmar from '../../components/Confirmar';
import EstadoBadge from '../../components/EstadoBadge';
import Field from '../../components/Field';
import Icon from '../../components/Icon';
import Modal from '../../components/Modal';
import Vacio from '../../components/Vacio';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import useCrud from '../../hooks/useCrud';
import { fecha, ROLES } from '../../utils/formato';

const NUEVO = { nombre: '', email: '', password: '', rol: 'RECEPCIONISTA', activo: 'true' };

export default function Usuarios() {
  const { usuario: yo } = useAuth();
  const toast = useToast();
  const [rol, setRol] = useState('');
  const crud = useCrud(usuariosApi, 'Usuario', rol ? { rol } : undefined);
  const f = crud.formulario;
  const cambiar = (e) => crud.setFormulario({ ...f, [e.target.name]: e.target.value });

  const enviar = (e) => {
    e.preventDefault();
    const { id, fechaCreacion, ...body } = f;
    crud.guardar(id, { ...body, password: body.password || null, activo: body.activo === 'true' });
  };

  const alternarEstado = async (u) => {
    try {
      await usuariosApi.cambiarEstado(u.id, !u.activo);
      toast.exito(`El usuario ${u.nombre} fue ${u.activo ? 'desactivado' : 'activado'}.`);
      crud.cargar();
    } catch (err) {
      toast.error(mensajeError(err));
    }
  };

  return (
    <>
      <div className="page-head">
        <div>
          <h1 className="page-title">Usuarios</h1>
          <p className="page-sub">Crea, edita, activa o desactiva las cuentas de acceso al sistema.</p>
        </div>
        <button className="btn btn-primary" onClick={() => crud.abrir(NUEVO)}><Icon name="plus" />Nuevo usuario</button>
      </div>

      <div className="toolbar">
        <select value={rol} onChange={(e) => setRol(e.target.value)}>
          <option value="">Todos los roles</option>
          {Object.entries(ROLES).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
        </select>
      </div>

      {!crud.lista ? <Cargando /> : (
        <div className="table-wrap">
          {crud.lista.length === 0 ? <Vacio mensaje="No hay usuarios con ese rol." icono="users" /> : (
            <table className="data">
              <thead><tr><th>Nombre</th><th>Rol</th><th>Creado</th><th>Estado</th><th /></tr></thead>
              <tbody>
                {crud.lista.map((u) => (
                  <tr key={u.id}>
                    <td><strong>{u.nombre}</strong><div className="muted">{u.email}</div></td>
                    <td>{ROLES[u.rol]}</td>
                    <td>{fecha(u.fechaCreacion)}</td>
                    <td><EstadoBadge estado={u.activo ? 'ACTIVO' : 'INACTIVO'} /></td>
                    <td className="actions">
                      {u.id !== yo.id && (
                        <button className="btn btn-outline-cool btn-sm" onClick={() => alternarEstado(u)}>
                          <Icon name="power" />{u.activo ? 'Desactivar' : 'Activar'}
                        </button>
                      )}
                      <button className="btn-icon" title="Editar"
                        onClick={() => crud.abrir({ ...u, password: '', activo: String(u.activo) })}><Icon name="pencil" /></button>
                      {u.id !== yo.id && (
                        <button className="btn-icon danger" title="Eliminar" onClick={() => crud.setAEliminar(u)}><Icon name="trash" /></button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {f && (
        <Modal titulo={f.id ? 'Editar usuario' : 'Nuevo usuario'} onCerrar={crud.cerrar}
          pie={<BotonesForm onCerrar={crud.cerrar} guardando={crud.guardando} formId="form-usuario" />}>
          <form id="form-usuario" onSubmit={enviar} noValidate>
            <Field label="Nombre completo" error={crud.errores.nombre} required><input name="nombre" value={f.nombre} onChange={cambiar} /></Field>
            <Field label="Correo electrónico" error={crud.errores.email} required><input type="email" name="email" value={f.email} onChange={cambiar} /></Field>
            <Field label={f.id ? 'Nueva contraseña (déjala vacía para no cambiarla)' : 'Contraseña'} error={crud.errores.password} required={!f.id}>
              <input type="password" name="password" value={f.password} onChange={cambiar} placeholder="Mínimo 6, letras y números" />
            </Field>
            <div className="field-row">
              <Field label="Rol" error={crud.errores.rol} required>
                <select name="rol" value={f.rol} onChange={cambiar}>
                  {Object.entries(ROLES).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
                </select>
              </Field>
              <Field label="Estado" error={crud.errores.activo}>
                <select name="activo" value={f.activo} onChange={cambiar}>
                  <option value="true">Activo</option>
                  <option value="false">Inactivo</option>
                </select>
              </Field>
            </div>
          </form>
        </Modal>
      )}

      {crud.aEliminar && (
        <Confirmar mensaje={`¿Eliminar al usuario ${crud.aEliminar.nombre}? Si tiene reservas asociadas, desactívalo en su lugar.`}
          onConfirmar={crud.eliminar} onCerrar={() => crud.setAEliminar(null)} cargando={crud.guardando} />
      )}
    </>
  );
}
