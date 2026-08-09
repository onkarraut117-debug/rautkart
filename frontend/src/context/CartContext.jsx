import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { api } from '../lib/api.js'
import { useAuth } from './AuthContext.jsx'

const CartContext = createContext(null)

const EMPTY = {
  items: [],
  itemCount: 0,
  subtotal: 0,
  deliveryFee: 0,
  total: 0,
  freeDeliveryAbove: 0,
  amountForFreeDelivery: 0,
}

/**
 * The cart lives on the server, keyed by user, so it survives refreshes and
 * follows the customer to another device. Logged-out visitors get an empty cart
 * and are pushed to login when they try to add something.
 */
export function CartProvider({ children }) {
  const { user } = useAuth()
  const [cart, setCart] = useState(EMPTY)
  const [busy, setBusy] = useState(false)

  const refresh = useCallback(async () => {
    if (!user) {
      setCart(EMPTY)
      return
    }
    const data = await api.get('/cart')
    setCart(data)
  }, [user])

  useEffect(() => {
    refresh().catch(() => setCart(EMPTY))
  }, [refresh])

  const addItem = useCallback(async (productId, quantity = 1) => {
    setBusy(true)
    try {
      setCart(await api.post('/cart/items', { productId, quantity }))
    } finally {
      setBusy(false)
    }
  }, [])

  const setQuantity = useCallback(async (productId, quantity) => {
    setBusy(true)
    try {
      setCart(await api.put(`/cart/items/${productId}`, { quantity }))
    } finally {
      setBusy(false)
    }
  }, [])

  const removeItem = useCallback(async (productId) => {
    setBusy(true)
    try {
      setCart(await api.del(`/cart/items/${productId}`))
    } finally {
      setBusy(false)
    }
  }, [])

  const clear = useCallback(async () => {
    setCart(await api.del('/cart'))
  }, [])

  const quantityOf = useCallback(
    (productId) => cart.items.find((i) => i.productId === productId)?.quantity ?? 0,
    [cart.items],
  )

  const value = useMemo(
    () => ({ cart, busy, refresh, addItem, setQuantity, removeItem, clear, quantityOf }),
    [cart, busy, refresh, addItem, setQuantity, removeItem, clear, quantityOf],
  )

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}

export function useCart() {
  const ctx = useContext(CartContext)
  if (!ctx) throw new Error('useCart must be used inside CartProvider')
  return ctx
}
