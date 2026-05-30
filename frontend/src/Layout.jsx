import { Outlet } from 'react-router-dom'
import Sidebar from './Sidebar'

export default function Layout() {
  return (
    <div className="flex min-h-screen bg-gray-50">
      <Sidebar />
      {/* Main content area */}
      <main className="flex-1 overflow-auto">
        <div className="max-w-6xl mx-auto px-6 py-6">
          <Outlet />
        </div>
      </main>
    </div>
  )
}