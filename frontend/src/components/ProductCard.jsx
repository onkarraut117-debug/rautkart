import { Link, useNavigate } from 'react-router-dom'
import { money } from '../lib/format.js'
import { useAuth } from '../context/AuthContext.jsx'
import { useCart } from '../context/CartContext.jsx'
import QuantityStepper from './QuantityStepper.jsx'

export default function ProductCard({ product }) {
  const { user } = useAuth()
  const { addItem, setQuantity, quantityOf, busy } = useCart()
  const navigate = useNavigate()

  const inCart = quantityOf(product.id)

  async function onAdd() {
    if (!user) {
      navigate('/login', { state: { from: `/product/${product.slug}` } })
      return
    }
    await addItem(product.id, 1)
  }

  return (
    <div className="group flex flex-col overflow-hidden rounded-xl border border-stone-200 bg-white transition hover:border-brand-300 hover:shadow-md">
      <Link to={`/product/${product.slug}`} className="relative block">
        <div className="grid aspect-square place-items-center bg-stone-50 text-6xl">
          {product.imageUrl ? (
            <img
              src={product.imageUrl}
              alt={product.name}
              className="h-full w-full object-cover transition group-hover:scale-105"
            />
          ) : (
            <span>{product.emoji || '🛒'}</span>
          )}
        </div>

        {product.discountPercent != null && (
          <span className="absolute left-2 top-2 rounded-md bg-brand-600 px-1.5 py-0.5 text-[11px] font-bold text-white">
            {product.discountPercent}% off
          </span>
        )}

        {!product.inStock && (
          <span className="absolute inset-0 grid place-items-center bg-white/70 text-sm font-semibold text-stone-600">
            Out of stock
          </span>
        )}
      </Link>

      <div className="flex flex-1 flex-col p-3">
        <Link
          to={`/product/${product.slug}`}
          className="line-clamp-2 text-sm font-semibold text-stone-800 hover:text-brand-700"
        >
          {product.name}
        </Link>
        <p className="mt-0.5 text-xs text-stone-500">{product.unit}</p>

        <div className="mt-2 flex items-baseline gap-2">
          <span className="text-base font-bold text-stone-900">{money(product.price)}</span>
          {product.discountPercent != null && (
            <span className="text-xs text-stone-400 line-through">{money(product.mrp)}</span>
          )}
        </div>

        <div className="mt-3">
          {inCart > 0 ? (
            <QuantityStepper
              value={inCart}
              max={product.stockQty}
              disabled={busy}
              onChange={(qty) => setQuantity(product.id, qty)}
            />
          ) : (
            <button
              onClick={onAdd}
              disabled={!product.inStock || busy}
              className="w-full rounded-lg border border-brand-600 py-1.5 text-sm font-semibold text-brand-700 transition hover:bg-brand-600 hover:text-white disabled:cursor-not-allowed disabled:border-stone-200 disabled:text-stone-400 disabled:hover:bg-transparent"
            >
              {product.inStock ? 'Add to cart' : 'Unavailable'}
            </button>
          )}
        </div>
      </div>
    </div>
  )
}
