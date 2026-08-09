const SCRIPT_SRC = 'https://checkout.razorpay.com/v1/checkout.js'

let loading = null

/** Injects the Razorpay checkout script once and resolves when it is ready. */
export function loadRazorpay() {
  if (window.Razorpay) return Promise.resolve(true)
  if (loading) return loading

  loading = new Promise((resolve) => {
    const script = document.createElement('script')
    script.src = SCRIPT_SRC
    script.onload = () => resolve(true)
    script.onerror = () => {
      loading = null
      resolve(false)
    }
    document.body.appendChild(script)
  })

  return loading
}

/**
 * Opens the Razorpay widget and resolves with the handler payload, or null if
 * the customer dismisses it.
 */
export function openRazorpay({ keyId, razorpayOrderId, amountInPaise, order, user }) {
  return new Promise((resolve, reject) => {
    const rzp = new window.Razorpay({
      key: keyId,
      amount: amountInPaise,
      currency: 'INR',
      name: 'RautKart',
      description: `Order ${order.orderNumber}`,
      order_id: razorpayOrderId,
      prefill: {
        name: order.shipName || user?.name,
        email: user?.email,
        contact: order.shipPhone,
      },
      theme: { color: '#16a34a' },
      handler: (response) => resolve(response),
      modal: { ondismiss: () => resolve(null) },
    })

    rzp.on('payment.failed', (response) => reject(new Error(response.error?.description || 'Payment failed')))
    rzp.open()
  })
}
