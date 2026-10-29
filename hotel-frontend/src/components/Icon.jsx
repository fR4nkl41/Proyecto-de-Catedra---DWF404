/**
 * Sprite de íconos SVG (los mismos del prototipo de la Fase 1).
 * Se renderiza una sola vez en App y cada ícono se usa con <Icon name="search" />.
 */
export function IconSprite() {
  return (
    <svg style={{ display: 'none' }} xmlns="http://www.w3.org/2000/svg">
      <symbol id="i-grid" viewBox="0 0 24 24"><rect x="3" y="3" width="7.5" height="7.5" rx="1.5"/><rect x="13.5" y="3" width="7.5" height="7.5" rx="1.5"/><rect x="3" y="13.5" width="7.5" height="7.5" rx="1.5"/><rect x="13.5" y="13.5" width="7.5" height="7.5" rx="1.5"/></symbol>
      <symbol id="i-search" viewBox="0 0 24 24"><circle cx="10.5" cy="10.5" r="6.5"/><line x1="15.3" y1="15.3" x2="21" y2="21"/></symbol>
      <symbol id="i-bed" viewBox="0 0 24 24"><path d="M2.5 18v-6.5a1.5 1.5 0 0 1 1.5-1.5h16a1.5 1.5 0 0 1 1.5 1.5V18"/><path d="M2.5 18v2.5"/><path d="M21.5 18v2.5"/><path d="M4.5 10V7a1.5 1.5 0 0 1 1.5-1.5h4a1.5 1.5 0 0 1 1.5 1.5v3"/><line x1="2.5" y1="14.5" x2="21.5" y2="14.5"/></symbol>
      <symbol id="i-calendar" viewBox="0 0 24 24"><rect x="3" y="5" width="18" height="16" rx="2"/><line x1="16" y1="3" x2="16" y2="7"/><line x1="8" y1="3" x2="8" y2="7"/><line x1="3" y1="10" x2="21" y2="10"/></symbol>
      <symbol id="i-users" viewBox="0 0 24 24"><circle cx="9" cy="8.2" r="3"/><path d="M3.3 19.5a5.7 5.7 0 0 1 11.4 0"/><circle cx="17.6" cy="9.4" r="2.3"/><path d="M14.8 19.5a4.8 4.8 0 0 1 8.2-3.4"/></symbol>
      <symbol id="i-user" viewBox="0 0 24 24"><circle cx="12" cy="8" r="3.4"/><path d="M4.8 20a7.2 7.2 0 0 1 14.4 0"/></symbol>
      <symbol id="i-card" viewBox="0 0 24 24"><rect x="2.5" y="5.5" width="19" height="13" rx="2"/><line x1="2.5" y1="10" x2="21.5" y2="10"/><line x1="6" y1="14.5" x2="10.5" y2="14.5"/></symbol>
      <symbol id="i-check" viewBox="0 0 24 24"><circle cx="12" cy="12" r="9.3"/><path d="M7.8 12.3l2.6 2.6 5.8-5.8"/></symbol>
      <symbol id="i-check-plain" viewBox="0 0 24 24"><path d="M4 12.5l5.5 5.5L20 6.5"/></symbol>
      <symbol id="i-logout" viewBox="0 0 24 24"><path d="M15 4h3a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2h-3"/><path d="M3.5 12h11.5"/><path d="M11 7l4 5-4 5"/></symbol>
      <symbol id="i-arrow-left" viewBox="0 0 24 24"><line x1="19" y1="12" x2="5" y2="12"/><path d="M11 6l-6 6 6 6"/></symbol>
      <symbol id="i-pencil" viewBox="0 0 24 24"><path d="M4 20l4-1 11-11a1.9 1.9 0 0 0 0-2.6l-1.4-1.4a1.9 1.9 0 0 0-2.6 0L4 15l-1 5z"/><line x1="13.2" y1="5.3" x2="17.7" y2="9.8"/></symbol>
      <symbol id="i-pin" viewBox="0 0 24 24"><path d="M12 21.5s-7.3-6.6-7.3-12A7.3 7.3 0 0 1 19.3 9.5c0 5.4-7.3 12-7.3 12z"/><circle cx="12" cy="9.4" r="2.4"/></symbol>
      <symbol id="i-mail" viewBox="0 0 24 24"><rect x="3" y="5.5" width="18" height="13" rx="2"/><path d="M3.5 6.5l8.5 6 8.5-6"/></symbol>
      <symbol id="i-phone" viewBox="0 0 24 24"><path d="M6.6 3.5h3l1.5 4-2 1.8a13.2 13.2 0 0 0 5.8 5.8l1.8-2 4 1.5v3a2 2 0 0 1-2.2 2A17.2 17.2 0 0 1 4.6 5.7a2 2 0 0 1 2-2.2z"/></symbol>
      <symbol id="i-moon" viewBox="0 0 24 24"><path d="M20 13.7A8.6 8.6 0 1 1 10.3 4a6.8 6.8 0 0 0 9.7 9.7z"/></symbol>
      <symbol id="i-building" viewBox="0 0 24 24"><rect x="4.5" y="3" width="15" height="18" rx="1.2"/><rect x="7.5" y="7" width="3" height="3"/><rect x="13.5" y="7" width="3" height="3"/><rect x="7.5" y="12.5" width="3" height="3"/><rect x="13.5" y="12.5" width="3" height="3"/><line x1="10.5" y1="21" x2="10.5" y2="18"/><line x1="13.5" y1="21" x2="13.5" y2="18"/></symbol>
      <symbol id="i-coin" viewBox="0 0 24 24"><circle cx="12" cy="12" r="9"/><path d="M9 9.7c0-1.1 1.2-2 3-2s3 .9 3 2-1.2 1.4-3 2-3 .9-3 2 1.2 2 3 2 3-.9 3-2"/><line x1="12" y1="5.8" x2="12" y2="7.3"/><line x1="12" y1="16.7" x2="12" y2="18.2"/></symbol>
      <symbol id="i-alert" viewBox="0 0 24 24"><path d="M12 2.5l10.5 18.2H1.5z"/><line x1="12" y1="9.5" x2="12" y2="14"/><line x1="12" y1="17" x2="12" y2="17.1"/></symbol>
      <symbol id="i-info" viewBox="0 0 24 24"><circle cx="12" cy="12" r="9.3"/><line x1="12" y1="11" x2="12" y2="16.5"/><line x1="12" y1="7.6" x2="12" y2="7.7"/></symbol>
      <symbol id="i-plus" viewBox="0 0 24 24"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></symbol>
      <symbol id="i-x" viewBox="0 0 24 24"><line x1="6" y1="6" x2="18" y2="18"/><line x1="18" y1="6" x2="6" y2="18"/></symbol>
      <symbol id="i-trash" viewBox="0 0 24 24"><path d="M4 7h16"/><path d="M9 7V4.5h6V7"/><path d="M6 7l1 13h10l1-13"/><line x1="10" y1="11" x2="10" y2="16"/><line x1="14" y1="11" x2="14" y2="16"/></symbol>
      <symbol id="i-eye" viewBox="0 0 24 24"><path d="M2 12s3.6-6.5 10-6.5S22 12 22 12s-3.6 6.5-10 6.5S2 12 2 12z"/><circle cx="12" cy="12" r="2.8"/></symbol>
      <symbol id="i-chat" viewBox="0 0 24 24"><path d="M4 5h16v11H9l-5 4z"/></symbol>
      <symbol id="i-list" viewBox="0 0 24 24"><line x1="9" y1="6" x2="20" y2="6"/><line x1="9" y1="12" x2="20" y2="12"/><line x1="9" y1="18" x2="20" y2="18"/><circle cx="4.5" cy="6" r="1"/><circle cx="4.5" cy="12" r="1"/><circle cx="4.5" cy="18" r="1"/></symbol>
      <symbol id="i-tag" viewBox="0 0 24 24"><path d="M3 12.5V4h8.5L21 13.5 13.5 21z"/><circle cx="7.5" cy="8.5" r="1.4"/></symbol>
      <symbol id="i-door-in" viewBox="0 0 24 24"><path d="M14 4h4a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2h-4"/><path d="M3 12h11"/><path d="M10 8l4 4-4 4"/></symbol>
      <symbol id="i-key" viewBox="0 0 24 24"><circle cx="8" cy="15" r="4"/><path d="M11 12l9-9"/><path d="M17 6l2.5 2.5"/></symbol>
      <symbol id="i-power" viewBox="0 0 24 24"><path d="M12 3v9"/><path d="M6.3 6.8a8 8 0 1 0 11.4 0"/></symbol>
    </svg>
  );
}

export default function Icon({ name, className = 'icon', style }) {
  return (
    <svg className={className} style={style} aria-hidden="true">
      <use href={`#i-${name}`} />
    </svg>
  );
}
