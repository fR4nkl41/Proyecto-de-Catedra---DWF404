/** Patrón decorativo de rombos usado en las tarjetas (igual al prototipo). */
export default function MiniPatron({ id, ancho = 64, alto = 64, tam = 16 }) {
  const m = tam / 2;
  return (
    <svg className="mini-pattern" viewBox={`0 0 ${ancho} ${alto}`} preserveAspectRatio="xMidYMid slice">
      <defs>
        <pattern id={id} width={tam} height={tam} patternUnits="userSpaceOnUse">
          <path d={`M${m} 1 L${tam - 1} ${m} L${m} ${tam - 1} L1 ${m} Z`} fill="none" stroke="#5866AE" strokeWidth="0.8" />
        </pattern>
      </defs>
      <rect width={ancho} height={alto} fill={`url(#${id})`} />
    </svg>
  );
}
