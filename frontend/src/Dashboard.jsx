import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { ClipboardList, Users, FileText, IndianRupee, Plus, ArrowRight } from 'lucide-react'
import { ordersApi } from './orders.api'
import { useAuthStore } from './auth.store'
import { StatusBadge, StatCard, PageLoader } from '../ui'
import { format } from 'date-fns'

export default function Dashboard() {
  const { lab, user } = useAuthStore()
  const navigate = useNavigate()

  // Fetch all orders for stats
  const { data: ordersPage, isLoading } = useQuery({
    queryKey: ['orders', 'all'],
    queryFn: () => ordersApi.getAll({ page: 0, size: 100 }),
  })

  const orders = ordersPage?.content ?? []

  // Quick stats computed from orders
  const today = format(new Date(), 'yyyy-MM-dd')
  const todayOrders   = orders.filter(o => o.createdAt?.startsWith(today))
  const pendingReport = orders.filter(o => o.status === 'REPORT_READY')
  const processing    = orders.filter(o => ['REGISTERED','SAMPLE_COLLECTED','PROCESSING'].includes(o.status))
  const todayRevenue  = todayOrders.reduce((sum, o) => sum + (o.netAmount ?? 0), 0)

  if (isLoading) return <PageLoader />

  return (
    <div>
      {/* Greeting */}
      <div className="mb-6">
        <h1 className="text-xl font-semibold text-gray-900">
          Good morning, {user?.name?.split(' ')[0]} 👋
        </h1>
        <p className="text-sm text-gray-500 mt-0.5">{lab?.name} · {format(new Date(), 'EEEE, dd MMM yyyy')}</p>
      </div>

      {/* Stat cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatCard label="Today's Orders"   value={todayOrders.length}          icon={ClipboardList} color="blue"   />
        <StatCard label="Pending Reports"  value={pendingReport.length}         icon={FileText}      color="purple" sub="Need to generate PDF" />
        <StatCard label="In Progress"      value={processing.length}            icon={Users}         color="amber"  sub="Registered / Processing" />
        <StatCard label="Today's Revenue"  value={`₹${todayRevenue.toFixed(0)}`} icon={IndianRupee}  color="green"  />
      </div>

      {/* Quick actions */}
      <div className="grid grid-cols-3 gap-3 mb-8">
        {[
          { label: 'New Order',    sub: 'Register a patient visit',  action: () => navigate('/orders/new'),    color: 'bg-blue-600' },
          { label: 'New Patient',  sub: 'Register a new patient',    action: () => navigate('/patients/new'), color: 'bg-teal-600' },
          { label: 'View Orders',  sub: 'All orders and statuses',   action: () => navigate('/orders'),       color: 'bg-indigo-600' },
        ].map((q) => (
          <button
            key={q.label}
            onClick={q.action}
            className="card p-4 text-left hover:shadow-md transition-all group"
          >
            <div className={`w-8 h-8 ${q.color} rounded-lg flex items-center justify-center mb-3`}>
              <Plus size={16} className="text-white" />
            </div>
            <p className="font-medium text-gray-900 text-sm">{q.label}</p>
            <p className="text-xs text-gray-400 mt-0.5">{q.sub}</p>
          </button>
        ))}
      </div>

      {/* Recent orders */}
      <div className="card">
        <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100">
          <h2 className="font-medium text-gray-800">Recent Orders</h2>
          <button onClick={() => navigate('/orders')} className="text-xs text-blue-600 hover:underline flex items-center gap-1">
            View all <ArrowRight size={12} />
          </button>
        </div>

        {orders.length === 0 ? (
          <div className="py-10 text-center text-sm text-gray-400">No orders yet. Create your first order.</div>
        ) : (
          <div className="table-wrap rounded-none border-0">
            <table className="table">
              <thead>
                <tr>
                  <th>Order No.</th>
                  <th>Patient</th>
                  <th>Tests</th>
                  <th>Amount</th>
                  <th>Status</th>
                  <th>Date</th>
                </tr>
              </thead>
              <tbody>
                {orders.slice(0, 8).map((order) => (
                  <tr
                    key={order.id}
                    className="cursor-pointer"
                    onClick={() => navigate(`/orders/${order.id}`)}
                  >
                    <td className="font-mono text-xs text-blue-600">{order.orderNumber}</td>
                    <td className="font-medium">{order.patient?.name ?? '—'}</td>
                    <td className="text-gray-500">{order.items?.length ?? 0} test{order.items?.length !== 1 ? 's' : ''}</td>
                    <td className="font-medium">₹{order.netAmount?.toFixed(0)}</td>
                    <td><StatusBadge status={order.status} /></td>
                    <td className="text-gray-400 text-xs">
                      {order.createdAt ? format(new Date(order.createdAt), 'dd MMM, HH:mm') : '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}