import { Navigate, Outlet } from 'react-router-dom'
import { useAuthStore } from './auth.store'

/**
 * Wraps protected routes.
 * If no token → redirect to /login.
 * If token exists → render the child route via <Outlet />.
 */
export default function PrivateRoute() {
  const token = useAuthStore((s) => s.token)
  return token ? <Outlet /> : <Navigate to="/login" replace />
}