import { createContext, useContext, useEffect, useState } from 'react';

/**
 * Guarda los datos del proceso de reserva mientras el usuario avanza por los pasos
 * (búsqueda -> habitación -> huésped -> resumen -> pago -> confirmación).
 * Se respalda en sessionStorage para no perderlos al recargar la página.
 */
const ReservaContext = createContext(null);
const CLAVE = 'anil_reserva';

const inicial = {
  busqueda: null,      // filtros usados en el buscador
  resultados: [],      // habitaciones disponibles
  seleccion: null,     // resultado elegido (habitación + noches + subtotal)
  huesped: null,       // datos del huésped
  solicitudes: '',
  servicios: [],       // [{ servicioId, cantidad }]
  reserva: null,       // reserva creada en el servidor
  pago: null,          // pago registrado
  errores: {},         // errores de validación devueltos por la API
};

function leer() {
  try {
    return { ...inicial, ...JSON.parse(sessionStorage.getItem(CLAVE)) };
  } catch {
    return inicial;
  }
}

export function ReservaProvider({ children }) {
  const [estado, setEstado] = useState(leer);

  useEffect(() => {
    sessionStorage.setItem(CLAVE, JSON.stringify(estado));
  }, [estado]);

  const actualizar = (cambios) => setEstado((previo) => ({ ...previo, ...cambios }));
  const reiniciar = () => setEstado(inicial);

  return (
    <ReservaContext.Provider value={{ ...estado, actualizar, reiniciar }}>{children}</ReservaContext.Provider>
  );
}

export const useReserva = () => useContext(ReservaContext);
