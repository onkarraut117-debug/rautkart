import { useCallback, useEffect, useState } from 'react'
import { api } from '../../lib/api.js'
import { money } from '../../lib/format.js'
import Loader from '../../components/Loader.jsx'

const BLANK = {
  name: '',
  description: '',
  categoryId: '',
  price: '',
  mrp: '',
  unit: '',
  stockQty: '',
  imageUrl: '',
  emoji: '',
  active: true,
}

export default function AdminProducts() {
  const [page, setPage] = useState(null)
  const [categories, setCategories] = useState([])
  const [pageNo, setPageNo] = useState(0)
  const [loading, setLoading] = useState(true)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(BLANK)
  const [error, setError] = useState(null)
  const [fieldErrors, setFieldErrors] = useState({})
  const [saving, setSaving] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      setPage(await api.get(`/admin/products?page=${pageNo}&size=20`))
    } finally {
      setLoading(false)
    }
  }, [pageNo])

  useEffect(() => {
    load().catch((e) => setError(e.message))
  }, [load])

  useEffect(() => {
    api.get('/categories', { auth: false }).then(setCategories).catch(() => setCategories([]))
  }, [])

  function startCreate() {
    setEditing('new')
    setForm(BLANK)
    setError(null)
    setFieldErrors({})
  }

  function startEdit(p) {
    setEditing(p.id)
    setForm({
      name: p.name,
      description: p.description || '',
      categoryId: String(p.categoryId),
      price: String(p.price),
      mrp: p.mrp == null ? '' : String(p.mrp),
      unit: p.unit,
      stockQty: String(p.stockQty),
      imageUrl: p.imageUrl || '',
      emoji: p.emoji || '',
      active: p.active,
    })
    setError(null)
    setFieldErrors({})
  }

  function set(key, value) {
    setForm((f) => ({ ...f, [key]: value }))
  }

  async function onSave(e) {
    e.preventDefault()
    setSaving(true)
    setError(null)
    setFieldErrors({})

    const body = {
      ...form,
      categoryId: Number(form.categoryId),
      price: form.price,
      mrp: form.mrp === '' ? null : form.mrp,
      stockQty: Number(form.stockQty),
    }

    try {
      if (editing === 'new') await api.post('/admin/products', body)
      else await api.put(`/admin/products/${editing}`, body)
      setEditing(null)
      await load()
    } catch (err) {
      setError(err.message)
      setFieldErrors(err.fieldErrors || {})
    } finally {
      setSaving(false)
    }
  }

  async function onRetire(p) {
    setError(null)
    try {
      await api.del(`/admin/products/${p.id}`)
      await load()
    } catch (err) {
      setError(err.message)
    }
  }

  const inputClass =
    'mt-1 w-full rounded-lg border border-stone-300 px-3 py-2 text-sm outline-none focus:border-brand-500'

  const field = (key, label, extra = {}) => (
    <div className={extra.wide ? 'sm:col-span-2' : ''}>
      <label className="text-xs font-semibold uppercase tracking-wide text-stone-500">{label}</label>
      <input
        type={extra.type || 'text'}
        step={extra.step}
        min={extra.min}
        value={form[key]}
        required={!extra.optional}
        placeholder={extra.placeholder}
        onChange={(e) => set(key, e.target.value)}
        className={inputClass}
      />
      {fieldErrors[key] && <p className="mt-1 text-xs text-red-600">{fieldErrors[key]}</p>}
    </div>
  )

  return (
    <div>
      <div className="flex items-center justify-between">
        <h2 className="font-bold text-stone-900">
          Catalogue {page && <span className="font-normal text-stone-500">({page.totalElements})</span>}
        </h2>
        <button
          onClick={startCreate}
          className="rounded-lg bg-brand-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-brand-700"
        >
          + Add product
        </button>
      </div>

      {error && <p className="mt-4 rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">{error}</p>}

      {editing !== null && (
        <form onSubmit={onSave} className="mt-5 rounded-xl border border-brand-200 bg-brand-50/40 p-5">
          <h3 className="font-bold text-stone-900">{editing === 'new' ? 'New product' : 'Edit product'}</h3>

          <div className="mt-4 grid gap-4 sm:grid-cols-2">
            {field('name', 'Name', { wide: true })}

            <div className="sm:col-span-2">
              <label className="text-xs font-semibold uppercase tracking-wide text-stone-500">Description</label>
              <textarea
                rows={2}
                value={form.description}
                onChange={(e) => set('description', e.target.value)}
                className={inputClass}
              />
            </div>

            <div>
              <label className="text-xs font-semibold uppercase tracking-wide text-stone-500">Category</label>
              <select
                value={form.categoryId}
                required
                onChange={(e) => set('categoryId', e.target.value)}
                className={inputClass}
              >
                <option value="">Choose…</option>
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>

            {field('unit', 'Unit', { placeholder: '1 kg, 500 g, 12 pcs' })}
            {field('price', 'Price (₹)', { type: 'number', step: '0.01', min: '0.01' })}
            {field('mrp', 'MRP (₹)', { type: 'number', step: '0.01', min: '0', optional: true })}
            {field('stockQty', 'Stock quantity', { type: 'number', min: '0' })}
            {field('emoji', 'Emoji', { optional: true, placeholder: '🍚' })}
            {field('imageUrl', 'Image URL', { wide: true, optional: true, placeholder: 'https://…' })}
          </div>

          <label className="mt-4 flex cursor-pointer items-center gap-2 text-sm text-stone-700">
            <input
              type="checkbox"
              checked={form.active}
              onChange={(e) => set('active', e.target.checked)}
              className="h-4 w-4 accent-brand-600"
            />
            Visible in the storefront
          </label>

          <div className="mt-5 flex gap-2">
            <button
              type="submit"
              disabled={saving}
              className="rounded-lg bg-brand-600 px-5 py-2 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:opacity-60"
            >
              {saving ? 'Saving…' : 'Save product'}
            </button>
            <button
              type="button"
              onClick={() => setEditing(null)}
              className="rounded-lg border border-stone-300 px-5 py-2 text-sm font-semibold text-stone-700 transition hover:bg-white"
            >
              Cancel
            </button>
          </div>
        </form>
      )}

      {loading ? (
        <Loader label="Loading catalogue" />
      ) : (
        <div className="mt-5 overflow-x-auto rounded-xl border border-stone-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-stone-200 bg-stone-50 text-xs uppercase tracking-wide text-stone-500">
              <tr>
                <th className="px-4 py-3">Product</th>
                <th className="px-4 py-3">Category</th>
                <th className="px-4 py-3 text-right">Price</th>
                <th className="px-4 py-3 text-right">Stock</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-stone-100">
              {page?.content.map((p) => (
                <tr key={p.id} className="hover:bg-stone-50">
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-3">
                      <span className="text-xl">{p.emoji || '🛒'}</span>
                      <div>
                        <p className="font-semibold text-stone-800">{p.name}</p>
                        <p className="text-xs text-stone-500">{p.unit}</p>
                      </div>
                    </div>
                  </td>
                  <td className="px-4 py-3 text-stone-600">{p.categoryName}</td>
                  <td className="px-4 py-3 text-right font-medium text-stone-800">{money(p.price)}</td>
                  <td className="px-4 py-3 text-right">
                    <span className={p.lowStock ? 'font-bold text-amber-700' : 'text-stone-700'}>{p.stockQty}</span>
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className={`rounded-full px-2 py-0.5 text-xs font-semibold ${
                        p.active ? 'bg-brand-100 text-brand-800' : 'bg-stone-200 text-stone-600'
                      }`}
                    >
                      {p.active ? 'Live' : 'Retired'}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <button
                      onClick={() => startEdit(p)}
                      className="text-sm font-semibold text-brand-700 hover:underline"
                    >
                      Edit
                    </button>
                    {p.active && (
                      <button
                        onClick={() => onRetire(p)}
                        className="ml-3 text-sm font-medium text-stone-500 hover:text-red-600"
                      >
                        Retire
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
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
