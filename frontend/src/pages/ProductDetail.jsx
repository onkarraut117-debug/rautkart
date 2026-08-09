import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api } from '../lib/api.js'
import { money } from '../lib/format.js'
import { useAuth } from '../context/AuthContext.jsx'
import { useCart } from '../context/CartContext.jsx'
import Loader from '../components/Loader.jsx'
import QuantityStepper from '../components/QuantityStepper.jsx'
import ProductCard from '../components/ProductCard.jsx'

export default function ProductDetail() {
  const { slug } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  const { addItem, setQuantity, quantityOf, busy } = useCart()

  const [product, setProduct] = useState(null)
  const [related, setRelated] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    setLoading(true)
    setError(null)
    api
      .get(`/products/${slug}`, { auth: false })
      .then(setProduct)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [slug])

  useEffect(() => {
    if (!product) return
    api
      .get(`/products?category=${product.categorySlug}&size=8`, { auth: false })
      .then((page) => setRelated(page.content.filter((p) => p.id !== product.id).slice(0, 4)))
      .catch(() => setRelated([]))
  }, [product])

  if (loading) return <Loader label="Loading product" />

  if (error || !product) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-20 text-center">
        <p className="text-5xl">🤷</p>
        <h1 className="mt-4 text-xl font-bold text-stone-900">We could not find that product</h1>
        <p className="mt-1 text-sm text-stone-500">{error}</p>
        <Link to="/shop" className="mt-6 inline-block rounded-lg bg-brand-600 px-5 py-2 text-sm font-semibold text-white">
          Back to the shop
        </Link>
      </div>
    )
  }

  const inCart = quantityOf(product.id)

  async function onAdd() {
    if (!user) {
      navigate('/login', { state: { from: `/product/${product.slug}` } })
      return
    }
    await addItem(product.id, 1)
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-8">
      <nav className="text-sm text-stone-500">
        <Link to="/shop" className="hover:text-brand-700">
          Shop
        </Link>
        <span className="mx-2">/</span>
        <Link to={`/shop?category=${product.categorySlug}`} className="hover:text-brand-700">
          {product.categoryName}
        </Link>
        <span className="mx-2">/</span>
        <span className="text-stone-700">{product.name}</span>
      </nav>

      <div className="mt-6 grid gap-8 md:grid-cols-2">
        <div className="grid aspect-square place-items-center overflow-hidden rounded-2xl border border-stone-200 bg-white text-[10rem]">
          {product.imageUrl ? (
            <img src={product.imageUrl} alt={product.name} className="h-full w-full object-cover" />
          ) : (
            <span>{product.emoji || '🛒'}</span>
          )}
        </div>

        <div>
          <span className="inline-block rounded-full bg-stone-100 px-3 py-1 text-xs font-semibold text-stone-600">
            {product.categoryName}
          </span>
          <h1 className="mt-3 text-3xl font-bold tracking-tight text-stone-900">{product.name}</h1>
          <p className="mt-1 text-sm text-stone-500">{product.unit}</p>

          <div className="mt-5 flex items-baseline gap-3">
            <span className="text-3xl font-bold text-stone-900">{money(product.price)}</span>
            {product.discountPercent != null && (
              <>
                <span className="text-lg text-stone-400 line-through">{money(product.mrp)}</span>
                <span className="rounded-md bg-brand-100 px-2 py-0.5 text-sm font-bold text-brand-800">
                  Save {product.discountPercent}%
                </span>
              </>
            )}
          </div>

          <p className="mt-5 leading-relaxed text-stone-600">{product.description}</p>

          <div className="mt-6 text-sm">
            <p
              className={`font-semibold ${
                !product.inStock ? 'text-stone-500' : product.lowStock ? 'text-amber-700' : 'text-brand-700'
              }`}
            >
              {product.availabilityLabel}
            </p>
          </div>

          <div className="mt-6 flex flex-wrap items-center gap-3">
            {inCart > 0 ? (
              <div className="w-40">
                <QuantityStepper
                  value={inCart}
                  max={product.stockQty}
                  disabled={busy}
                  onChange={(qty) => setQuantity(product.id, qty)}
                />
              </div>
            ) : (
              <button
                onClick={onAdd}
                disabled={!product.inStock || busy}
                className="rounded-lg bg-brand-600 px-8 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-stone-300"
              >
                Add to cart
              </button>
            )}

            {inCart > 0 && (
              <Link
                to="/cart"
                className="rounded-lg border border-stone-300 px-6 py-3 text-sm font-semibold text-stone-700 transition hover:bg-stone-50"
              >
                Go to cart
              </Link>
            )}
          </div>

          <dl className="mt-8 grid grid-cols-2 gap-4 border-t border-stone-200 pt-6 text-sm">
            <div>
              <dt className="text-stone-500">Delivery</dt>
              <dd className="mt-0.5 font-semibold text-stone-800">Same day, 8am–9pm</dd>
            </div>
            <div>
              <dt className="text-stone-500">Payment</dt>
              <dd className="mt-0.5 font-semibold text-stone-800">Online or cash on delivery</dd>
            </div>
          </dl>
        </div>
      </div>

      {related.length > 0 && (
        <section className="mt-16">
          <h2 className="text-xl font-bold text-stone-900">More from {product.categoryName}</h2>
          <div className="mt-5 grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
            {related.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
        </section>
      )}
    </div>
  )
}
