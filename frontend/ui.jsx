import clsx from 'clsx'
import { Loader2, Inbox } from 'lucide-react'

// ---- Status Badge -------------------------------------------------------
// Renders a colored pill based on order/payment status string

const STATUS_STYLES = {
  REGISTERED:       'bg-gray-100 text-gray-700',
  SAMPLE_COLLECTED: 'bg-yellow-100 text-yellow-800',
  PROCESSING:       'bg-blue-100 text-blue-700',
  REPORT_READY:     'bg-purple-100 text-purple-700',
  DELIVERED:        'bg-green-100 text-green-700',
  // payment
  PAID:             'bg-green-100 text-green-700',
  PENDING:          'bg-yellow-100 text-yellow-800',
  REFUNDED:         'bg-red-100 text-red-700',
  // result flags
  NORMAL:           'bg-green-100 text-green-700',
  LOW:              'bg-blue-100 text-blue-700',
  HIGH:             'bg-red-100 text-red-700',
  CRITICAL:         'bg-red-200 text-red-900 font-bold',
}

export function StatusBadge({ status, className }) {
  return (
    <span className={clsx(
      'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
      STATUS_STYLES[status] ?? 'bg-gray-100 text-gray-600',
      className
    )}>
      {status?.replace(/_/g, ' ')}
    </span>
  )
}

// ---- Spinner ------------------------------------------------------------

export function Spinner({ className }) {
  return (
    <Loader2
      size={20}
      className={clsx('animate-spin text-blue-600', className)}
    />
  )
}

// ---- Full-page loading --------------------------------------------------

export function PageLoader() {
  return (
    <div className="flex items-center justify-center h-64">
      <Spinner className="w-8 h-8" />
    </div>
  )
}

// ---- Empty state --------------------------------------------------------

export function EmptyState({ message = 'No data found', action }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <Inbox size={40} className="text-gray-300 mb-3" />
      <p className="text-sm text-gray-500 mb-4">{message}</p>
      {action}
    </div>
  )
}

// ---- Error box ----------------------------------------------------------

export function ErrorBox({ message }) {
  return (
    <div className="rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-sm text-red-700">
      {message}
    </div>
  )
}

// ---- Stat card ----------------------------------------------------------

export function StatCard({ label, value, icon: Icon, color = 'blue', sub }) {
  const colors = {
    blue:   'bg-blue-50 text-blue-600',
    green:  'bg-green-50 text-green-600',
    purple: 'bg-purple-50 text-purple-600',
    amber:  'bg-amber-50 text-amber-600',
  }
  return (
    <div className="card p-5">
      <div className="flex items-center justify-between mb-3">
        <span className="text-sm text-gray-500">{label}</span>
        {Icon && (
          <div className={clsx('w-9 h-9 rounded-lg flex items-center justify-center', colors[color])}>
            <Icon size={18} />
          </div>
        )}
      </div>
      <p className="text-2xl font-bold text-gray-900">{value ?? '—'}</p>
      {sub && <p className="text-xs text-gray-400 mt-1">{sub}</p>}
    </div>
  )
}

// ---- Section heading ----------------------------------------------------

export function SectionHead({ title, action }) {
  return (
    <div className="page-header">
      <h1 className="page-title">{title}</h1>
      {action}
    </div>
  )
}