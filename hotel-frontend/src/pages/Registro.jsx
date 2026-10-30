import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { erroresCampos, mensajeError } from '../api/axios';
import Field from '../components/Field';
import Icon from '../components/Icon';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import AuthShell from './AuthShell';

export default function Registro() {
  const { usuario, registrar } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const [form, setForm] = useState({ nombre: '', email: '', password: '', confirmar: '' });
  const [errores, setErrores] = useState({});
  const [error, setError] = useState('');
  const [cargando, setCargando] = useState(false);

  if (usuario) return <Navigate to="/" replace />;

  const cambiar = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const enviar = async (e) => {
    e.preventDefault();
    setError('');
    if (form.password !== form.confirmar) {
      setErrores({ confirmar: 'Las contraseñas no coinciden' });
      return;
    }
    setCargando(true);
    setErrores({});
    try {
      const r = await registrar({ nombre: form.nombre, email: form.email, password: form.password });
      toast.exito(r.mensaje, 'Cuenta creada');
      navigate('/');
    } catch (err) {
      setError(mensajeError(err));
      setErrores(erroresCampos(err));
    } finally {
      setCargando(false);
    }
  };

  return (
    <AuthShell>
      <p className="auth-eyebrow">Únete a Añil</p>
      <h1>Crea tu cuenta</h1>
      <p className="sub">Regístrate como huésped para reservar y dar seguimiento a tus estadías.</p>

      {error && (
        <div className="alert alert-error"><Icon name="alert" /><span>{error}</span></div>
      )}

      <form onSubmit={enviar} noValidate>
        <Field label="Nombre completo" icon="user" error={errores.nombre} required>
          <input name="nombre" value={form.nombre} onChange={cambiar} placeholder="María Rodríguez" />
        </Field>
        <Field label="Correo electrónico" icon="mail" error={errores.email} required>
          <input type="email" name="email" value={form.email} onChange={cambiar} placeholder="maria@ejemplo.com" />
        </Field>
        <div className="field-row">
          <Field label="Contraseña" error={errores.password} required>
            <input type="password" name="password" value={form.password} onChange={cambiar} placeholder="Mínimo 6, letras y números" />
          </Field>
          <Field label="Confirmar contraseña" error={errores.confirmar} required>
            <input type="password" name="confirmar" value={form.confirmar} onChange={cambiar} />
          </Field>
        </div>
        <button className="btn btn-success btn-block" disabled={cargando}>
          {cargando ? 'Creando cuenta...' : 'Crear cuenta'}
        </button>
      </form>
      <p className="auth-switch">
        ¿Ya tienes cuenta? <Link to="/login" className="btn-text" style={{ display: 'inline' }}>Inicia sesión</Link>
      </p>
    </AuthShell>
  );
}
