import Icon from '../components/Icon';

/** Panel de marca (izquierda) + formulario (derecha), igual que el prototipo. */
export default function AuthShell({ children }) {
  return (
    <div id="auth-shell" className="active">
      <div className="auth-grid">
        <div className="auth-brand">
          <svg className="tile-pattern" viewBox="0 0 400 400" preserveAspectRatio="xMidYMid slice">
            <defs>
              <pattern id="anilTile" width="42" height="42" patternUnits="userSpaceOnUse">
                <path d="M21 2 L40 21 L21 40 L2 21 Z" fill="none" stroke="#5866AE" strokeWidth="1" opacity="0.55" />
                <circle cx="21" cy="21" r="2.4" fill="#D9A85B" opacity="0.5" />
              </pattern>
            </defs>
            <rect width="400" height="400" fill="url(#anilTile)" />
          </svg>

          <div className="brand-mark">
            <svg width="30" height="30" viewBox="0 0 28 28">
              <rect x="1" y="1" width="26" height="26" rx="7" fill="#33418C" />
              <path d="M14 6 L22 14 L14 22 L6 14 Z" fill="none" stroke="#fff" strokeWidth="1.6" />
              <circle cx="14" cy="14" r="2.1" fill="#fff" />
            </svg>
            <span className="brand-wordmark">Añil</span>
          </div>

          <div>
            <p className="brand-quote">"Un sistema de gestión hotelera pensado para equipos pequeños que atienden mucho."</p>
            <div className="brand-features">
              <div className="brand-feature"><Icon name="calendar" /> Disponibilidad y tarifas en un solo lugar</div>
              <div className="brand-feature"><Icon name="users" /> Reservas y huéspedes sin hojas de cálculo</div>
              <div className="brand-feature"><Icon name="card" /> Pagos y confirmaciones automáticas</div>
            </div>
          </div>

          <p className="brand-foot">Añil PMS · San Salvador, El Salvador</p>
        </div>

        <div className="auth-form-panel">
          <div className="auth-card">{children}</div>
        </div>
      </div>
    </div>
  );
}
