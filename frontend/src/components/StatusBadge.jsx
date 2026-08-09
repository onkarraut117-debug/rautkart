import { ORDER_STATUS_LABELS, ORDER_STATUS_STYLES } from '../lib/format.js'

export default function StatusBadge({ status }) {
  return (
    <span
      className={`inline-block rounded-full px-2.5 py-1 text-xs font-semibold ${
        ORDER_STATUS_STYLES[status] || 'bg-stone-100 text-stone-700'
      }`}
    >
      {ORDER_STATUS_LABELS[status] || status}
    </span>
  )
}
