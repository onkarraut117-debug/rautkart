import { useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { api } from '../lib/api.js'
import ProductCard from '../components/ProductCard.jsx'
import Loader from '../components/Loader.jsx'
import EmptyState from '../components/EmptyState.jsx'

const SORTS = [
  { value: 'name', label: 'Name (A–Z)' },
  { value: 'price_asc', label: 'Price: low to high' },
  { value: 'price_desc', label: 'Price: high to low' },
  { value: 'newest', label: 'Newest first' },
]

export default function Catalog() {
  const [params, setParams] = useSearchParams()
  const [categories, setCategories] = useState([])
  const [page, setPage] = useState(null)
  const [loading, setLoading] = useState(true)

  const q = params.get('q') || ''
  const category = params.get('category') || ''
  const sort = params.get('sort') || 'name'
  const maxPrice = params.get('maxPrice') || ''
  const inStockOnly = params.get('inStockOnly') === 'true'
  const pageNo = Number(params.get('page') || 0)

  const queryString = useMemo(() => {
    const sp = new URLSearchParams()
    if (q) sp.set('q', q)
    if (category) sp.set('category', category)
    if (maxPrice) sp.set('maxPrice', maxPrice)
    if (inStockOnly) sp.set('inStockOnly', 'true')
    sp.set('sort', sort)
    sp.set('page', String(pageNo))
    sp.set('size', '12')
    return sp.toString()
  }, [q, category, maxPrice, inStockOnly, sort, pageNo])

  useEffect(() => {
    api.get('/categories', { auth: false }).then(setCategories).catch(() => setCategories([]))
  }, [])

  useEffect(() => {
    setLoading(true)
    api
      .get(`/products?${queryString}`, { auth: false })
      .then(setPage)
      .catch(() => setPage(null))
      .finally(() => setLoading(false))
  }, [queryString])

  /** Merge one filter change into the URL, resetting pagination. */
  function update(key, value) {
    const next = new URLSearchParams(params)
    if (value === '' || value === false || value == null) next.delete(key)
    else next.set(key, String(value))
    if (key !== 'page') next.delete('page')
    setParams(next)
  }

  function clearAll() {
    setParams(new URLSearchParams())
  }

  const hasFilters = Boolean(q || category || maxPrice || inStockOnly)
  const activeCategory = categories.find((c) => c.slug === category)

  return (
    <div className="mx-auto max-w-7xl px-4 py-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-stone-900">
            {activeCategory ? `${activeCategory.icon} ${activeCategory.name}` : 'All products'}
          </h1>
          <p className="mt-1 text-sm text-stone-500">
            {q && (
              <>
                Results for <span className="font-semibold text-stone-700">“{q}”</span> ·{' '}
              </>
            )}
            {page ? `${page.totalElements} item${page.totalElements === 1 ? '' : 's'}` : '…'}
          </p>
        </div>

        <select
          value={sort}
          onChange={(e) => update('sort', e.target.value)}
          className="rounded-lg border border-stone-300 bg-white px-3 py-2 text-sm outline-none focus:border-brand-500"
        >
          {SORTS.map((s) => (
            <option key={s.value} value={s.value}>
              {s.label}
            </option>
          ))}
        </select>
      </div>

      <div className="mt-6 grid gap-6 lg:grid-cols-[14rem_1fr]">
        <aside className="space-y-6 lg:sticky lg:top-24 lg:self-start">
          <div className="rounded-xl border border-stone-200 bg-white p-4">
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-bold text-stone-900">Filters</h2>
              {hasFilters && (
                <button onClick={clearAll} className="text-xs font-semibold text-brand-700 hover:underline">
                  Clear
                </button>
              )}
            </div>

            <div className="mt-4">
              <p className="text-xs font-semibold uppercase tracking-wide text-stone-400">Category</p>
              <ul className="mt-2 space-y-1">
                <li>
                  <button
                    onClick={() => update('category', '')}
                    className={`w-full rounded-md px-2 py-1.5 text-left text-sm transition hover:bg-stone-100 ${
                      !category ? 'bg-brand-50 font-semibold text-brand-800' : 'text-stone-600'
                    }`}
                  >
                    All categories
                  </button>
                </li>
                {categories.map((c) => (
                  <li key={c.id}>
                    <button
                      onClick={() => update('category', c.slug)}
                      className={`w-full rounded-md px-2 py-1.5 text-left text-sm transition hover:bg-stone-100 ${
                        category === c.slug ? 'bg-brand-50 font-semibold text-brand-800' : 'text-stone-600'
                      }`}
                    >
                      <span className="mr-1.5">{c.icon}</span>
                      {c.name}
                    </button>
                  </li>
                ))}
              </ul>
            </div>

            <div className="mt-5 border-t border-stone-100 pt-4">
              <label className="text-xs font-semibold uppercase tracking-wide text-stone-400">
                Max price
              </label>
              <input
                type="number"
                min="0"
                value={maxPrice}
                placeholder="Any"
                onChange={(e) => update('maxPrice', e.target.value)}
                className="mt-2 w-full rounded-lg border border-stone-300 px-3 py-1.5 text-sm outline-none focus:border-brand-500"
              />
            </div>

            <label className="mt-4 flex cursor-pointer items-center gap-2 text-sm text-stone-600">
              <input
                type="checkbox"
                checked={inStockOnly}
                onChange={(e) => update('inStockOnly', e.target.checked)}
                className="h-4 w-4 rounded border-stone-300 accent-brand-600"
              />
              In stock only
            </label>
          </div>
        </aside>

        <div>
          {loading ? (
            <Loader label="Fetching products" />
          ) : !page || page.content.length === 0 ? (
            <EmptyState
              icon="🔍"
              title="Nothing matched that"
              description="Try a different search term, or clear the filters to see the full catalogue."
              actionTo="/shop"
              actionLabel="Browse everything"
            />
          ) : (
            <>
              <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 xl:grid-cols-4">
                {page.content.map((p) => (
                  <ProductCard key={p.id} product={p} />
                ))}
              </div>

              {page.totalPages > 1 && (
                <div className="mt-8 flex items-center justify-center gap-2">
                  <button
                    disabled={pageNo <= 0}
                    onClick={() => update('page', pageNo - 1)}
                    className="rounded-lg border border-stone-300 bg-white px-4 py-2 text-sm font-medium transition hover:bg-stone-50 disabled:opacity-40"
                  >
                    Previous
                  </button>
                  <span className="px-3 text-sm text-stone-500">
                    Page {page.page + 1} of {page.totalPages}
                  </span>
                  <button
                    disabled={pageNo >= page.totalPages - 1}
                    onClick={() => update('page', pageNo + 1)}
                    className="rounded-lg border border-stone-300 bg-white px-4 py-2 text-sm font-medium transition hover:bg-stone-50 disabled:opacity-40"
                  >
                    Next
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  )
}
