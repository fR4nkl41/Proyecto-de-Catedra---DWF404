import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { erroresCampos, mensajeError } from '../api/axios';
import { reservasApi } from '../api/servicios';
import Cargando from '../components/Cargando';
import Confirmar from '../components/Confirmar';
import EstadoBadge from '../components/EstadoBadge';
import Field from '../components/Field';
import Icon from '../components/Icon';
import Modal from '../components/Modal';
import Vacio from '../components/Vacio';
import { useAuth } from '../context/AuthContext';
import { useReserva } from '../context/ReservaContext';
import { useToast } from '../context/ToastContext';
import { dinero, fecha, fechaHora } from '../utils/formato';

const ESTADOS = ['PENDIENTE', 'CONFIRMADA', 'EN_CURSO', 'FINALIZADA', 'CANCELADA'];

function DetalleReserva({ r, onCerrar }) {
  return (
    <Modal titulo={`Reserva #${r.codigo}`} onCerrar={onCerrar} ancho>
      <div className="detail-grid" style={{ marginBottom: 14 }}>
        <div className="d-row"><span className="k">Estado</span><EstadoBadge estado={r.estado} /></div>
        <div className="d-row"><span className="k">Registrada</span><span className="v">{fechaHora(r.fechaReserva)}</span></div>
        <div className="d-row"><span className="k">Huésped</span><span className="v">{r.huespedNombre}</span></div>
        <div className="d-row"><span className="k">Documento</span><span className="v">{r.huespedDocumento}</span></div>
        <div className="d-row"><span className="k">Contacto</span><span className="v">{r.huespedTelefono}</span></div>
        <div className="d-row"><span className="k">Correo</span><span className="v">{r.huespedEmail}</span></div>
        <div className="d-row"><span className="k">Hotel</span><span className="v">{r.hotel}</span></div>
        <div className="d-row"><span className="k">Sucursal</span><span className="v">{r.sucursal} ({r.ciudad})</span></div>
        <div className="d-row"><span className="k">Habitación</span><span className="v">{r.tipoHabitacion} {r.habitacionNumero}</span></div>
        <div className="d-row"><span className="k">Huéspedes</span><span className="v">{r.numeroHuespedes}</span></div>
        <div className="d-row"><span className="k">Entrada</span><span className="v">{fecha(r.fechaEntrada)}</span></div>
        <div className="d-row"><span className="k">Salida</span><span className="v">{fecha(r.fechaSalida)}</span></div>
      </div>

      <div className="receipt">
        <div className="r-row"><span className="k">{r.numeroNoches} noche(s) × {dinero(r.precioNoche)}</span><span className="v">{dinero(r.subtotalHabitacion)}</span></div>
        {r.servicios.map((s) => (
          <div className="r-row" key={s.servicioId}><span className="k">{s.nombre} × {s.cantidad}</span><span className="v">{dinero(s.subtotal)}</span></div>
        ))}
        <div className="r-row"><span className="k"><strong>Total</strong></span><span className="v">{dinero(r.precioTotal)}</span></div>
        {r.pago && (
          <div className="r-row"><span className="k">Pago ({r.pago.metodoPago.toLowerCase()} · {r.pago.referencia})</span><EstadoBadge estado={r.pago.estado} /></div>
        )}
        {r.cancelacion && (
          <div className="r-row">
            <span className="k">Cancelada el {fechaHora(r.cancelacion.fechaCancelacion)}: {r.cancelacion.motivo}</span>
            <span className="v">Penalización {dinero(r.cancelacion.montoPenalizacion)} · Reembolso {dinero(r.cancelacion.montoReembolso)}</span>
          </div>
        )}
      </div>
      {r.solicitudesEspeciales && <p className="muted">Solicitudes especiales: {r.solicitudesEspeciales}</p>}
    </Modal>
  );
}

function CancelarReserva({ r, onCerrar, onCancelada }) {
  const toast = useToast();
  const [simulacion, setSimulacion] = useState(null);
  const [motivo, setMotivo] = useState('');
  const [errores, setErrores] = useState({});
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    reservasApi.simularCancelacion(r.id).then(setSimulacion).catch((e) => {
      toast.error(mensajeError(e));
      onCerrar();
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const cancelar = async () => {
    setEnviando(true);
    try {
      const c = await reservasApi.cancelar(r.id, motivo);
      toast.exito(`Reserva ${r.codigo} cancelada. Reembolso: ${dinero(c.montoReembolso)}.`, 'Cancelación registrada');
      onCancelada();
    } catch (err) {
      setErrores(erroresCampos(err));
      toast.error(mensajeError(err));
    } finally {
      setEnviando(false);
    }
  };

  return (
    <Modal
      titulo={`Cancelar reserva #${r.codigo}`}
      onCerrar={onCerrar}
      pie={
        <>
          <button className="btn btn-outline-cool" onClick={onCerrar}>Volver</button>
          <button className="btn btn-danger" onClick={cancelar} disabled={!simulacion || enviando}>
            {enviando ? 'Cancelando...' : 'Confirmar cancelación'}
          </button>
        </>
      }
    >
      {!simulacion ? (
        <Cargando />
      ) : (
        <>
          <div className="policy-note"><Icon name="alert" /><span>{simulacion.politica}</span></div>
          <div className="receipt">
            <div className="r-row"><span className="k">Horas de anticipación</span><span className="v">{simulacion.horasAnticipacion} h</span></div>
            <div className="r-row"><span className="k">Penalización ({simulacion.porcentajePenalizacion}%)</span><span className="v">{dinero(simulacion.montoPenalizacion)}</span></div>
            <div className="r-row"><span className="k">Monto a reembolsar</span><span className="v">{dinero(simulacion.montoReembolso)}</span></div>
          </div>
          <Field label="Motivo de la cancelación" error={errores.motivo} required>
            <textarea value={motivo} onChange={(e) => setMotivo(e.target.value)} placeholder="Ej: Cambio de planes de viaje" maxLength={250} />
          </Field>
        </>
      )}
    </Modal>
  );
}

export default function Reservas() {
  const { esPersonal, esAdmin } = useAuth();
  const reservaCtx = useReserva();
  const toast = useToast();
  const navigate = useNavigate();
  const [reservas, setReservas] = useState(null);
  const [estado, setEstado] = useState('');
  const [texto, setTexto] = useState('');
  const [verDetalle, setVerDetalle] = useState(null);
  const [aCancelar, setACancelar] = useState(null);
  const [aEliminar, setAEliminar] = useState(null);
  const [procesando, setProcesando] = useState(false);

  const cargar = () => {
    reservasApi
      .listar(estado ? { estado } : undefined)
      .then(setReservas)
      .catch((e) => toast.error(mensajeError(e)));
  };

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(cargar, [estado]);

  const accion = async (promesa, mensaje) => {
    setProcesando(true);
    try {
      await promesa;
      toast.exito(mensaje);
      cargar();
    } catch (err) {
      toast.error(mensajeError(err));
    } finally {
      setProcesando(false);
    }
  };

  const irAPagar = (r) => {
    reservaCtx.reiniciar();
    reservaCtx.actualizar({ reserva: r });
    navigate('/reservar/pago');
  };

  const eliminar = async () => {
    await accion(reservasApi.eliminar(aEliminar.id), `Reserva ${aEliminar.codigo} eliminada.`);
    setAEliminar(null);
  };

  const filtro = texto.trim().toLowerCase();
  const visibles = (reservas || []).filter(
    (r) => !filtro || [r.codigo, r.huespedNombre, r.hotel, r.huespedDocumento].some((v) => v?.toLowerCase().includes(filtro))
  );

  return (
    <>
      <div className="page-head">
        <div>
          <h1 className="page-title">{esPersonal ? 'Reservas' : 'Mis reservas'}</h1>
          <p className="page-sub">Consulta, paga, cancela o registra la llegada y salida de los huéspedes.</p>
        </div>
        <Link to="/reservar/buscar" className="btn btn-primary"><Icon name="plus" />Nueva reserva</Link>
      </div>

      <div className="toolbar">
        <select value={estado} onChange={(e) => setEstado(e.target.value)}>
          <option value="">Todos los estados</option>
          {ESTADOS.map((e) => <option key={e} value={e}>{e.replace('_', ' ')}</option>)}
        </select>
        <input value={texto} onChange={(e) => setTexto(e.target.value)} placeholder="Buscar por código, huésped u hotel" />
      </div>

      {!reservas ? (
        <Cargando />
      ) : (
        <div className="table-wrap">
          {visibles.length === 0 ? (
            <Vacio mensaje="No hay reservas con esos filtros." icono="calendar" />
          ) : (
            <table className="data">
              <thead>
                <tr>
                  <th>Código</th>
                  {esPersonal && <th>Huésped</th>}
                  <th>Hotel / habitación</th>
                  <th>Fechas</th>
                  <th>Total</th>
                  <th>Estado</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {visibles.map((r) => (
                  <tr key={r.id}>
                    <td><strong>{r.codigo}</strong></td>
                    {esPersonal && <td>{r.huespedNombre}<div className="muted">{r.huespedDocumento}</div></td>}
                    <td>{r.hotel}<div className="muted">{r.tipoHabitacion} {r.habitacionNumero} · {r.sucursal}</div></td>
                    <td>{fecha(r.fechaEntrada)} – {fecha(r.fechaSalida)}<div className="muted">{r.numeroNoches} noche(s)</div></td>
                    <td>{dinero(r.precioTotal)}</td>
                    <td><EstadoBadge estado={r.estado} /></td>
                    <td className="actions">
                      <button className="btn-icon" title="Ver detalle" onClick={() => setVerDetalle(r)}><Icon name="eye" /></button>
                      {r.estado === 'PENDIENTE' && (
                        <button className="btn btn-success btn-sm" onClick={() => irAPagar(r)}>Pagar</button>
                      )}
                      {esPersonal && r.estado === 'CONFIRMADA' && (
                        <button className="btn btn-primary btn-sm" disabled={procesando}
                          onClick={() => accion(reservasApi.checkIn(r.id), `Check-in registrado para ${r.huespedNombre}.`)}>Check-in</button>
                      )}
                      {esPersonal && r.estado === 'EN_CURSO' && (
                        <button className="btn btn-primary btn-sm" disabled={procesando}
                          onClick={() => accion(reservasApi.checkOut(r.id), `Check-out registrado. La habitación ${r.habitacionNumero} quedó disponible.`)}>Check-out</button>
                      )}
                      {['PENDIENTE', 'CONFIRMADA'].includes(r.estado) && (
                        <button className="btn btn-outline-cool btn-sm" onClick={() => setACancelar(r)}>Cancelar</button>
                      )}
                      {esAdmin && (r.estado === 'CANCELADA' || (r.estado === 'PENDIENTE' && !r.pago)) && (
                        <button className="btn-icon danger" title="Eliminar" onClick={() => setAEliminar(r)}><Icon name="trash" /></button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {verDetalle && <DetalleReserva r={verDetalle} onCerrar={() => setVerDetalle(null)} />}
      {aCancelar && (
        <CancelarReserva r={aCancelar} onCerrar={() => setACancelar(null)} onCancelada={() => { setACancelar(null); cargar(); }} />
      )}
      {aEliminar && (
        <Confirmar
          mensaje={`¿Eliminar definitivamente la reserva ${aEliminar.codigo}? Esta acción no se puede deshacer.`}
          onConfirmar={eliminar}
          onCerrar={() => setAEliminar(null)}
          cargando={procesando}
        />
      )}
    </>
  );
}
