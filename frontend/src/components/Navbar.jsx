import { useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { useCart } from '../context/CartContext.jsx'

export default function Navbar() {
  const { user, logout, isAdmin } = useAuth()
  const { cart } = useCart()
  const navigate = useNavigate()
  const [query, setQuery] = useState('')

  function onSearch(e) {
    e.preventDefault()
    navigate(query.trim() ? `/shop?q=${encodeURIComponent(query.trim())}` : '/shop')
  }

  function onLogout() {
    logout()
    navigate('/')
  }

  const linkClass = ({ isActive }) =>
    `text-sm font-medium transition-colors hover:text-brand-700 ${isActive ? 'text-brand-700' : 'text-stone-600'}`

  return (
    <header className="sticky top-0 z-40 border-b border-stone-200 bg-white/90 backdrop-blur">
      <div className="mx-auto flex max-w-7xl flex-wrap items-center gap-x-6 gap-y-3 px-4 py-3">
        <Link to="/" className="flex shrink-0 items-center gap-2">
          <span className="grid h-9 w-9 place-items-center rounded-lg bg-brand-600 text-lg">🛒</span>
          <span className="text-xl font-bold tracking-tight text-stone-900">
            Raut<span className="text-brand-600">Kart</span>
          </span>
        </Link>

        <form onSubmit={onSearch} className="order-last w-full flex-1 sm:order-none sm:w-auto sm:min-w-[16rem]">
          <div className="relative">
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Search for rice, dal, milk…"
              className="w-full rounded-lg border border-stone-300 bg-stone-50 py-2 pl-9 pr-3 text-sm outline-none transition focus:border-brand-500 focus:bg-white focus:ring-2 focus:ring-brand-100"
            />
            <span className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-stone-400">⌕</span>
          </div>
        </form>

        <nav className="ml-auto flex items-center gap-5">
          <NavLink to="/shop" className={linkClass}>
            Shop
          </NavLink>

          {user && (
            <NavLink to="/orders" className={linkClass}>
              Orders
            </NavLink>
          )}

          {user && (
            <NavLink to="/account" className={linkClass}>
              Account
            </NavLink>
          )}

          {isAdmin && (
            <NavLink to="/admin" className={linkClass}>
              Admin
            </NavLink>
          )}

          <Link to="/cart" className="relative text-stone-600 transition-colors hover:text-brand-700">
            <span className="text-xl">🛍️</span>
            {cart.itemCount > 0 && (
              <span className="absolute -right-2 -top-1 grid h-5 min-w-5 place-items-center rounded-full bg-brand-600 px-1 text-[11px] font-bold text-white">
                {cart.itemCount}
              </span>
            )}
          </Link>

          {user ? (
            <div className="flex items-center gap-3">
              <span className="hidden text-sm text-stone-500 sm:inline">
                Hi, {user.name.split(' ')[0]}
              </span>
              <button
                onClick={onLogout}
                className="rounded-lg border border-stone-300 px-3 py-1.5 text-sm font-medium text-stone-700 transition hover:bg-stone-100"
              >
                Log out
              </button>
            </div>
          ) : (
            <Link
              to="/login"
              className="rounded-lg bg-brand-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-brand-700"
            >
              Log in
            </Link>
          )}
        </nav>
      </div>
    </header>
  )
}
