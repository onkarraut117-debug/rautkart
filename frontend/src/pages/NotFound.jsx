import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <div className="mx-auto max-w-lg px-4 py-24 text-center">
      <p className="text-6xl">🧺</p>
      <h1 className="mt-6 text-3xl font-bold text-stone-900">This aisle is empty</h1>
      <p className="mt-2 text-stone-500">The page you were looking for does not exist.</p>
      <Link
        to="/"
        className="mt-8 inline-block rounded-lg bg-brand-600 px-6 py-3 text-sm font-semibold text-white transition hover:bg-brand-700"
      >
        Back to the shop
      </Link>
    </div>
  )
}
