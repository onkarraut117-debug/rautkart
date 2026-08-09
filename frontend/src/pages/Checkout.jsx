import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../lib/api.js'
import { money } from '../lib/format.js'
import { loadRazorpay, openRazorpay } from '../lib/razorpay.js'
import { useAuth } from '../context/AuthContext.jsx'
import { useCart } from '../context/CartContext.jsx'
import Loader from '../components/Loader.jsx'
import EmptyState from '../components/EmptyState.jsx'

const BLANK_ADDRESS = {
  fullName: '',
  phone: '',
  line1: '',
  line2: '',
  city: '',
  state: '',
  pincode: '',
  isDefault: true,
}

export default function Checkout() {
  const { user } = useAuth()
  const { cart, refresh } = useCart()
  const navigate = useNavigate()

  const [addresses, setAddresses] = useState([])
  const [selectedId, setSelectedId] = useState(null)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ ...BLANK_ADDRESS, fullName: user?.name || '', phone: user?.phone || '' })
  const [payment, setPayment] = useState('ONLINE')
  const [loading, setLoading] = useState(true)
  const [placing, setPlacing] = useState(false)
  const [error, setError] = useState(null)
  const [fieldErrors, setFieldErrors] = useState({})

  useEffect(() => {
    api
      .get('/addresses')
      .then((list) => {
        setAddresses(list)
        const preferred = list.find((a) => a.isDefault) || list[0]
        setSelectedId(preferred?.id ?? null)
        setShowForm(list.length === 0)
      })
      .catch(() => setShowForm(true))
      .finally(() => setLoading(false))
  }, [])

  function set(key, value) {
    setForm((f) => ({ ...f, [key]: value }))
  }

  async function onPlaceOrder(e) {
    e.preventDefault()
    setPlacing(true)
    setError(null)
    setFieldErrors({})

    try {
      // The backend accepts either a saved address id or a fresh address inline,
      // and works out totals, stock and payment state itself.
      const body = showForm
        ? { address: form, paymentMethod: payment }
        : { addressId: selectedId, paymentMethod: payment }

      const checkout = await api.post('/orders', body)

      if (!checkout.paymentRequired) {
        await refresh()
        navigate(`/orders/${checkout.order.id}`, { replace: true })
        return
      }

      const ready = await loadRazorpay()
      if (!ready) {
        throw new Error('Could not load the payment window. Check your internet connection.')
      }

      const result = await openRazorpay({
        keyId: checkout.razorpayKeyId,
        razorpayOrderId: checkout.razorpayOrderId,
        amountInPaise: checkout.amountInPaise,
        order: checkout.order,
        user,
      })

      if (!result) {
        // Dismissed. The order exists as unpaid, so send them to it.
        await refresh()
        navigate(`/orders/${checkout.order.id}`, { replace: true })
        return
      }

      await api.post('/orders/payment/verify', {
        razorpayOrderId: result.razorpay_order_id,
        razorpayPaymentId: result.razorpay_payment_id,
        razorpaySignature: result.razorpay_signature,
      })

      await refresh()
      navigate(`/orders/${checkout.order.id}`, { replace: true })
    } catch (err) {
      setError(err.message)
      setFieldErrors(err.fieldErrors || {})
    } finally {
      setPlacing(false)
    }
  }

  if (loading) return <Loader label="Preparing checkout" />

  if (cart.items.length === 0) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-12">
        <EmptyState
          icon="🛍️"
          title="Nothing to check out"
          description="Your cart is empty, so there is no order to place yet."
          actionTo="/shop"
          actionLabel="Go shopping"
        />
      </div>
    )
  }

  const inputClass =
    'mt-1 w-full rounded-lg border border-stone-300 px-3 py-2 text-sm outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100'

  const addressField = (key, label, extra = {}) => (
    <div className={extra.wide ? 'sm:col-span-2' : ''}>
      <label className="text-sm font-medium text-stone-700">{label}</label>
      <input
        value={form[key]}
        onChange={(e) => set(key, e.target.value)}
        required={!extra.optional}
        maxLength={extra.maxLength}
        placeholder={extra.placeholder}
        className={inputClass}
      />
      {fieldErrors[`address.${key}`] && (
        <p className="mt-1 text-xs text-red-600">{fieldErrors[`address.${key}`]}</p>
      )}
    </div>
  )

  return (
    <form onSubmit={onPlaceOrder} className="mx-auto max-w-6xl px-4 py-8">
      <h1 className="text-2xl font-bold text-stone-900">Checkout</h1>

      <div className="mt-6 grid gap-6 lg:grid-cols-[1fr_22rem]">
        <div className="space-y-6">
          <section className="rounded-xl border border-stone-200 bg-white p-5">
            <div className="flex items-center justify-between">
              <h2 className="font-bold text-stone-900">Delivery address</h2>
              {addresses.length > 0 && (
                <button
                  type="button"
                  onClick={() => setShowForm((s) => !s)}
                  className="text-sm font-semibold text-brand-700 hover:underline"
                >
                  {showForm ? 'Use a saved address' : 'Add a new address'}
                </button>
              )}
            </div>

            {showForm ? (
              <div className="mt-4 grid gap-4 sm:grid-cols-2">
                {addressField('fullName', 'Full name', { wide: true })}
                {addressField('phone', 'Phone', { maxLength: 10, placeholder: '10 digits' })}
                {addressField('pincode', 'Pincode', { maxLength: 6, placeholder: '6 digits' })}
                {addressField('line1', 'Address line 1', { wide: true })}
                {addressField('line2', 'Address line 2 (optional)', { wide: true, optional: true })}
                {addressField('city', 'City')}
                {addressField('state', 'State')}
              </div>
            ) : (
              <ul className="mt-4 space-y-3">
                {addresses.map((a) => (
                  <li key={a.id}>
                    <label
                      className={`flex cursor-pointer gap-3 rounded-lg border p-3 transition ${
                        selectedId === a.id ? 'border-brand-500 bg-brand-50' : 'border-stone-200 hover:bg-stone-50'
                      }`}
                    >
                      <input
                        type="radio"
                        name="address"
                        checked={selectedId === a.id}
                        onChange={() => setSelectedId(a.id)}
                        className="mt-1 h-4 w-4 accent-brand-600"
                      />
                      <div className="text-sm">
                        <p className="font-semibold text-stone-800">
                          {a.fullName}
                          {a.isDefault && (
                            <span className="ml-2 rounded bg-stone-200 px-1.5 py-0.5 text-[10px] font-bold uppercase text-stone-600">
                              Default
                            </span>
                          )}
                        </p>
                        <p className="mt-0.5 text-stone-600">
                          {a.line1}
                          {a.line2 ? `, ${a.line2}` : ''}, {a.city}, {a.state} — {a.pincode}
                        </p>
                        <p className="mt-0.5 text-stone-500">{a.phone}</p>
                      </div>
                    </label>
                  </li>
                ))}
              </ul>
            )}
          </section>

          <section className="rounded-xl border border-stone-200 bg-white p-5">
            <h2 className="font-bold text-stone-900">Payment method</h2>
            <div className="mt-4 space-y-3">
              {[
                {
                  value: 'ONLINE',
                  title: 'Pay online',
                  note: 'Card, UPI or netbanking via Razorpay (test mode — no real money moves).',
                },
                { value: 'COD', title: 'Cash on delivery', note: 'Pay the delivery person when the order arrives.' },
              ].map((opt) => (
                <label
                  key={opt.value}
                  className={`flex cursor-pointer gap-3 rounded-lg border p-3 transition ${
                    payment === opt.value ? 'border-brand-500 bg-brand-50' : 'border-stone-200 hover:bg-stone-50'
                  }`}
                >
                  <input
                    type="radio"
                    name="payment"
                    checked={payment === opt.value}
                    onChange={() => setPayment(opt.value)}
                    className="mt-1 h-4 w-4 accent-brand-600"
                  />
                  <div className="text-sm">
                    <p className="font-semibold text-stone-800">{opt.title}</p>
                    <p className="mt-0.5 text-stone-500">{opt.note}</p>
                  </div>
                </label>
              ))}
            </div>
          </section>
        </div>

        <aside className="h-fit rounded-xl border border-stone-200 bg-white p-5 lg:sticky lg:top-24">
          <h2 className="font-bold text-stone-900">Order summary</h2>

          <ul className="mt-4 space-y-2 text-sm">
            {cart.items.map((item) => (
              <li key={item.id} className="flex justify-between gap-3">
                <span className="text-stone-600">
                  {item.name} <span className="text-stone-400">× {item.quantity}</span>
                </span>
                <span className="shrink-0 font-medium text-stone-800">{money(item.lineTotal)}</span>
              </li>
            ))}
          </ul>

          <dl className="mt-4 space-y-2 border-t border-stone-200 pt-4 text-sm">
            <div className="flex justify-between">
              <dt className="text-stone-500">Subtotal</dt>
              <dd className="font-medium text-stone-800">{money(cart.subtotal)}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-stone-500">Delivery</dt>
              <dd className="font-medium text-stone-800">
                {Number(cart.deliveryFee) === 0 ? <span className="text-brand-700">Free</span> : money(cart.deliveryFee)}
              </dd>
            </div>
          </dl>

          <div className="mt-4 flex justify-between border-t border-stone-200 pt-4">
            <span className="font-bold text-stone-900">Total</span>
            <span className="text-lg font-bold text-stone-900">{money(cart.total)}</span>
          </div>

          {error && <p className="mt-4 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>}

          <button
            type="submit"
            disabled={placing || (!showForm && !selectedId)}
            className="mt-5 w-full rounded-lg bg-brand-600 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:opacity-60"
          >
            {placing ? 'Placing order…' : payment === 'COD' ? 'Place order' : `Pay ${money(cart.total)}`}
          </button>
        </aside>
      </div>
    </form>
  )
}
