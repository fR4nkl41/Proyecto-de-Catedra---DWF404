import { Fragment } from 'react';
import Icon from './Icon';

const PASOS = ['Habitación', 'Huésped', 'Resumen', 'Pago'];

/** Indicador de pasos del proceso de reserva (paso actual: 1 a 4). */
export default function Stepper({ actual }) {
  return (
    <div className="stepper">
      {PASOS.map((nombre, i) => {
        const numero = i + 1;
        const estado = numero < actual ? 'done' : numero === actual ? 'now' : 'next';
        return (
          <Fragment key={nombre}>
            {i > 0 && <div className={`step-line ${numero <= actual ? 'done' : ''}`} />}
            <div className={`step ${estado}`}>
              <div className="dot">
                {estado === 'done' ? (
                  <Icon name="check-plain" style={{ width: 13, height: 13, stroke: '#fff' }} />
                ) : (
                  numero
                )}
              </div>
              <div className="lbl">{nombre}</div>
            </div>
          </Fragment>
        );
      })}
    </div>
  );
}
