import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../../lib/api.js'
import { money } from '../../lib/format.js'
import Loader from '../../components/Loader.jsx'

function Stat({ label, value, hint }) {
  return (
    <div className="rounded-xl border border-stone-200 bg-white p-5">
      <p className="text-sm text-stone-500">{label}</p>
      <p className="mt-1 text-2xl font-bold text-stone-900">{value}</p>
      {hint && <p className="mt-1 text-xs text-stone-400">{hint}</p>}
    </div>
  )
}

export default function Dashboard() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    api
      .get('/admin/dashboard')
      .then(setData)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <Loader label="Crunching the numbers" />
  if (error) return <p className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">{error}</p>

  return (
    <div className="space-y-8">
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <Stat label="Total revenue" value={money(data.totalRevenue)} hint="Excludes cancelled orders" />
        <Stat label="Orders" value={data.totalOrders} hint={`${data.ordersLast7Days} in the last 7 days`} />
        <Stat label="Awaiting packing" value={data.pendingOrders} hint="Orders still marked Placed" />
        <Stat label="Active products" value={data.activeProducts} />
        <Stat label="Registered customers" value={data.customers} />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <section className="rounded-xl border border-stone-200 bg-white p-5">
          <h2 className="font-bold text-stone-900">Top sellers</h2>
          {data.topProducts.length === 0 ? (
            <p className="mt-3 text-sm text-stone-500">No sales yet.</p>
          ) : (
            <ol className="mt-4 space-y-3">
              {data.topProducts.map((p, i) => (
                <li key={p.productName} className="flex items-center gap-3 text-sm">
                  <span className="grid h-6 w-6 shrink-0 place-items-center rounded-full bg-stone-100 text-xs font-bold text-stone-600">
                    {i + 1}
                  </span>
                  <span className="flex-1 text-stone-700">{p.productName}</span>
                  <span className="font-semibold text-stone-900">{p.unitsSold} sold</span>
                </li>
              ))}
            </ol>
          )}
        </section>

        <section className="rounded-xl border border-stone-200 bg-white p-5">
          <div className="flex items-center justify-between">
            <h2 className="font-bold text-stone-900">Running low</h2>
            <Link to="/admin/products" className="text-sm font-semibold text-brand-700 hover:underline">
              Restock →
            </Link>
          </div>
          {data.lowStock.length === 0 ? (
            <p className="mt-3 text-sm text-stone-500">Everything is well stocked.</p>
          ) : (
            <ul className="mt-4 space-y-3">
              {data.lowStock.map((p) => (
                <li key={p.id} className="flex items-center gap-3 text-sm">
                  <span className="flex-1 text-stone-700">
                    {p.name} <span className="text-stone-400">({p.unit})</span>
                  </span>
                  <span
                    className={`rounded-full px-2 py-0.5 text-xs font-bold ${
                      p.stockQty === 0 ? 'bg-red-100 text-red-700' : 'bg-amber-100 text-amber-800'
                    }`}
                  >
                    {p.stockQty} left
                  </span>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </div>
  )
}
