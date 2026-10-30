import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { erroresCampos, mensajeError } from '../../api/axios';
import { pagosApi } from '../../api/servicios';
import EstadoBadge from '../../components/EstadoBadge';
import Field from '../../components/Field';
import Icon from '../../components/Icon';
import Stepper from '../../components/Stepper';
import { useAuth } from '../../context/AuthContext';
import { useReserva } from '../../context/ReservaContext';
import { useToast } from '../../context/ToastContext';
import { dinero } from '../../utils/formato';

export default function Pago() {
  const { esPersonal } = useAuth();
  const reserva = useReserva();
  const toast = useToast();
  const navigate = useNavigate();
  const [metodo, setMetodo] = useState('TARJETA');
  const [tarjeta, setTarjeta] = useState({ numeroTarjeta: '', titular: '', vencimiento: '', cvv: '' });
  const [referencia, setReferencia] = useState('');
  const [errores, setErrores] = useState({});
  const [enviando, setEnviando] = useState(false);

  const r = reserva.reserva;
  if (!r) return <Navigate to="/reservar/buscar" replace />;

  const cambiarTarjeta = (e) => setTarjeta({ ...tarjeta, [e.target.name]: e.target.value });

  const pagar = async () => {
    // El huésped que elige efectivo paga al llegar: la reserva queda pendiente
    if (metodo === 'EFECTIVO' && !esPersonal) {
      reserva.actualizar({ pago: null });
      toast.info('Tu reserva quedó registrada. Paga en la recepción al llegar para confirmarla.', 'Pago en el hotel');
      navigate('/reservar/confirmacion');
      return;
    }
    setEnviando(true);
    setErrores({});
    const body = { reservaId: r.id, metodoPago: metodo };
    if (metodo === 'TARJETA') {
      Object.assign(body, { ...tarjeta, numeroTarjeta: tarjeta.numeroTarjeta.replace(/[\s-]/g, '') });
    }
    if (metodo === 'TRANSFERENCIA') body.referenciaTransferencia = referencia;
    try {
      const pago = await pagosApi.pagar(body);
      reserva.actualizar({ pago, reserva: { ...r, estado: 'CONFIRMADA', pago } });
      toast.exito('El pago se procesó correctamente.', 'Reserva confirmada');
      navigate('/reservar/confirmacion');
    } catch (err) {
      setErrores(erroresCampos(err));
      toast.error(mensajeError(err));
    } finally {
      setEnviando(false);
    }
  };

  return (
    <>
      <Stepper actual={4} />
      <h1 className="page-title">Método de pago</h1>
      <p className="page-sub">Selecciona cómo deseas realizar el pago (simulado, no se realizan cargos reales).</p>

      <Field label="Método de pago" icon="card" error={errores.metodoPago}>
        <select value={metodo} onChange={(e) => { setMetodo(e.target.value); setErrores({}); }}>
          <option value="TARJETA">Tarjeta de crédito o débito</option>
          <option value="TRANSFERENCIA">Transferencia bancaria</option>
          <option value="EFECTIVO">Efectivo (en el hotel)</option>
        </select>
      </Field>

      {metodo === 'TARJETA' && (
        <div className="panel">
          <div className="field-row">
            <Field label="Número de tarjeta" error={errores.numeroTarjeta} required>
              <input name="numeroTarjeta" value={tarjeta.numeroTarjeta} onChange={cambiarTarjeta} placeholder="4242 4242 4242 4242" maxLength={23} inputMode="numeric" />
            </Field>
            <Field label="Nombre del titular" error={errores.titular} required>
              <input name="titular" value={tarjeta.titular} onChange={cambiarTarjeta} placeholder="Como aparece en la tarjeta" />
            </Field>
          </div>
          <div className="field-row">
            <Field label="Vencimiento (MM/AA)" error={errores.vencimiento} required>
              <input name="vencimiento" value={tarjeta.vencimiento} onChange={cambiarTarjeta} placeholder="12/28" maxLength={5} />
            </Field>
            <Field label="CVV" error={errores.cvv} required>
              <input name="cvv" type="password" value={tarjeta.cvv} onChange={cambiarTarjeta} placeholder="123" maxLength={4} inputMode="numeric" />
            </Field>
          </div>
          <p className="muted">Para pruebas puedes usar la tarjeta 4242 4242 4242 4242 con cualquier fecha futura.</p>
        </div>
      )}

      {metodo === 'TRANSFERENCIA' && (
        <div className="panel">
          <p className="muted" style={{ marginBottom: 12 }}>Transfiere a la cuenta 0012-3456-789 (Banco Agrícola) a nombre de Añil PMS e ingresa el número de referencia.</p>
          <Field label="Número de referencia" error={errores.referenciaTransferencia} required>
            <input value={referencia} onChange={(e) => setReferencia(e.target.value)} placeholder="TRF-778812" />
          </Field>
        </div>
      )}

      {metodo === 'EFECTIVO' && (
        <div className="alert alert-info">
          <Icon name="info" />
          <span>
            {esPersonal
              ? 'Registra el pago en efectivo recibido en recepción. La reserva quedará confirmada.'
              : 'Pagarás en efectivo al llegar al hotel. Tu reserva quedará PENDIENTE hasta que la recepción registre el pago.'}
          </span>
        </div>
      )}

      <div className="pay-summary">
        <div className="p-row"><span className="k">Monto a pagar</span><span className="v">{dinero(r.precioTotal)}</span></div>
        <div className="p-row"><span className="k">Reserva</span><span className="v">#{r.codigo}</span></div>
        <div className="p-row"><span className="k">Estado</span><EstadoBadge estado={r.estado} /></div>
      </div>

      <div className="flex-actions">
        <button className="btn btn-outline-cool" onClick={() => navigate('/reservar/resumen')} disabled={enviando}>Volver al resumen</button>
        <button className="btn btn-success" style={{ flex: 2 }} onClick={pagar} disabled={enviando}>
          {enviando ? 'Procesando pago...' : metodo === 'EFECTIVO' && !esPersonal ? 'Pagaré en el hotel' : 'Confirmar pago'}
        </button>
      </div>
    </>
  );
}
