import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { FlaskConical, Eye, EyeOff } from 'lucide-react'
import { authApi } from './auth.api'
import { useAuthStore } from './auth.store'
import { Spinner } from '../ui'

export default function Login() {
  const navigate  = useNavigate()
  const setAuth   = useAuthStore((s) => s.setAuth)
  const [form, setForm]   = useState({ email: 'demo@pathlab.com', password: 'Admin@123' })
  const [showPw, setShowPw] = useState(false)

  const { mutate: login, isPending, error } = useMutation({
    mutationFn: () => authApi.login(form),
    onSuccess: (data) => {
      setAuth(data)
      toast.success(`Welcome back, ${data.userName}!`)
      navigate('/dashboard')
    },
    onError: (err) => toast.error(err.message)
  })

  const handleSubmit = (e) => {
    e.preventDefault()
    login()
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
      <div className="w-full max-w-md">

        {/* Logo */}
        <div className="text-center mb-8">
          <div className="w-14 h-14 bg-blue-600 rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-lg">
            <FlaskConical size={28} className="text-white" />
          </div>
          <h1 className="text-2xl font-bold text-gray-900">PathLab</h1>
          <p className="text-gray-500 text-sm mt-1">Lab Management System</p>
        </div>

        {/* Card */}
        <div className="card p-8">
          <h2 className="text-lg font-semibold text-gray-800 mb-6">Sign in to your lab</h2>

          {error && (
            <div className="mb-4 rounded-lg bg-red-50 border border-red-200 px-3 py-2 text-sm text-red-700">
              {error.message}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="label">Email address</label>
              <input
                type="email"
                className="input"
                placeholder="you@lab.com"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
                required
                autoFocus
              />
            </div>

            <div>
              <label className="label">Password</label>
              <div className="relative">
                <input
                  type={showPw ? 'text' : 'password'}
                  className="input pr-10"
                  placeholder="••••••••"
                  value={form.password}
                  onChange={(e) => setForm({ ...form, password: e.target.value })}
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowPw(!showPw)}
                  className="absolute right-3 top-2.5 text-gray-400 hover:text-gray-600"
                >
                  {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={isPending}
              className="btn-primary w-full justify-center py-2.5 mt-2"
            >
              {isPending ? <><Spinner className="w-4 h-4" /> Signing in…</> : 'Sign in'}
            </button>
          </form>

          <p className="mt-6 text-xs text-center text-gray-400">
            Demo credentials are pre-filled above
          </p>
        </div>
      </div>
    </div>
  )
}