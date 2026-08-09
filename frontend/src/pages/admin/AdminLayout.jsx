import { NavLink, Outlet } from 'react-router-dom'

const TABS = [
  { to: '/admin', label: 'Dashboard', end: true },
  { to: '/admin/products', label: 'Products' },
  { to: '/admin/orders', label: 'Orders' },
]

export default function AdminLayout() {
  return (
    <div className="mx-auto max-w-7xl px-4 py-8">
      <header>
        <h1 className="text-2xl font-bold text-stone-900">Store admin</h1>
        <p className="mt-1 text-sm text-stone-500">Everything behind the counter.</p>
      </header>

      <nav className="mt-6 flex gap-1 border-b border-stone-200">
        {TABS.map((tab) => (
          <NavLink
            key={tab.to}
            to={tab.to}
            end={tab.end}
            className={({ isActive }) =>
              `-mb-px border-b-2 px-4 py-2.5 text-sm font-semibold transition ${
                isActive
                  ? 'border-brand-600 text-brand-700'
                  : 'border-transparent text-stone-500 hover:text-stone-800'
              }`
            }
          >
            {tab.label}
          </NavLink>
        ))}
      </nav>

      <div className="mt-6">
        <Outlet />
      </div>
    </div>
  )
}
