import { Link } from 'react-router-dom'
import { useCart } from '../context/CartContext.jsx'
import { money } from '../lib/format.js'
import QuantityStepper from '../components/QuantityStepper.jsx'
import EmptyState from '../components/EmptyState.jsx'

export default function Cart() {
  const { cart, setQuantity, removeItem, busy } = useCart()

  if (cart.items.length === 0) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-12">
        <EmptyState
          icon="🛍️"
          title="Your cart is empty"
          description="Add some rice, dal or fresh vegetables and they will show up here."
          actionTo="/shop"
          actionLabel="Start shopping"
        />
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-6xl px-4 py-8">
      <h1 className="text-2xl font-bold text-stone-900">
        Your cart <span className="text-base font-normal text-stone-500">({cart.itemCount} items)</span>
      </h1>

      <div className="mt-6 grid gap-6 lg:grid-cols-[1fr_22rem]">
        <ul className="divide-y divide-stone-200 overflow-hidden rounded-xl border border-stone-200 bg-white">
          {cart.items.map((item) => (
            <li key={item.id} className="flex gap-4 p-4">
              <Link
                to={`/product/${item.slug}`}
                className="grid h-20 w-20 shrink-0 place-items-center overflow-hidden rounded-lg bg-stone-50 text-3xl"
              >
                {item.imageUrl ? (
                  <img src={item.imageUrl} alt={item.name} className="h-full w-full object-cover" />
                ) : (
                  <span>{item.emoji || '🛒'}</span>
                )}
              </Link>

              <div className="flex flex-1 flex-col">
                <div className="flex justify-between gap-4">
                  <div>
                    <Link to={`/product/${item.slug}`} className="font-semibold text-stone-800 hover:text-brand-700">
                      {item.name}
                    </Link>
                    <p className="text-xs text-stone-500">{item.unit}</p>
                  </div>
                  <p className="shrink-0 font-bold text-stone-900">{money(item.lineTotal)}</p>
                </div>

                <div className="mt-auto flex items-center justify-between pt-3">
                  <div className="w-32">
                    <QuantityStepper
                      value={item.quantity}
                      max={item.stockQty}
                      disabled={busy}
                      onChange={(qty) => setQuantity(item.productId, qty)}
                    />
                  </div>
                  <button
                    onClick={() => removeItem(item.productId)}
                    disabled={busy}
                    className="text-sm font-medium text-stone-500 transition hover:text-red-600 disabled:opacity-50"
                  >
                    Remove
                  </button>
                </div>
              </div>
            </li>
          ))}
        </ul>

        <aside className="h-fit rounded-xl border border-stone-200 bg-white p-5 lg:sticky lg:top-24">
          <h2 className="font-bold text-stone-900">Order summary</h2>

          <dl className="mt-4 space-y-2 text-sm">
            <div className="flex justify-between">
              <dt className="text-stone-500">Subtotal</dt>
              <dd className="font-medium text-stone-800">{money(cart.subtotal)}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-stone-500">Delivery</dt>
              <dd className="font-medium text-stone-800">
                {Number(cart.deliveryFee) === 0 ? (
                  <span className="text-brand-700">Free</span>
                ) : (
                  money(cart.deliveryFee)
                )}
              </dd>
            </div>
          </dl>

          {Number(cart.amountForFreeDelivery) > 0 && (
            <p className="mt-3 rounded-lg bg-amber-50 px-3 py-2 text-xs text-amber-800">
              Add {money(cart.amountForFreeDelivery)} more for free delivery.
            </p>
          )}

          <div className="mt-4 flex justify-between border-t border-stone-200 pt-4">
            <span className="font-bold text-stone-900">Total</span>
            <span className="text-lg font-bold text-stone-900">{money(cart.total)}</span>
          </div>

          <Link
            to="/checkout"
            className="mt-5 block rounded-lg bg-brand-600 py-3 text-center text-sm font-semibold text-white transition hover:bg-brand-700"
          >
            Proceed to checkout
          </Link>
          <Link
            to="/shop"
            className="mt-2 block py-2 text-center text-sm font-medium text-stone-500 hover:text-brand-700"
          >
            Continue shopping
          </Link>
        </aside>
      </div>
    </div>
  )
}
