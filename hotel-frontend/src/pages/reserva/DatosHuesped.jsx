import { useEffect, useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { mensajeError } from '../../api/axios';
import { huespedesApi } from '../../api/servicios';
import Field from '../../components/Field';
import Icon from '../../components/Icon';
import Stepper from '../../components/Stepper';
import { useAuth } from '../../context/AuthContext';
import { useReserva } from '../../context/ReservaContext';
import { useToast } from '../../context/ToastContext';

const VACIO = { nombreCompleto: '', fechaNacimiento: '', tipoDocumento: 'DUI', documento: '', telefono: '', email: '' };
const OBLIGATORIOS = ['nombreCompleto', 'fechaNacimiento', 'documento', 'telefono', 'email'];

/** Convierte { "huesped.documento": "..." } en { documento: "..." }. */
function erroresDeHuesped(errores = {}) {
  const resultado = {};
  Object.entries(errores).forEach(([campo, mensaje]) => {
    if (campo.startsWith('huesped.')) resultado[campo.substring(8)] = mensaje;
    if (campo === 'solicitudesEspeciales') resultado.solicitudes = mensaje;
  });
  return resultado;
}

export default function DatosHuesped() {
  const { usuario, esPersonal } = useAuth();
  const reserva = useReserva();
  const toast = useToast();
  const navigate = useNavigate();
  const [form, setForm] = useState(reserva.huesped || VACIO);
  const [solicitudes, setSolicitudes] = useState(reserva.solicitudes || '');
  const [errores, setErrores] = useState(erroresDeHuesped(reserva.errores));
  const [buscarDoc, setBuscarDoc] = useState('');

  useEffect(() => {
    // El huésped ve sus datos guardados; si es su primera reserva se usan los de su cuenta
    if (!esPersonal && !reserva.huesped) {
      huespedesApi
        .miPerfil()
        .then((p) => setForm({ ...VACIO, ...p }))
        .catch(() => setForm({ ...VACIO, nombreCompleto: usuario.nombre, email: usuario.email }));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (!reserva.seleccion) return <Navigate to="/reservar/buscar" replace />;

  const cambiar = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const buscarExistente = async () => {
    if (!buscarDoc.trim()) return;
    try {
      const lista = await huespedesApi.listar({ buscar: buscarDoc.trim() });
      if (lista.length === 0) {
        toast.info('No se encontró ningún huésped. Completa los datos para registrarlo.', 'Huésped nuevo');
      } else {
        setForm({ ...VACIO, ...lista[0] });
        toast.exito(`Se cargaron los datos de ${lista[0].nombreCompleto}.`, 'Huésped encontrado');
      }
    } catch (err) {
      toast.error(mensajeError(err));
    }
  };

  const continuar = (e) => {
    e.preventDefault();
    const faltantes = {};
    OBLIGATORIOS.forEach((c) => {
      if (!String(form[c] || '').trim()) faltantes[c] = 'Este campo es obligatorio';
    });
    if (Object.keys(faltantes).length) {
      setErrores(faltantes);
      toast.error('Completa los campos obligatorios marcados con *.');
      return;
    }
    const { nombreCompleto, fechaNacimiento, tipoDocumento, documento, telefono, email } = form;
    reserva.actualizar({
      huesped: { nombreCompleto, fechaNacimiento, tipoDocumento, documento: documento.trim().toUpperCase(), telefono, email },
      solicitudes,
      errores: {},
    });
    navigate('/reservar/resumen');
  };

  return (
    <>
      <Stepper actual={2} />
      <h1 className="page-title">Datos del huésped</h1>
      <p className="page-sub">Completa la información para confirmar la reserva. Los campos con * son obligatorios.</p>

      {esPersonal && (
        <div className="panel">
          <div className="field-row" style={{ alignItems: 'flex-end' }}>
            <Field label="¿El huésped ya está registrado? Búscalo por documento o nombre" icon="search">
              <input value={buscarDoc} onChange={(e) => setBuscarDoc(e.target.value)} placeholder="01234567-8" />
            </Field>
            <div className="field" style={{ flex: '0 0 auto', minWidth: 0 }}>
              <button type="button" className="btn btn-outline-cool" onClick={buscarExistente}><Icon name="search" />Buscar</button>
            </div>
          </div>
        </div>
      )}

      <form onSubmit={continuar} noValidate>
        <div className="field-row">
          <Field label="Nombre completo" icon="user" error={errores.nombreCompleto} required>
            <input name="nombreCompleto" value={form.nombreCompleto} onChange={cambiar} placeholder="Carlos Pérez" />
          </Field>
          <Field label="Fecha de nacimiento" icon="calendar" error={errores.fechaNacimiento} required>
            <input type="date" name="fechaNacimiento" value={form.fechaNacimiento} onChange={cambiar} />
          </Field>
        </div>
        <div className="field-row">
          <Field label="Tipo de documento" error={errores.tipoDocumento} required>
            <select name="tipoDocumento" value={form.tipoDocumento} onChange={cambiar}>
              <option value="DUI">DUI</option>
              <option value="PASAPORTE">Pasaporte</option>
            </select>
          </Field>
          <Field label="Número de documento" error={errores.documento} required>
            <input name="documento" value={form.documento} onChange={cambiar}
              placeholder={form.tipoDocumento === 'DUI' ? '01234567-8' : 'A12345678'} />
          </Field>
          <Field label="Teléfono" icon="phone" error={errores.telefono} required>
            <input type="tel" name="telefono" value={form.telefono} onChange={cambiar} placeholder="7777-8888" />
          </Field>
        </div>
        <Field label="Correo electrónico" icon="mail" error={errores.email} required>
          <input type="email" name="email" value={form.email} onChange={cambiar} placeholder="carlos@mail.com" />
        </Field>
        <Field label="Solicitudes especiales (opcional)" error={errores.solicitudes}>
          <input value={solicitudes} onChange={(e) => setSolicitudes(e.target.value)} placeholder="Ej: cuna, habitación silenciosa..." maxLength={250} />
        </Field>

        <button className="btn btn-primary btn-block" style={{ marginBottom: 10 }}>Continuar al resumen</button>
      </form>
      <Link to="/reservar/detalle" className="btn btn-outline-cool btn-block"><Icon name="arrow-left" />Volver al detalle</Link>
    </>
  );
}
