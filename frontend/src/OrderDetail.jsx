import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { ArrowLeft, ChevronRight, FileText, Send, CreditCard, ClipboardEdit } from 'lucide-react'
import { ordersApi } from './orders.api'
import { reportsApi } from './index'
import { paymentsApi } from './index'
import { StatusBadge, PageLoader, ErrorBox, Spinner } from '../ui'
import { format } from 'date-fns'

// Status transition map — what button to show and what status it transitions to
const NEXT_STATUS = {
  REGISTERED:       { label: 'Mark Sample Collected', next: 'SAMPLE_COLLECTED', color: 'bg-yellow-500' },
  SAMPLE_COLLECTED: { label: 'Mark Processing',       next: 'PROCESSING',       color: 'bg-blue-500' },
  PROCESSING:       { label: 'Mark Report Ready',     next: 'REPORT_READY',     color: 'bg-purple-500' },
}

export default function OrderDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const qc = useQueryClient()

  const { data: order, isLoading, error } = useQuery({
    queryKey: ['order', id],
    queryFn: () => ordersApi.getById(id),
  })

  const { data: reports = [] } = useQuery({
    queryKey: ['reports', id],
    queryFn: () => reportsApi.listByOrder(id),
    enabled: !!id,
  })

  const { data: payments = [] } = useQuery({
    queryKey: ['payments', id],
    queryFn: () => paymentsApi.getByOrder(id),
    enabled: !!id,
  })

  const { mutate: updateStatus, isPending: updatingStatus } = useMutation({
    mutationFn: (status) => ordersApi.updateStatus(id, status),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['order', id] }); toast.success('Status updated!') },
    onError: (err) => toast.error(err.message),
  })

  const { mutate: generateReport, isPending: generatingReport } = useMutation({
    mutationFn: () => reportsApi.generate(id),
    onSuccess: (r) => { qc.invalidateQueries({ queryKey: ['reports', id] }); toast.success('Report generated!') },
    onError: (err) => toast.error(err.message),
  })

  const { mutate: deliverReport, isPending: delivering } = useMutation({
    mutationFn: () => reportsApi.deliver(id, { sendWhatsApp: true, sendSms: false, sendEmail: false }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['order', id] }); toast.success('Report delivered via WhatsApp!') },
    onError: (err) => toast.error(err.message),
  })

  if (isLoading) return <PageLoader />
  if (error)     return <ErrorBox message={error.message} />
  if (!order)    return null

  const nextStep = NEXT_STATUS[order.status]
  const latestReport = reports[0]
  const totalPaid = payments.reduce((s, p) => s + (p.status === 'PAID' ? p.amount : 0), 0)
  const balanceDue = (order.netAmount ?? 0) - totalPaid

  return (
    <div>
      {/* Header */}
      <div className="flex items-center gap-3 mb-6">
        <button onClick={() => navigate('/orders')} className="btn-secondary btn-sm">
          <ArrowLeft size={14} />
        </button>
        <div>
          <h1 className="text-lg font-semibold text-gray-900">{order.orderNumber}</h1>
          <p className="text-sm text-gray-400">
            {order.createdAt ? format(new Date(order.createdAt), 'dd MMM yyyy, HH:mm') : ''}
          </p>
        </div>
        <StatusBadge status={order.status} className="ml-auto" />
      </div>

      <div className="grid grid-cols-3 gap-5">

        {/* LEFT col */}
        <div className="col-span-2 space-y-5">

          {/* Status flow + action button */}
          <div className="card p-5">
            <h3 className="font-medium text-gray-800 mb-4">Order Progress</h3>

            {/* Step indicators */}
            <div className="flex items-center gap-1 mb-5 overflow-x-auto">
              {['REGISTERED','SAMPLE_COLLECTED','PROCESSING','REPORT_READY','DELIVERED'].map((s, i, arr) => {
                const statuses = ['REGISTERED','SAMPLE_COLLECTED','PROCESSING','REPORT_READY','DELIVERED']
                const currentIdx = statuses.indexOf(order.status)
                const stepIdx = statuses.indexOf(s)
                const done = stepIdx <= currentIdx
                return (
                  <div key={s} className="flex items-center gap-1 flex-shrink-0">
                    <div className={`flex flex-col items-center ${done ? 'text-blue-600' : 'text-gray-300'}`}>
                      <div className={`w-6 h-6 rounded-full border-2 flex items-center justify-center text-xs font-bold
                        ${done ? 'border-blue-600 bg-blue-600 text-white' : 'border-gray-300'}`}>
                        {stepIdx < currentIdx ? '✓' : stepIdx + 1}
                      </div>
                      <span className="text-xs mt-1 whitespace-nowrap">{s.replace(/_/g,' ')}</span>
                    </div>
                    {i < arr.length - 1 && (
                      <div className={`h-0.5 w-8 mb-4 ${stepIdx < currentIdx ? 'bg-blue-600' : 'bg-gray-200'}`} />
                    )}
                  </div>
                )
              })}
            </div>

            {/* Action buttons for this status */}
            <div className="flex flex-wrap gap-2">
              {nextStep && (
                <button onClick={() => updateStatus(nextStep.next)} disabled={updatingStatus}
                  className={`btn text-white ${nextStep.color} hover:opacity-90`}>
                  {updatingStatus ? <Spinner className="w-4 h-4" /> : <ChevronRight size={16} />}
                  {nextStep.label}
                </button>
              )}

              {(order.status === 'PROCESSING' || order.status === 'REPORT_READY') && (
                <button onClick={() => navigate(`/orders/${id}/results`)} className="btn-secondary">
                  <ClipboardEdit size={16} /> Enter Results
                </button>
              )}

              {order.status === 'REPORT_READY' && (
                <button onClick={() => generateReport()} disabled={generatingReport} className="btn-primary">
                  {generatingReport ? <Spinner className="w-4 h-4" /> : <FileText size={16} />}
                  Generate Report
                </button>
              )}

              {latestReport && order.status !== 'DELIVERED' && (
                <button onClick={() => deliverReport()} disabled={delivering} className="btn bg-green-600 text-white hover:bg-green-700">
                  {delivering ? <Spinner className="w-4 h-4" /> : <Send size={16} />}
                  Send to Patient
                </button>
              )}
            </div>
          </div>

          {/* Tests ordered */}
          <div className="card">
            <div className="px-5 py-3 border-b border-gray-100 font-medium text-sm text-gray-700">Tests Ordered</div>
            <div className="divide-y divide-gray-100">
              {order.items?.map((item) => (
                <div key={item.id} className="flex items-center justify-between px-5 py-3">
                  <div>
                    <p className="font-medium text-sm">{item.testName}</p>
                    <p className="text-xs text-gray-400">Item ID: {item.id?.slice(0,8)}…</p>
                  </div>
                  <div className="flex items-center gap-4">
                    <span className="font-medium text-sm">₹{item.price?.toFixed(0)}</span>
                    <StatusBadge status={item.status} />
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Reports */}
          {reports.length > 0 && (
            <div className="card">
              <div className="px-5 py-3 border-b border-gray-100 font-medium text-sm text-gray-700">Reports</div>
              {reports.map((r) => (
                <div key={r.id} className="flex items-center justify-between px-5 py-3">
                  <div>
                    <p className="font-medium text-sm">Report v{r.version}</p>
                    <p className="text-xs text-gray-400">
                      {r.generatedAt ? format(new Date(r.generatedAt), 'dd MMM, HH:mm') : ''}
                    </p>
                  </div>
                  <a href={reportsApi.publicUrl(r.accessToken)} target="_blank" rel="noreferrer"
                    className="btn-secondary btn-sm" onClick={(e) => e.stopPropagation()}>
                    <FileText size={13} /> View PDF
                  </a>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* RIGHT col */}
        <div className="col-span-1 space-y-4">

          {/* Patient info */}
          <div className="card p-4">
            <h3 className="font-medium text-sm text-gray-700 mb-3">Patient</h3>
            <p className="font-semibold">{order.patient?.name}</p>
            <p className="text-sm text-gray-500">{order.patient?.phone}</p>
            {order.patient?.age && (
              <p className="text-xs text-gray-400 mt-1">{order.patient.age} yrs · {order.patient.gender}</p>
            )}
            {order.referredBy && (
              <p className="text-xs text-gray-500 mt-2">Ref: Dr. {order.referredBy}</p>
            )}
          </div>

          {/* Bill */}
          <div className="card p-4">
            <h3 className="font-medium text-sm text-gray-700 mb-3">Bill</h3>
            <div className="space-y-1.5 text-sm">
              <div className="flex justify-between"><span className="text-gray-500">Subtotal</span><span>₹{order.totalAmount?.toFixed(0)}</span></div>
              {order.discountAmount > 0 && (
                <div className="flex justify-between text-green-600"><span>Discount</span><span>- ₹{order.discountAmount?.toFixed(0)}</span></div>
              )}
              <div className="flex justify-between font-bold border-t border-gray-100 pt-1.5"><span>Net</span><span>₹{order.netAmount?.toFixed(0)}</span></div>
              <div className="flex justify-between text-green-600"><span>Paid</span><span>₹{totalPaid.toFixed(0)}</span></div>
              {balanceDue > 0 && (
                <div className="flex justify-between text-red-600 font-medium"><span>Balance Due</span><span>₹{balanceDue.toFixed(0)}</span></div>
              )}
            </div>
            <button onClick={() => navigate('/payments', { state: { orderId: id, order } })}
              className="btn-secondary btn-sm w-full justify-center mt-3">
              <CreditCard size={13} /> Record Payment
            </button>
          </div>

          {/* Notes */}
          {order.notes && (
            <div className="card p-4">
              <h3 className="font-medium text-sm text-gray-700 mb-2">Notes</h3>
              <p className="text-sm text-gray-600">{order.notes}</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}