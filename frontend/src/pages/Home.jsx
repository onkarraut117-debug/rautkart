import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../lib/api.js'
import ProductCard from '../components/ProductCard.jsx'
import Loader from '../components/Loader.jsx'

export default function Home() {
  const [categories, setCategories] = useState([])
  const [featured, setFeatured] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    Promise.all([api.get('/categories', { auth: false }), api.get('/products/featured', { auth: false })])
      .then(([cats, prods]) => {
        setCategories(cats)
        setFeatured(prods)
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div>
      <section className="border-b border-stone-200 bg-gradient-to-br from-brand-50 via-white to-amber-50">
        <div className="mx-auto max-w-7xl px-4 py-16 sm:py-24">
          <div className="max-w-2xl">
            <span className="inline-block rounded-full bg-brand-100 px-3 py-1 text-xs font-semibold text-brand-800">
              Free delivery on orders over ₹500
            </span>
            <h1 className="mt-4 text-4xl font-extrabold tracking-tight text-stone-900 sm:text-5xl">
              Your neighbourhood kirana,{' '}
              <span className="text-brand-600">now a tap away</span>
            </h1>
            <p className="mt-4 text-lg text-stone-600">
              The same rice, dal and fresh produce we have stocked for years — ordered online and
              delivered to your door the same day.
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Link
                to="/shop"
                className="rounded-lg bg-brand-600 px-6 py-3 text-sm font-semibold text-white transition hover:bg-brand-700"
              >
                Start shopping
              </Link>
              <Link
                to="/shop?category=fruits-vegetables"
                className="rounded-lg border border-stone-300 bg-white px-6 py-3 text-sm font-semibold text-stone-700 transition hover:bg-stone-50"
              >
                Today&apos;s fresh produce
              </Link>
            </div>
          </div>
        </div>
      </section>

      {loading ? (
        <Loader label="Loading the shop" />
      ) : error ? (
        <div className="mx-auto max-w-7xl px-4 py-16">
          <div className="rounded-xl border border-amber-200 bg-amber-50 p-6 text-sm text-amber-900">
            <p className="font-semibold">Could not reach the store.</p>
            <p className="mt-1">{error}</p>
            <p className="mt-2 text-amber-800">
              Make sure the Spring Boot backend is running on port 8080.
            </p>
          </div>
        </div>
      ) : (
        <>
          <section className="mx-auto max-w-7xl px-4 py-12">
            <h2 className="text-2xl font-bold text-stone-900">Shop by category</h2>
            <div className="mt-6 grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-5">
              {categories.map((c) => (
                <Link
                  key={c.id}
                  to={`/shop?category=${c.slug}`}
                  className="flex flex-col items-center gap-2 rounded-xl border border-stone-200 bg-white p-5 text-center transition hover:border-brand-300 hover:shadow-md"
                >
                  <span className="text-3xl">{c.icon}</span>
                  <span className="text-sm font-semibold text-stone-700">{c.name}</span>
                </Link>
              ))}
            </div>
          </section>

          <section className="mx-auto max-w-7xl px-4 pb-12">
            <div className="flex items-end justify-between">
              <h2 className="text-2xl font-bold text-stone-900">New in store</h2>
              <Link to="/shop" className="text-sm font-semibold text-brand-700 hover:underline">
                View all →
              </Link>
            </div>
            <div className="mt-6 grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
              {featured.map((p) => (
                <ProductCard key={p.id} product={p} />
              ))}
            </div>
          </section>
        </>
      )}
    </div>
  )
}
