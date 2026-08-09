import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../lib/api.js'

export default function ForgotPassword() {
  const [email, setEmail] = useState('')
  const [sent, setSent] = useState(null)
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)

  async function onSubmit(e) {
    e.preventDefault()
    setBusy(true)
    setError(null)
    try {
      setSent(await api.post('/auth/forgot-password', { email }, { auth: false }))
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="mx-auto max-w-md px-4 py-12">
      <div className="rounded-2xl border border-stone-200 bg-white p-8">
        <h1 className="text-2xl font-bold text-stone-900">Forgot your password?</h1>
        <p className="mt-1 text-sm text-stone-500">
          Give us the email on your account and we will send a link to set a new password.
        </p>

        {sent ? (
          <div className="mt-6">
            <div className="rounded-lg bg-brand-50 px-4 py-3 text-sm text-brand-900">{sent.message}</div>

            {/* Only present when the backend runs with expose-token on, which
                stands in for the email this project does not send. */}
            {sent.resetToken && (
              <div className="mt-4 rounded-lg border border-amber-200 bg-amber-50 p-3 text-xs text-amber-900">
                <p className="font-semibold">Development mode</p>
                <p className="mt-1">
                  No email is sent in this demo, so here is the link directly:
                </p>
                <Link
                  to={`/reset-password?token=${encodeURIComponent(sent.resetToken)}`}
                  className="mt-2 block break-all font-semibold text-brand-700 underline"
                >
                  Set a new password
                </Link>
              </div>
            )}

            <Link
              to="/login"
              className="mt-5 block py-2 text-center text-sm font-medium text-stone-500 hover:text-brand-700"
            >
              Back to sign in
            </Link>
          </div>
        ) : (
          <form onSubmit={onSubmit} className="mt-6 space-y-4">
            <div>
              <label className="text-sm font-medium text-stone-700">Email</label>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="mt-1 w-full rounded-lg border border-stone-300 px-3 py-2 text-sm outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100"
              />
            </div>

            {error && <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>}

            <button
              type="submit"
              disabled={busy}
              className="w-full rounded-lg bg-brand-600 py-2.5 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:opacity-60"
            >
              {busy ? 'Sending…' : 'Send reset link'}
            </button>

            <Link
              to="/login"
              className="block py-1 text-center text-sm font-medium text-stone-500 hover:text-brand-700"
            >
              Back to sign in
            </Link>
          </form>
        )}
      </div>
    </div>
  )
}
