import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import Loader from './Loader.jsx'

/**
 * Gate for routes that need a session. `adminOnly` additionally requires the
 * ADMIN role and bounces everyone else to the admin login.
 */
export default function ProtectedRoute({ children, adminOnly = false }) {
  const { user, loading, isAdmin } = useAuth()
  const location = useLocation()

  if (loading) return <Loader label="Checking your session" />

  if (!user) {
    const to = adminOnly ? '/admin/login' : '/login'
    return <Navigate to={to} replace state={{ from: location.pathname }} />
  }

  if (adminOnly && !isAdmin) {
    return <Navigate to="/admin/login" replace />
  }

  return children
}
