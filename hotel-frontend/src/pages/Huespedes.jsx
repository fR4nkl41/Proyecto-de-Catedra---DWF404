import { useState } from 'react';
import { huespedesApi } from '../api/servicios';
import BotonesForm from '../components/BotonesForm';
import Cargando from '../components/Cargando';
import Confirmar from '../components/Confirmar';
import Field from '../components/Field';
import Icon from '../components/Icon';
import Modal from '../components/Modal';
import Vacio from '../components/Vacio';
import { useAuth } from '../context/AuthContext';
import useCrud from '../hooks/useCrud';
import { fecha } from '../utils/formato';

const NUEVO = { nombreCompleto: '', fechaNacimiento: '', tipoDocumento: 'DUI', documento: '', telefono: '', email: '' };

export default function Huespedes() {
  const { esAdmin } = useAuth();
  const [texto, setTexto] = useState('');
  const [buscar, setBuscar] = useState('');
  const crud = useCrud(huespedesApi, 'Huésped', buscar ? { buscar } : undefined);
  const f = crud.formulario;
  const cambiar = (e) => crud.setFormulario({ ...f, [e.target.name]: e.target.value });

  const enviar = (e) => {
    e.preventDefault();
    const { id, nombreCompleto, fechaNacimiento, tipoDocumento, documento, telefono, email } = f;
    crud.guardar(id, { nombreCompleto, fechaNacimiento: fechaNacimiento || null, tipoDocumento, documento, telefono, email });
  };

  return (
    <>
      <div className="page-head">
        <div>
          <h1 className="page-title">Huéspedes</h1>
          <p className="page-sub">Registro de huéspedes. El documento (DUI o pasaporte) es único.</p>
        </div>
        <button className="btn btn-primary" onClick={() => crud.abrir(NUEVO)}><Icon name="plus" />Registrar huésped</button>
      </div>

      <form className="toolbar" onSubmit={(e) => { e.preventDefault(); setBuscar(texto.trim()); }}>
        <input value={texto} onChange={(e) => setTexto(e.target.value)} placeholder="Nombre, documento o correo" />
        <button className="btn btn-outline-cool btn-sm"><Icon name="search" />Buscar</button>
      </form>

      {!crud.lista ? <Cargando /> : (
        <div className="table-wrap">
          {crud.lista.length === 0 ? <Vacio mensaje="No se encontraron huéspedes." icono="users" /> : (
            <table className="data">
              <thead><tr><th>Nombre</th><th>Documento</th><th>Nacimiento</th><th>Contacto</th><th>Cuenta</th><th /></tr></thead>
              <tbody>
                {crud.lista.map((h) => (
                  <tr key={h.id}>
                    <td><strong>{h.nombreCompleto}</strong></td>
                    <td>{h.documento}<div className="muted">{h.tipoDocumento}</div></td>
                    <td>{fecha(h.fechaNacimiento)}</td>
                    <td>{h.telefono}<div className="muted">{h.email}</div></td>
                    <td>{h.usuarioId ? <span className="badge badge-anil">En línea</span> : <span className="badge badge-gray">Recepción</span>}</td>
                    <td className="actions">
                      <button className="btn-icon" title="Editar" onClick={() => crud.abrir({ ...h })}><Icon name="pencil" /></button>
                      {esAdmin && <button className="btn-icon danger" title="Eliminar" onClick={() => crud.setAEliminar(h)}><Icon name="trash" /></button>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {f && (
        <Modal titulo={f.id ? 'Editar huésped' : 'Registrar huésped'} onCerrar={crud.cerrar}
          pie={<BotonesForm onCerrar={crud.cerrar} guardando={crud.guardando} formId="form-huesped" />}>
          <form id="form-huesped" onSubmit={enviar} noValidate>
            <Field label="Nombre completo" error={crud.errores.nombreCompleto} required><input name="nombreCompleto" value={f.nombreCompleto} onChange={cambiar} /></Field>
            <div className="field-row">
              <Field label="Fecha de nacimiento" error={crud.errores.fechaNacimiento} required><input type="date" name="fechaNacimiento" value={f.fechaNacimiento} onChange={cambiar} /></Field>
              <Field label="Tipo de documento" error={crud.errores.tipoDocumento} required>
                <select name="tipoDocumento" value={f.tipoDocumento} onChange={cambiar}>
                  <option value="DUI">DUI</option>
                  <option value="PASAPORTE">Pasaporte</option>
                </select>
              </Field>
            </div>
            <div className="field-row">
              <Field label="Documento" error={crud.errores.documento} required><input name="documento" value={f.documento} onChange={cambiar} placeholder="01234567-8" /></Field>
              <Field label="Teléfono" error={crud.errores.telefono} required><input name="telefono" value={f.telefono} onChange={cambiar} placeholder="7777-8888" /></Field>
            </div>
            <Field label="Correo electrónico" error={crud.errores.email} required><input type="email" name="email" value={f.email} onChange={cambiar} /></Field>
          </form>
        </Modal>
      )}

      {crud.aEliminar && (
        <Confirmar mensaje={`¿Eliminar al huésped ${crud.aEliminar.nombreCompleto}?`} onConfirmar={crud.eliminar}
          onCerrar={() => crud.setAEliminar(null)} cargando={crud.guardando} />
      )}
    </>
  );
}
