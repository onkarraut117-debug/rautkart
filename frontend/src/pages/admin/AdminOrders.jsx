import { useCallback, useEffect, useState } from 'react'
import { api } from '../../lib/api.js'
import { ORDER_STATUS_LABELS, dateTime, money } from '../../lib/format.js'
import Loader from '../../components/Loader.jsx'
import StatusBadge from '../../components/StatusBadge.jsx'

const FILTERS = ['', 'PLACED', 'PACKED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED']
const ASSIGNABLE = ['PLACED', 'PACKED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED']

export default function AdminOrders() {
  const [page, setPage] = useState(null)
  const [status, setStatus] = useState('')
  const [pageNo, setPageNo] = useState(0)
  const [loading, setLoading] = useState(true)
  const [expanded, setExpanded] = useState(null)
  const [error, setError] = useState(null)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const query = new URLSearchParams({ page: String(pageNo), size: '20' })
      if (status) query.set('status', status)
      setPage(await api.get(`/admin/orders?${query}`))
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }, [status, pageNo])

  useEffect(() => {
    load()
  }, [load])

  async function onStatusChange(order, next) {
    setError(null)
    try {
      const updated = await api.patch(`/admin/orders/${order.id}/status`, { status: next })
      setPage((p) => ({ ...p, content: p.content.map((o) => (o.id === updated.id ? updated : o)) }))
    } catch (e) {
      setError(e.message)
    }
  }

  return (
    <div>
      <div className="flex flex-wrap items-center gap-2">
        {FILTERS.map((f) => (
          <button
            key={f || 'all'}
            onClick={() => {
              setStatus(f)
              setPageNo(0)
            }}
            className={`rounded-full px-3.5 py-1.5 text-sm font-semibold transition ${
              status === f ? 'bg-stone-900 text-white' : 'bg-stone-100 text-stone-600 hover:bg-stone-200'
            }`}
          >
            {f ? ORDER_STATUS_LABELS[f] : 'All'}
          </button>
        ))}
      </div>

      {error && <p className="mt-4 rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">{error}</p>}

      {loading ? (
        <Loader label="Loading orders" />
      ) : !page || page.content.length === 0 ? (
        <p className="mt-6 rounded-xl border border-dashed border-stone-300 bg-white px-4 py-12 text-center text-sm text-stone-500">
          No orders in this view.
        </p>
      ) : (
        <ul className="mt-5 space-y-3">
          {page.content.map((order) => (
            <li key={order.id} className="rounded-xl border border-stone-200 bg-white">
              <div className="flex flex-wrap items-center gap-x-6 gap-y-3 p-4">
                <div className="min-w-40">
                  <p className="font-bold text-stone-900">{order.orderNumber}</p>
                  <p className="text-xs text-stone-500">{dateTime(order.createdAt)}</p>
                </div>

                <div className="min-w-40 text-sm">
                  <p className="font-medium text-stone-800">{order.customerName}</p>
                  <p className="text-xs text-stone-500">
                    {order.shipCity} — {order.shipPincode}
                  </p>
                </div>

                <div className="text-sm">
                  <p className="font-bold text-stone-900">{money(order.total)}</p>
                  <p className="text-xs text-stone-500">
                    {order.paymentStatus === 'COD' ? 'Cash on delivery' : `Online · ${order.paymentStatus}`}
                  </p>
                </div>

                <div className="ml-auto flex items-center gap-3">
                  <StatusBadge status={order.status} />
                  <select
                    value={order.status}
                    onChange={(e) => onStatusChange(order, e.target.value)}
                    className="rounded-lg border border-stone-300 px-2 py-1.5 text-sm outline-none focus:border-brand-500"
                  >
                    {ASSIGNABLE.map((s) => (
                      <option key={s} value={s}>
                        {ORDER_STATUS_LABELS[s]}
                      </option>
                    ))}
                  </select>
                  <button
                    onClick={() => setExpanded(expanded === order.id ? null : order.id)}
                    className="text-sm font-semibold text-brand-700 hover:underline"
                  >
                    {expanded === order.id ? 'Hide' : 'Details'}
                  </button>
                </div>
              </div>

              {expanded === order.id && (
                <div className="grid gap-6 border-t border-stone-100 p-4 sm:grid-cols-2">
                  <div>
                    <h3 className="text-xs font-semibold uppercase tracking-wide text-stone-500">Items</h3>
                    <ul className="mt-2 space-y-1.5 text-sm">
                      {order.items.map((item) => (
                        <li key={item.id} className="flex justify-between gap-3">
                          <span className="text-stone-700">
                            {item.emoji} {item.productName}{' '}
                            <span className="text-stone-400">× {item.quantity}</span>
                          </span>
                          <span className="font-medium text-stone-800">{money(item.lineTotal)}</span>
                        </li>
                      ))}
                    </ul>
                  </div>

                  <div className="text-sm">
                    <h3 className="text-xs font-semibold uppercase tracking-wide text-stone-500">Deliver to</h3>
                    <p className="mt-2 font-medium text-stone-800">{order.shipName}</p>
                    <p className="text-stone-600">
                      {order.shipLine1}
                      {order.shipLine2 ? `, ${order.shipLine2}` : ''}
                      <br />
                      {order.shipCity}, {order.shipState} — {order.shipPincode}
                    </p>
                    <p className="mt-1 text-stone-500">{order.shipPhone}</p>
                    <p className="mt-1 text-stone-500">{order.customerEmail}</p>
                  </div>
                </div>
              )}
            </li>
          ))}
        </ul>
      )}

      {page && page.totalPages > 1 && (
        <div className="mt-6 flex items-center justify-center gap-2">
          <button
            disabled={pageNo <= 0}
            onClick={() => setPageNo((n) => n - 1)}
            className="rounded-lg border border-stone-300 bg-white px-4 py-2 text-sm font-medium transition hover:bg-stone-50 disabled:opacity-40"
          >
            Previous
          </button>
          <span className="px-3 text-sm text-stone-500">
            Page {page.page + 1} of {page.totalPages}
          </span>
          <button
            disabled={pageNo >= page.totalPages - 1}
            onClick={() => setPageNo((n) => n + 1)}
            className="rounded-lg border border-stone-300 bg-white px-4 py-2 text-sm font-medium transition hover:bg-stone-50 disabled:opacity-40"
          >
            Next
          </button>
        </div>
      )}
    </div>
  )
}
