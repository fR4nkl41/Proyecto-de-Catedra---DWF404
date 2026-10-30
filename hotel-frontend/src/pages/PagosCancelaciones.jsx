import { useEffect, useState } from 'react';
import { mensajeError } from '../api/axios';
import { pagosApi, reservasApi } from '../api/servicios';
import Cargando from '../components/Cargando';
import EstadoBadge from '../components/EstadoBadge';
import Vacio from '../components/Vacio';
import { useToast } from '../context/ToastContext';
import { dinero, fechaHora } from '../utils/formato';

export default function PagosCancelaciones() {
  const toast = useToast();
  const [pagos, setPagos] = useState(null);
  const [cancelaciones, setCancelaciones] = useState(null);

  useEffect(() => {
    pagosApi.listar().then(setPagos).catch((e) => toast.error(mensajeError(e)));
    reservasApi.cancelaciones().then(setCancelaciones).catch((e) => toast.error(mensajeError(e)));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <>
      <h1 className="page-title">Pagos y cancelaciones</h1>
      <p className="page-sub">Historial de pagos registrados y de cancelaciones con su penalización.</p>

      <div className="panel-head"><h3>Pagos</h3></div>
      {!pagos ? <Cargando /> : (
        <div className="table-wrap" style={{ marginBottom: 28 }}>
          {pagos.length === 0 ? <Vacio mensaje="Aún no hay pagos registrados." icono="coin" /> : (
            <table className="data">
              <thead><tr><th>Reserva</th><th>Huésped</th><th>Método</th><th>Fecha</th><th>Monto</th><th>Estado</th></tr></thead>
              <tbody>
                {pagos.map((p) => (
                  <tr key={p.id}>
                    <td><strong>{p.codigoReserva}</strong></td>
                    <td>{p.huesped}</td>
                    <td>{p.metodoPago}<div className="muted">{p.referencia}</div></td>
                    <td>{fechaHora(p.fechaPago)}</td>
                    <td>{dinero(p.monto)}</td>
                    <td><EstadoBadge estado={p.estado} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      <div className="panel-head"><h3>Cancelaciones</h3></div>
      {!cancelaciones ? <Cargando /> : (
        <div className="table-wrap">
          {cancelaciones.length === 0 ? <Vacio mensaje="No hay cancelaciones registradas." /> : (
            <table className="data">
              <thead><tr><th>Reserva</th><th>Huésped</th><th>Fecha</th><th>Motivo</th><th>Anticipación</th><th>Penalización</th><th>Reembolso</th></tr></thead>
              <tbody>
                {cancelaciones.map((c) => (
                  <tr key={c.id}>
                    <td><strong>{c.codigoReserva}</strong></td>
                    <td>{c.huesped}</td>
                    <td>{fechaHora(c.fechaCancelacion)}</td>
                    <td>{c.motivo}</td>
                    <td>{c.horasAnticipacion} h</td>
                    <td>{dinero(c.montoPenalizacion)}<div className="muted">{c.porcentajePenalizacion}%</div></td>
                    <td>{dinero(c.montoReembolso)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </>
  );
}
