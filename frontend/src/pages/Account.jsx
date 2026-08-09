import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../lib/api.js'
import { useAuth } from '../context/AuthContext.jsx'

export default function Account() {
  const { user } = useAuth()

  const [form, setForm] = useState({ currentPassword: '', newPassword: '', confirm: '' })
  const [error, setError] = useState(null)
  const [fieldErrors, setFieldErrors] = useState({})
  const [done, setDone] = useState(null)
  const [busy, setBusy] = useState(false)

  function set(key, value) {
    setForm((f) => ({ ...f, [key]: value }))
  }

  async function onSubmit(e) {
    e.preventDefault()
    setDone(null)
    setError(null)
    setFieldErrors({})

    if (form.newPassword !== form.confirm) {
      setError('The two new passwords do not match')
      return
    }

    setBusy(true)
    try {
      const res = await api.post('/auth/change-password', {
        currentPassword: form.currentPassword,
        newPassword: form.newPassword,
      })
      setDone(res.message)
      setForm({ currentPassword: '', newPassword: '', confirm: '' })
    } catch (err) {
      setError(err.message)
      setFieldErrors(err.fieldErrors || {})
    } finally {
      setBusy(false)
    }
  }

  const inputClass =
    'mt-1 w-full rounded-lg border border-stone-300 px-3 py-2 text-sm outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100'

  return (
    <div className="mx-auto max-w-3xl px-4 py-8">
      <h1 className="text-2xl font-bold text-stone-900">Your account</h1>

      <section className="mt-6 rounded-xl border border-stone-200 bg-white p-5">
        <h2 className="font-bold text-stone-900">Details</h2>
        <dl className="mt-4 grid gap-4 text-sm sm:grid-cols-2">
          <div>
            <dt className="text-stone-500">Name</dt>
            <dd className="mt-0.5 font-medium text-stone-800">{user?.name}</dd>
          </div>
          <div>
            <dt className="text-stone-500">Email</dt>
            <dd className="mt-0.5 font-medium text-stone-800">{user?.email}</dd>
          </div>
          <div>
            <dt className="text-stone-500">Phone</dt>
            <dd className="mt-0.5 font-medium text-stone-800">{user?.phone || '—'}</dd>
          </div>
          <div>
            <dt className="text-stone-500">Account type</dt>
            <dd className="mt-0.5 font-medium text-stone-800">
              {user?.role === 'ADMIN' ? 'Store admin' : 'Customer'}
            </dd>
          </div>
        </dl>
      </section>

      <section className="mt-6 rounded-xl border border-stone-200 bg-white p-5">
        <h2 className="font-bold text-stone-900">Change password</h2>
        <p className="mt-1 text-sm text-stone-500">
          You will need your current password. Any outstanding reset links stop working.
        </p>

        <form onSubmit={onSubmit} className="mt-5 max-w-sm space-y-4">
          <div>
            <label className="text-sm font-medium text-stone-700">Current password</label>
            <input
              type="password"
              required
              value={form.currentPassword}
              onChange={(e) => set('currentPassword', e.target.value)}
              className={inputClass}
            />
          </div>

          <div>
            <label className="text-sm font-medium text-stone-700">New password</label>
            <input
              type="password"
              required
              minLength={6}
              value={form.newPassword}
              onChange={(e) => set('newPassword', e.target.value)}
              className={inputClass}
            />
            {fieldErrors.newPassword && (
              <p className="mt-1 text-xs text-red-600">{fieldErrors.newPassword}</p>
            )}
          </div>

          <div>
            <label className="text-sm font-medium text-stone-700">Confirm new password</label>
            <input
              type="password"
              required
              value={form.confirm}
              onChange={(e) => set('confirm', e.target.value)}
              className={inputClass}
            />
          </div>

          {error && <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>}
          {done && <p className="rounded-lg bg-brand-50 px-3 py-2 text-sm text-brand-900">{done}</p>}

          <button
            type="submit"
            disabled={busy}
            className="rounded-lg bg-brand-600 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:opacity-60"
          >
            {busy ? 'Updating…' : 'Update password'}
          </button>
        </form>
      </section>

      <Link to="/orders" className="mt-6 inline-block text-sm font-semibold text-brand-700 hover:underline">
        View your orders →
      </Link>
    </div>
  )
}
