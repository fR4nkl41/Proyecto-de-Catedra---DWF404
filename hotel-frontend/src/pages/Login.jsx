import { useEffect, useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { erroresCampos, mensajeError } from '../api/axios';
import Field from '../components/Field';
import Icon from '../components/Icon';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import AuthShell from './AuthShell';

export default function Login() {
  const { usuario, login } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '' });
  const [errores, setErrores] = useState({});
  const [error, setError] = useState('');
  const [aviso, setAviso] = useState('');
  const [cargando, setCargando] = useState(false);

  useEffect(() => {
    const mensaje = sessionStorage.getItem('anil_aviso');
    if (mensaje) {
      setAviso(mensaje);
      sessionStorage.removeItem('anil_aviso');
    }
  }, []);

  if (usuario) return <Navigate to="/" replace />;

  const cambiar = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const enviar = async (e) => {
    e.preventDefault();
    setCargando(true);
    setError('');
    setErrores({});
    try {
      const r = await login(form.email, form.password);
      toast.exito(r.mensaje);
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
      <p className="auth-eyebrow">Bienvenido de nuevo</p>
      <h1>Inicia sesión</h1>
      <p className="sub">Entra a tu panel para gestionar hoteles y reservas.</p>

      {aviso && (
        <div className="alert alert-info"><Icon name="info" /><span>{aviso}</span></div>
      )}
      {error && (
        <div className="alert alert-error"><Icon name="alert" /><span>{error}</span></div>
      )}

      <div className="demo-note">
        <Icon name="info" />
        <span>Usuarios de prueba: admin@hotel.com / Admin123 · recepcion@hotel.com / Recep123 · carlos@mail.com / Huesped123</span>
      </div>

      <form onSubmit={enviar} noValidate>
        <Field label="Correo electrónico" icon="mail" error={errores.email} required>
          <input type="email" name="email" value={form.email} onChange={cambiar} placeholder="admin@hotel.com" autoComplete="username" />
        </Field>
        <Field label="Contraseña" icon="key" error={errores.password} required>
          <input type="password" name="password" value={form.password} onChange={cambiar} autoComplete="current-password" />
        </Field>
        <button className="btn btn-primary btn-block" disabled={cargando}>
          {cargando ? 'Validando...' : 'Iniciar sesión'}
        </button>
      </form>
      <p className="auth-switch">
        ¿No tienes cuenta? <Link to="/registro" className="btn-text" style={{ display: 'inline' }}>Regístrate</Link>
      </p>
    </AuthShell>
  );
}
