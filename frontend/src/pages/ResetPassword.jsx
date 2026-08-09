import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { api } from '../lib/api.js'

export default function ResetPassword() {
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const token = params.get('token') || ''

  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)
  const [done, setDone] = useState(false)

  async function onSubmit(e) {
    e.preventDefault()
    if (password !== confirm) {
      setError('The two passwords do not match')
      return
    }
    setBusy(true)
    setError(null)
    try {
      await api.post('/auth/reset-password', { token, newPassword: password }, { auth: false })
      setDone(true)
      setTimeout(() => navigate('/login', { replace: true }), 2500)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  const inputClass =
    'mt-1 w-full rounded-lg border border-stone-300 px-3 py-2 text-sm outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100'

  if (!token) {
    return (
      <div className="mx-auto max-w-md px-4 py-12">
        <div className="rounded-2xl border border-stone-200 bg-white p-8 text-center">
          <p className="text-4xl">🔗</p>
          <h1 className="mt-4 text-xl font-bold text-stone-900">This link is incomplete</h1>
          <p className="mt-1 text-sm text-stone-500">
            Open the link from your email, or request a new one.
          </p>
          <Link
            to="/forgot-password"
            className="mt-6 inline-block rounded-lg bg-brand-600 px-5 py-2 text-sm font-semibold text-white"
          >
            Request a new link
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-md px-4 py-12">
      <div className="rounded-2xl border border-stone-200 bg-white p-8">
        <h1 className="text-2xl font-bold text-stone-900">Set a new password</h1>

        {done ? (
          <div className="mt-6">
            <div className="rounded-lg bg-brand-50 px-4 py-3 text-sm text-brand-900">
              Your password has been changed. Taking you to sign in…
            </div>
          </div>
        ) : (
          <>
            <p className="mt-1 text-sm text-stone-500">Choose something you have not used here before.</p>

            <form onSubmit={onSubmit} className="mt-6 space-y-4">
              <div>
                <label className="text-sm font-medium text-stone-700">New password</label>
                <input
                  type="password"
                  required
                  minLength={6}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className={inputClass}
                />
                <p className="mt-1 text-xs text-stone-400">At least 6 characters.</p>
              </div>

              <div>
                <label className="text-sm font-medium text-stone-700">Confirm new password</label>
                <input
                  type="password"
                  required
                  value={confirm}
                  onChange={(e) => setConfirm(e.target.value)}
                  className={inputClass}
                />
              </div>

              {error && <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>}

              <button
                type="submit"
                disabled={busy}
                className="w-full rounded-lg bg-brand-600 py-2.5 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:opacity-60"
              >
                {busy ? 'Saving…' : 'Change my password'}
              </button>
            </form>
          </>
        )}
      </div>
    </div>
  )
}
