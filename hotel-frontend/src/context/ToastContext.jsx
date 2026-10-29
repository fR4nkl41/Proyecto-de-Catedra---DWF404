import { createContext, useCallback, useContext, useState } from 'react';
import Icon from '../components/Icon';

const ToastContext = createContext(null);

const ICONOS = { success: 'check', error: 'alert', info: 'info' };

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const mostrar = useCallback((tipo, mensaje, titulo) => {
    const id = Date.now() + Math.random();
    setToasts((lista) => [...lista, { id, tipo, mensaje, titulo }]);
    setTimeout(() => setToasts((lista) => lista.filter((t) => t.id !== id)), tipo === 'error' ? 6500 : 4000);
  }, []);

  const toast = {
    exito: (mensaje, titulo = 'Listo') => mostrar('success', mensaje, titulo),
    error: (mensaje, titulo = 'No se pudo completar') => mostrar('error', mensaje, titulo),
    info: (mensaje, titulo) => mostrar('info', mensaje, titulo),
  };

  return (
    <ToastContext.Provider value={toast}>
      {children}
      <div className="toast-area" role="status" aria-live="polite">
        {toasts.map((t) => (
          <div key={t.id} className={`toast ${t.tipo}`}>
            <Icon name={ICONOS[t.tipo]} />
            <div>
              {t.titulo && <strong>{t.titulo}</strong>}
              {t.mensaje}
            </div>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export const useToast = () => useContext(ToastContext);
