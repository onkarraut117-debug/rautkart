import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../lib/api.js'
import { dateTime, money } from '../lib/format.js'
import Loader from '../components/Loader.jsx'
import EmptyState from '../components/EmptyState.jsx'
import StatusBadge from '../components/StatusBadge.jsx'

export default function Orders() {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api
      .get('/orders')
      .then(setOrders)
      .catch(() => setOrders([]))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <Loader label="Loading your orders" />

  if (orders.length === 0) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-12">
        <EmptyState
          icon="📦"
          title="No orders yet"
          description="Once you place an order it will show up here with its delivery status."
          actionTo="/shop"
          actionLabel="Browse the shop"
        />
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-4xl px-4 py-8">
      <h1 className="text-2xl font-bold text-stone-900">Your orders</h1>

      <ul className="mt-6 space-y-4">
        {orders.map((order) => (
          <li key={order.id}>
            <Link
              to={`/orders/${order.id}`}
              className="block rounded-xl border border-stone-200 bg-white p-5 transition hover:border-brand-300 hover:shadow-md"
            >
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div>
                  <p className="font-bold text-stone-900">{order.orderNumber}</p>
                  <p className="mt-0.5 text-sm text-stone-500">{dateTime(order.createdAt)}</p>
                </div>
                <StatusBadge status={order.status} />
              </div>

              <div className="mt-4 flex flex-wrap items-center justify-between gap-3 border-t border-stone-100 pt-4">
                <div className="flex items-center gap-1.5 text-2xl">
                  {order.items.slice(0, 5).map((item) => (
                    <span key={item.id} title={item.productName}>
                      {item.emoji || '🛒'}
                    </span>
                  ))}
                  {order.items.length > 5 && (
                    <span className="ml-1 text-sm text-stone-500">+{order.items.length - 5} more</span>
                  )}
                </div>
                <p className="font-bold text-stone-900">{money(order.total)}</p>
              </div>
            </Link>
          </li>
        ))}
      </ul>
    </div>
  )
}
