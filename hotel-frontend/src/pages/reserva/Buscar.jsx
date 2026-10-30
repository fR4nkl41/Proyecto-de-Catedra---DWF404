import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { erroresCampos, mensajeError } from '../../api/axios';
import { habitacionesApi, tiposApi } from '../../api/servicios';
import Field from '../../components/Field';
import Icon from '../../components/Icon';
import { useReserva } from '../../context/ReservaContext';
import { useToast } from '../../context/ToastContext';
import { hoyISO } from '../../utils/formato';

export default function Buscar() {
  const reserva = useReserva();
  const toast = useToast();
  const navigate = useNavigate();
  const [tipos, setTipos] = useState([]);
  const [errores, setErrores] = useState({});
  const [cargando, setCargando] = useState(false);
  const [form, setForm] = useState(
    reserva.busqueda || { ciudad: 'San Salvador', fechaEntrada: hoyISO(1), fechaSalida: hoyISO(4), huespedes: 2, tipoId: '' }
  );

  useEffect(() => {
    tiposApi.listar().then(setTipos).catch(() => {});
  }, []);

  const cambiar = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const buscar = async (e) => {
    e.preventDefault();
    setCargando(true);
    setErrores({});
    const filtros = { ...form, tipoId: form.tipoId || undefined, ciudad: form.ciudad.trim() || undefined };
    try {
      const resultados = await habitacionesApi.disponibles(filtros);
      reserva.actualizar({
        busqueda: form, resultados, seleccion: null, reserva: null, pago: null, servicios: [], errores: {},
      });
      navigate('/reservar/resultados');
    } catch (err) {
      setErrores(erroresCampos(err));
      toast.error(mensajeError(err));
    } finally {
      setCargando(false);
    }
  };

  return (
    <>
      <h1 className="page-title">Buscar habitaciones</h1>
      <p className="page-sub">Encuentra la habitación ideal para tu huésped.</p>

      <form className="panel" onSubmit={buscar} noValidate>
        <div className="field-row">
          <Field label="Ciudad o ubicación" icon="pin" error={errores.ciudad}>
            <input name="ciudad" value={form.ciudad} onChange={cambiar} placeholder="Todas las ciudades" />
          </Field>
          <Field label="Fecha de entrada" icon="calendar" error={errores.fechaEntrada} required>
            <input type="date" name="fechaEntrada" value={form.fechaEntrada} min={hoyISO()} onChange={cambiar} />
          </Field>
          <Field label="Fecha de salida" icon="calendar" error={errores.fechaSalida} required>
            <input type="date" name="fechaSalida" value={form.fechaSalida} min={form.fechaEntrada} onChange={cambiar} />
          </Field>
        </div>
        <div className="field-row">
          <Field label="Huéspedes" icon="users" error={errores.huespedes} required>
            <input type="number" name="huespedes" value={form.huespedes} min="1" max="10" onChange={cambiar} />
          </Field>
          <Field label="Tipo de habitación" icon="bed" error={errores.tipoId}>
            <select name="tipoId" value={form.tipoId} onChange={cambiar}>
              <option value="">Cualquiera</option>
              {tipos.map((t) => (
                <option key={t.id} value={t.id}>{t.nombre} (hasta {t.capacidadMaxima} pers.)</option>
              ))}
            </select>
          </Field>
          <div className="field" style={{ display: 'flex', alignItems: 'flex-end' }}>
            <button className="btn btn-primary btn-block" disabled={cargando}>
              <Icon name="search" />{cargando ? 'Buscando...' : 'Buscar'}
            </button>
          </div>
        </div>
      </form>

      <Link to="/" className="btn btn-outline-cool"><Icon name="arrow-left" />Volver al panel</Link>
    </>
  );
}
