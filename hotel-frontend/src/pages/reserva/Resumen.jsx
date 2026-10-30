import { useEffect, useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { erroresCampos, mensajeError } from '../../api/axios';
import { reservasApi, serviciosApi } from '../../api/servicios';
import Icon from '../../components/Icon';
import Stepper from '../../components/Stepper';
import { useReserva } from '../../context/ReservaContext';
import { useToast } from '../../context/ToastContext';
import { dinero, fecha } from '../../utils/formato';

export default function Resumen() {
  const reserva = useReserva();
  const toast = useToast();
  const navigate = useNavigate();
  const [extras, setExtras] = useState([]);
  const [elegidos, setElegidos] = useState(() =>
    Object.fromEntries((reserva.servicios || []).map((s) => [s.servicioId, s.cantidad]))
  );
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    serviciosApi.listar({ adicional: true }).then(setExtras).catch((e) => toast.error(mensajeError(e)));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const { seleccion, huesped, busqueda } = reserva;
  if (!seleccion || !huesped) return <Navigate to="/reservar/buscar" replace />;
  const h = seleccion.habitacion;

  const alternar = (id) => {
    const copia = { ...elegidos };
    if (copia[id]) delete copia[id];
    else copia[id] = 1;
    setElegidos(copia);
  };

  const cambiarCantidad = (id, valor) => setElegidos({ ...elegidos, [id]: Math.min(10, Math.max(1, Number(valor) || 1)) });

  // Total estimado en pantalla; el total definitivo lo calcula la API
  const totalServicios = extras.reduce((suma, s) => suma + (elegidos[s.id] ? Number(s.costo) * elegidos[s.id] : 0), 0);
  const totalEstimado = Number(seleccion.subtotalEstimado) + totalServicios;

  const confirmar = async () => {
    setEnviando(true);
    const servicios = Object.entries(elegidos).map(([servicioId, cantidad]) => ({ servicioId: Number(servicioId), cantidad }));
    const body = {
      habitacionId: h.id,
      fechaEntrada: seleccion.fechaEntrada,
      fechaSalida: seleccion.fechaSalida,
      numeroHuespedes: Number(busqueda.huespedes),
      huesped,
      servicios,
      solicitudesEspeciales: reserva.solicitudes || null,
    };
    try {
      // Si el usuario regresó desde el pago, se modifica la reserva pendiente en lugar de crear otra
      const guardada = reserva.reserva
        ? await reservasApi.actualizar(reserva.reserva.id, body)
        : await reservasApi.crear(body);
      reserva.actualizar({ reserva: guardada, servicios, errores: {} });
      toast.exito(`Reserva ${guardada.codigo} registrada. Ahora completa el pago.`, 'Reserva creada');
      navigate('/reservar/pago');
    } catch (err) {
      const errores = erroresCampos(err);
      toast.error(mensajeError(err));
      if (Object.keys(errores).some((c) => c.startsWith('huesped.') || c === 'solicitudesEspeciales')) {
        reserva.actualizar({ errores });
        navigate('/reservar/huesped');
      }
    } finally {
      setEnviando(false);
    }
  };

  return (
    <>
      <Stepper actual={3} />
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', flexWrap: 'wrap', gap: 8 }}>
        <h1 className="page-title">Resumen de la reserva</h1>
        <Link to="/reservar/huesped" className="btn-text"><Icon name="pencil" style={{ width: 14, height: 14 }} />Editar</Link>
      </div>
      <p className="page-sub">Revisa los detalles y agrega servicios adicionales antes de continuar al pago.</p>

      <div className="receipt">
        <div className="r-row"><span className="k"><Icon name="building" />Hotel</span><span className="v">{h.hotel} · {h.sucursal}</span></div>
        <div className="r-row"><span className="k"><Icon name="bed" />Habitación</span><span className="v">{h.tipo} {h.numero} ({dinero(h.precioNoche)}/noche)</span></div>
        <div className="r-row"><span className="k"><Icon name="calendar" />Entrada</span><span className="v">{fecha(seleccion.fechaEntrada)}</span></div>
        <div className="r-row"><span className="k"><Icon name="calendar" />Salida</span><span className="v">{fecha(seleccion.fechaSalida)}</span></div>
        <div className="r-row"><span className="k"><Icon name="moon" />Noches</span><span className="v">{seleccion.noches} noche(s) · {dinero(seleccion.subtotalEstimado)}</span></div>
        <div className="r-row"><span className="k"><Icon name="users" />Huéspedes</span><span className="v">{busqueda.huespedes}</span></div>
        <div className="r-row"><span className="k"><Icon name="user" />Huésped</span><span className="v">{huesped.nombreCompleto} ({huesped.tipoDocumento}: {huesped.documento})</span></div>
        <div className="r-row"><span className="k"><Icon name="phone" />Contacto</span><span className="v">{huesped.email} · {huesped.telefono}</span></div>
        {reserva.solicitudes && (
          <div className="r-row"><span className="k"><Icon name="info" />Solicitudes</span><span className="v">{reserva.solicitudes}</span></div>
        )}
      </div>

      <div className="panel">
        <div className="panel-head">
          <h3>Servicios adicionales</h3>
          <span className="hint">Opcionales, se suman al total</span>
        </div>
        {extras.map((s) => (
          <div className="extra-row" key={s.id}>
            <input type="checkbox" checked={!!elegidos[s.id]} onChange={() => alternar(s.id)} id={`extra-${s.id}`} />
            <label className="info" htmlFor={`extra-${s.id}`}>
              <div className="n">{s.nombre} (+{dinero(s.costo)})</div>
              <div className="d">{s.descripcion}</div>
            </label>
            {elegidos[s.id] && (
              <input type="number" min="1" max="10" value={elegidos[s.id]} onChange={(e) => cambiarCantidad(s.id, e.target.value)} title="Cantidad" />
            )}
          </div>
        ))}
      </div>

      <div className="total-box">
        <span className="lbl">Total a pagar{totalServicios > 0 ? ` (incluye ${dinero(totalServicios)} en servicios)` : ''}</span>
        <span className="amount">{dinero(totalEstimado)}</span>
      </div>

      <button className="btn btn-success btn-block" style={{ marginBottom: 10 }} onClick={confirmar} disabled={enviando}>
        {enviando ? 'Registrando reserva...' : 'Confirmar e ir a pagar'}
      </button>
      <Link to="/reservar/huesped" className="btn btn-outline-cool btn-block"><Icon name="arrow-left" />Volver</Link>
    </>
  );
}
