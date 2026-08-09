/**
 * Minus / count / plus control. Dropping below 1 removes the line, which the
 * backend handles when it receives quantity 0.
 */
export default function QuantityStepper({ value, max = 99, disabled = false, onChange }) {
  const atMax = value >= max

  return (
    <div className="flex items-center justify-between rounded-lg border border-brand-600 bg-brand-600 text-white">
      <button
        type="button"
        aria-label="Decrease quantity"
        disabled={disabled}
        onClick={() => onChange(value - 1)}
        className="px-3 py-1.5 text-lg leading-none transition hover:bg-brand-700 disabled:opacity-50"
      >
        −
      </button>
      <span className="text-sm font-semibold tabular-nums">{value}</span>
      <button
        type="button"
        aria-label="Increase quantity"
        disabled={disabled || atMax}
        onClick={() => onChange(value + 1)}
        title={atMax ? `Only ${max} in stock` : undefined}
        className="px-3 py-1.5 text-lg leading-none transition hover:bg-brand-700 disabled:opacity-50"
      >
        +
      </button>
    </div>
  )
}
