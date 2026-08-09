import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api } from '../lib/api.js'
import { ORDER_FLOW, ORDER_STATUS_LABELS, dateTime, money } from '../lib/format.js'
import Loader from '../components/Loader.jsx'
import StatusBadge from '../components/StatusBadge.jsx'

export default function OrderDetail() {
  const { id } = useParams()
  const [order, setOrder] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [cancelling, setCancelling] = useState(false)

  useEffect(() => {
    api
      .get(`/orders/${id}`)
      .then(setOrder)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [id])

  async function onCancel() {
    setCancelling(true)
    setError(null)
    try {
      setOrder(await api.post(`/orders/${id}/cancel`))
    } catch (e) {
      setError(e.message)
    } finally {
      setCancelling(false)
    }
  }

  if (loading) return <Loader label="Loading order" />

  if (error && !order) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-20 text-center">
        <p className="text-5xl">🤷</p>
        <h1 className="mt-4 text-xl font-bold text-stone-900">We could not find that order</h1>
        <p className="mt-1 text-sm text-stone-500">{error}</p>
        <Link to="/orders" className="mt-6 inline-block rounded-lg bg-brand-600 px-5 py-2 text-sm font-semibold text-white">
          Back to your orders
        </Link>
      </div>
    )
  }

  const cancelled = order.status === 'CANCELLED'
  const currentStep = ORDER_FLOW.indexOf(order.status)

  return (
    <div className="mx-auto max-w-4xl px-4 py-8">
      <Link to="/orders" className="text-sm text-stone-500 hover:text-brand-700">
        ← All orders
      </Link>

      <div className="mt-4 flex flex-wrap items-start justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-stone-900">{order.orderNumber}</h1>
          <p className="mt-1 text-sm text-stone-500">Placed on {dateTime(order.createdAt)}</p>
        </div>
        <StatusBadge status={order.status} />
      </div>

      {order.paymentStatus === 'PENDING' && (
        <p className="mt-4 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
          Payment for this order has not completed yet.
        </p>
      )}

      {!cancelled && (
        <section className="mt-6 rounded-xl border border-stone-200 bg-white p-5">
          <h2 className="font-bold text-stone-900">Delivery progress</h2>
          <ol className="mt-5 flex items-center">
            {ORDER_FLOW.map((step, index) => {
              const done = index <= currentStep
              return (
                <li key={step} className="flex flex-1 items-center last:flex-none">
                  <div className="flex flex-col items-center gap-2">
                    <span
                      className={`grid h-8 w-8 place-items-center rounded-full text-sm font-bold ${
                        done ? 'bg-brand-600 text-white' : 'bg-stone-200 text-stone-500'
                      }`}
                    >
                      {done ? '✓' : index + 1}
                    </span>
                    <span
                      className={`whitespace-nowrap text-[11px] font-medium sm:text-xs ${
                        done ? 'text-brand-800' : 'text-stone-400'
                      }`}
                    >
                      {ORDER_STATUS_LABELS[step]}
                    </span>
                  </div>
                  {index < ORDER_FLOW.length - 1 && (
                    <div className={`mx-2 h-0.5 flex-1 ${index < currentStep ? 'bg-brand-600' : 'bg-stone-200'}`} />
                  )}
                </li>
              )
            })}
          </ol>
        </section>
      )}

      <div className="mt-6 grid gap-6 md:grid-cols-[1fr_18rem]">
        <section className="rounded-xl border border-stone-200 bg-white p-5">
          <h2 className="font-bold text-stone-900">Items</h2>
          <ul className="mt-4 divide-y divide-stone-100">
            {order.items.map((item) => (
              <li key={item.id} className="flex items-center gap-4 py-3">
                <span className="grid h-12 w-12 shrink-0 place-items-center rounded-lg bg-stone-50 text-2xl">
                  {item.emoji || '🛒'}
                </span>
                <div className="flex-1">
                  <p className="text-sm font-semibold text-stone-800">{item.productName}</p>
                  <p className="text-xs text-stone-500">
                    {item.productUnit} · {money(item.unitPrice)} × {item.quantity}
                  </p>
                </div>
                <p className="font-semibold text-stone-900">{money(item.lineTotal)}</p>
              </li>
            ))}
          </ul>

          <dl className="mt-4 space-y-2 border-t border-stone-200 pt-4 text-sm">
            <div className="flex justify-between">
              <dt className="text-stone-500">Subtotal</dt>
              <dd className="font-medium text-stone-800">{money(order.subtotal)}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-stone-500">Delivery</dt>
              <dd className="font-medium text-stone-800">
                {Number(order.deliveryFee) === 0 ? <span className="text-brand-700">Free</span> : money(order.deliveryFee)}
              </dd>
            </div>
            <div className="flex justify-between border-t border-stone-100 pt-2">
              <dt className="font-bold text-stone-900">Total</dt>
              <dd className="font-bold text-stone-900">{money(order.total)}</dd>
            </div>
          </dl>
        </section>

        <aside className="space-y-6">
          <section className="rounded-xl border border-stone-200 bg-white p-5 text-sm">
            <h2 className="font-bold text-stone-900">Delivering to</h2>
            <p className="mt-3 font-semibold text-stone-800">{order.shipName}</p>
            <p className="mt-1 text-stone-600">
              {order.shipLine1}
              {order.shipLine2 ? `, ${order.shipLine2}` : ''}
              <br />
              {order.shipCity}, {order.shipState} — {order.shipPincode}
            </p>
            <p className="mt-2 text-stone-500">{order.shipPhone}</p>
          </section>

          <section className="rounded-xl border border-stone-200 bg-white p-5 text-sm">
            <h2 className="font-bold text-stone-900">Payment</h2>
            <p className="mt-2 text-stone-600">
              {order.paymentStatus === 'COD' ? 'Cash on delivery' : `Online · ${order.paymentStatus}`}
            </p>
          </section>

          {order.status === 'PLACED' && (
            <div>
              {error && <p className="mb-2 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>}
              <button
                onClick={onCancel}
                disabled={cancelling}
                className="w-full rounded-lg border border-stone-300 py-2.5 text-sm font-semibold text-stone-700 transition hover:border-red-300 hover:bg-red-50 hover:text-red-700 disabled:opacity-60"
              >
                {cancelling ? 'Cancelling…' : 'Cancel this order'}
              </button>
            </div>
          )}
        </aside>
      </div>
    </div>
  )
}
