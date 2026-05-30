import { NavLink, useNavigate } from 'react-router-dom'
import {
  LayoutDashboard, Users, ClipboardList, TestTube2,
  UserCog, CreditCard, LogOut, FlaskConical
} from 'lucide-react'
import { useAuthStore } from './auth.store'
import clsx from 'clsx'

const navItems = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/patients',  icon: Users,           label: 'Patients'  },
  { to: '/orders',    icon: ClipboardList,   label: 'Orders'    },
  { to: '/catalog',   icon: TestTube2,       label: 'Test Catalog' },
  { to: '/payments',  icon: CreditCard,      label: 'Payments'  },
  { to: '/staff',     icon: UserCog,         label: 'Staff',  ownerOnly: true },
]

export default function Sidebar() {
  const { user, lab, clearAuth } = useAuthStore()
  const navigate = useNavigate()

  const handleLogout = () => {
    clearAuth()
    navigate('/login')
  }

  return (
    <aside className="w-60 min-h-screen bg-white border-r border-gray-200 flex flex-col">

      {/* Logo + lab name */}
      <div className="px-5 py-5 border-b border-gray-100">
        <div className="flex items-center gap-2 mb-1">
          <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center">
            <FlaskConical size={16} className="text-white" />
          </div>
          <span className="font-bold text-blue-700 text-base">PathLab</span>
        </div>
        <p className="text-xs text-gray-500 truncate pl-10">{lab?.name}</p>
      </div>

      {/* Navigation links */}
      <nav className="flex-1 px-3 py-4 space-y-0.5">
        {navItems.map((item) => {
          // Hide staff page for non-owners
          if (item.ownerOnly && user?.role !== 'OWNER') return null
          const Icon = item.icon
          return (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => clsx(
                'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors',
                isActive
                  ? 'bg-blue-50 text-blue-700'
                  : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
              )}
            >
              <Icon size={17} />
              {item.label}
            </NavLink>
          )
        })}
      </nav>

      {/* User info + logout */}
      <div className="px-4 py-4 border-t border-gray-100">
        <div className="mb-3">
          <p className="text-sm font-medium text-gray-800 truncate">{user?.name}</p>
          <p className="text-xs text-gray-400 truncate">{user?.role}</p>
        </div>
        <button
          onClick={handleLogout}
          className="flex items-center gap-2 text-sm text-gray-500 hover:text-red-600 transition-colors w-full"
        >
          <LogOut size={15} />
          Sign out
        </button>
      </div>
    </aside>
  )
}