import { Link } from 'react-router-dom'

export default function Footer() {
  return (
    <footer className="mt-16 border-t border-stone-200 bg-white">
      <div className="mx-auto grid max-w-7xl gap-8 px-4 py-10 sm:grid-cols-3">
        <div>
          <div className="flex items-center gap-2">
            <span className="grid h-8 w-8 place-items-center rounded-lg bg-brand-600 text-base">🛒</span>
            <span className="text-lg font-bold text-stone-900">
              Raut<span className="text-brand-600">Kart</span>
            </span>
          </div>
          <p className="mt-3 max-w-xs text-sm text-stone-500">
            The neighbourhood kirana shop, online. Fresh stock, fair prices, delivered to your door.
          </p>
        </div>

        <div>
          <h3 className="text-sm font-semibold text-stone-900">Shop</h3>
          <ul className="mt-3 space-y-2 text-sm text-stone-500">
            <li>
              <Link to="/shop" className="hover:text-brand-700">
                All products
              </Link>
            </li>
            <li>
              <Link to="/shop?category=fruits-vegetables" className="hover:text-brand-700">
                Fruits &amp; vegetables
              </Link>
            </li>
            <li>
              <Link to="/shop?category=dals-pulses" className="hover:text-brand-700">
                Dals &amp; pulses
              </Link>
            </li>
          </ul>
        </div>

        <div>
          <h3 className="text-sm font-semibold text-stone-900">Store</h3>
          <ul className="mt-3 space-y-2 text-sm text-stone-500">
            <li>Open 8:00 – 21:00, all week</li>
            <li>Free delivery over ₹500</li>
            <li>Cash on delivery available</li>
          </ul>
        </div>
      </div>

      <div className="border-t border-stone-100 py-4 text-center text-xs text-stone-400">
        Portfolio demo project — payments run in Razorpay test mode, no real transactions.
      </div>
    </footer>
  )
}
