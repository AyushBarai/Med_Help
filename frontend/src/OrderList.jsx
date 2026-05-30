import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Plus, Search } from 'lucide-react'
import { ordersApi } from './orders.api'
import { StatusBadge, PageLoader, EmptyState, SectionHead } from '../ui'
import { format } from 'date-fns'

const STATUSES = [
  { label: 'All',             value: null },
  { label: 'Registered',      value: 'REGISTERED' },
  { label: 'Sample Collected',value: 'SAMPLE_COLLECTED' },
  { label: 'Processing',      value: 'PROCESSING' },
  { label: 'Report Ready',    value: 'REPORT_READY' },
  { label: 'Delivered',       value: 'DELIVERED' },
]

export default function OrderList() {
  const navigate = useNavigate()
  const [activeStatus, setActiveStatus] = useState(null)
  const [page, setPage] = useState(0)

  const { data, isLoading } = useQuery({
    queryKey: ['orders', activeStatus, page],
    queryFn: () => ordersApi.getAll({ status: activeStatus, page, size: 20 }),
  })

  const orders = data?.content ?? []
  const totalPages = data?.totalPages ?? 0

  return (
    <div>
      <SectionHead
        title="Orders"
        action={
          <button onClick={() => navigate('/orders/new')} className="btn-primary">
            <Plus size={16} /> New Order
          </button>
        }
      />

      {/* Status filter tabs */}
      <div className="flex gap-1.5 mb-5 flex-wrap">
        {STATUSES.map((s) => (
          <button
            key={s.label}
            onClick={() => { setActiveStatus(s.value); setPage(0) }}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors border ${
              activeStatus === s.value
                ? 'bg-blue-600 text-white border-blue-600'
                : 'bg-white text-gray-600 border-gray-200 hover:bg-gray-50'
            }`}
          >
            {s.label}
          </button>
        ))}
      </div>

      {isLoading && <PageLoader />}

      {!isLoading && orders.length === 0 && (
        <EmptyState
          message="No orders found"
          action={
            <button onClick={() => navigate('/orders/new')} className="btn-primary btn-sm">
              <Plus size={14} /> Create Order
            </button>
          }
        />
      )}

      {orders.length > 0 && (
        <div className="card table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Order No.</th>
                <th>Patient</th>
                <th>Phone</th>
                <th>Tests</th>
                <th>Amount</th>
                <th>Status</th>
                <th>Date</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {orders.map((o) => (
                <tr key={o.id} className="cursor-pointer" onClick={() => navigate(`/orders/${o.id}`)}>
                  <td className="font-mono text-xs text-blue-600 font-medium">{o.orderNumber}</td>
                  <td className="font-medium">{o.patient?.name ?? '—'}</td>
                  <td className="text-gray-500 text-xs">{o.patient?.phone ?? '—'}</td>
                  <td className="text-gray-500">{o.items?.length ?? 0} test(s)</td>
                  <td className="font-medium">₹{o.netAmount?.toFixed(0) ?? '—'}</td>
                  <td><StatusBadge status={o.status} /></td>
                  <td className="text-gray-400 text-xs whitespace-nowrap">
                    {o.createdAt ? format(new Date(o.createdAt), 'dd MMM, HH:mm') : '—'}
                  </td>
                  <td>
                    <button className="btn-secondary btn-sm" onClick={(e) => { e.stopPropagation(); navigate(`/orders/${o.id}`) }}>
                      View
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-2 mt-4">
          <button disabled={page === 0} onClick={() => setPage(p => p - 1)} className="btn-secondary btn-sm">Prev</button>
          <span className="text-sm text-gray-500">Page {page + 1} of {totalPages}</span>
          <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)} className="btn-secondary btn-sm">Next</button>
        </div>
      )}
    </div>
  )
}