import { Link } from 'react-router-dom'

export default function EmptyState({ icon = '📦', title, description, actionTo, actionLabel }) {
  return (
    <div className="grid place-items-center rounded-xl border border-dashed border-stone-300 bg-white px-6 py-16 text-center">
      <span className="text-5xl">{icon}</span>
      <h2 className="mt-4 text-lg font-semibold text-stone-800">{title}</h2>
      {description && <p className="mt-1 max-w-sm text-sm text-stone-500">{description}</p>}
      {actionTo && (
        <Link
          to={actionTo}
          className="mt-5 rounded-lg bg-brand-600 px-5 py-2 text-sm font-semibold text-white transition hover:bg-brand-700"
        >
          {actionLabel}
        </Link>
      )}
    </div>
  )
}
