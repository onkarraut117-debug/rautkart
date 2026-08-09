const inr = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  maximumFractionDigits: 2,
})

export function money(value) {
  const n = Number(value ?? 0)
  return inr.format(n)
}

export function dateTime(iso) {
  if (!iso) return ''
  return new Date(iso).toLocaleString('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  })
}

export const ORDER_STATUS_LABELS = {
  PLACED: 'Placed',
  PACKED: 'Packed',
  OUT_FOR_DELIVERY: 'Out for delivery',
  DELIVERED: 'Delivered',
  CANCELLED: 'Cancelled',
}

export const ORDER_STATUS_STYLES = {
  PLACED: 'bg-amber-100 text-amber-800',
  PACKED: 'bg-sky-100 text-sky-800',
  OUT_FOR_DELIVERY: 'bg-indigo-100 text-indigo-800',
  DELIVERED: 'bg-brand-100 text-brand-800',
  CANCELLED: 'bg-stone-200 text-stone-600',
}

/** The forward-only path an order walks through. */
export const ORDER_FLOW = ['PLACED', 'PACKED', 'OUT_FOR_DELIVERY', 'DELIVERED']
