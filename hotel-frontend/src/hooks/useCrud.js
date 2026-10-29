import { useCallback, useEffect, useState } from 'react';
import { erroresCampos, mensajeError } from '../api/axios';
import { useToast } from '../context/ToastContext';

/**
 * Lógica común de las pantallas de administración:
 * listar, abrir formulario (crear/editar), guardar y eliminar.
 */
export default function useCrud(api, nombre, params) {
  const toast = useToast();
  const [lista, setLista] = useState(null);
  const [formulario, setFormulario] = useState(null); // null = cerrado, {id?, ...datos}
  const [errores, setErrores] = useState({});
  const [guardando, setGuardando] = useState(false);
  const [aEliminar, setAEliminar] = useState(null);

  const clave = JSON.stringify(params || {});

  const cargar = useCallback(() => {
    api.listar(params).then(setLista).catch((e) => toast.error(mensajeError(e)));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [clave]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const abrir = (datos) => {
    setErrores({});
    setFormulario(datos);
  };

  const cerrar = () => setFormulario(null);

  const guardar = async (id, body) => {
    setGuardando(true);
    setErrores({});
    try {
      if (id) await api.actualizar(id, body);
      else await api.crear(body);
      toast.exito(`${nombre} ${id ? 'actualizado' : 'creado'} correctamente.`);
      setFormulario(null);
      cargar();
    } catch (err) {
      setErrores(erroresCampos(err));
      toast.error(mensajeError(err));
    } finally {
      setGuardando(false);
    }
  };

  const eliminar = async () => {
    setGuardando(true);
    try {
      const r = await api.eliminar(aEliminar.id);
      toast.exito(r.mensaje);
      setAEliminar(null);
      cargar();
    } catch (err) {
      toast.error(mensajeError(err));
      setAEliminar(null);
    } finally {
      setGuardando(false);
    }
  };

  return { lista, cargar, formulario, setFormulario, abrir, cerrar, errores, guardando, guardar, aEliminar, setAEliminar, eliminar };
}
