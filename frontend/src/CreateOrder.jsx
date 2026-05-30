import { useState, useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { Search, X, Plus, UserPlus, CheckCircle2 } from 'lucide-react'
import { patientsApi } from './patients.api'
import { catalogApi } from './index'
import { ordersApi } from './orders.api'
import { SectionHead, Spinner, PageLoader } from '../ui'

export default function CreateOrder() {
  const navigate  = useNavigate()
  const location  = useLocation()
  const qc        = useQueryClient()

  // If navigated from PatientList with a patient already selected
  const [patient,  setPatient]  = useState(location.state?.patient ?? null)
  const [searchQ,  setSearchQ]  = useState('')
  const [selectedTests, setSelectedTests] = useState([])
  const [form, setForm] = useState({ referredBy: '', collectionType: 'WALK_IN', discountAmount: '', notes: '' })

  // Fetch all active tests for this lab
  const { data: catalog = [], isLoading: catalogLoading } = useQuery({
    queryKey: ['catalog'],
    queryFn: catalogApi.getAll,
  })

  // Search patients
  const { data: searchResults = [], isFetching: searching } = useQuery({
    queryKey: ['patients', 'search', searchQ],
    queryFn: () => patientsApi.search(searchQ),
    enabled: searchQ.trim().length >= 2 && !patient,
  })

  const total    = selectedTests.reduce((s, t) => s + t.price, 0)
  const discount = parseFloat(form.discountAmount) || 0
  const net      = Math.max(0, total - discount)

  const { mutate: createOrder, isPending } = useMutation({
    mutationFn: () => ordersApi.create({
      patientId:       patient.id,
      testIds:         selectedTests.map(t => t.id),
      referredBy:      form.referredBy  || null,
      collectionType:  form.collectionType,
      discountAmount:  discount || null,
      notes:           form.notes || null,
    }),
    onSuccess: (order) => {
      qc.invalidateQueries({ queryKey: ['orders'] })
      toast.success(`Order ${order.orderNumber} created!`)
      navigate(`/orders/${order.id}`)
    },
    onError: (err) => toast.error(err.message)
  })

  const toggleTest = (test) => {
    setSelectedTests(prev =>
      prev.find(t => t.id === test.id)
        ? prev.filter(t => t.id !== test.id)
        : [...prev, test]
    )
  }

  const groupedCatalog = catalog.reduce((acc, test) => {
    const cat = test.category || 'General'
    if (!acc[cat]) acc[cat] = []
    acc[cat].push(test)
    return acc
  }, {})

  const canSubmit = patient && selectedTests.length > 0 && !isPending

  return (
    <div>
      <SectionHead title="New Order" action={
        <button onClick={() => navigate(-1)} className="btn-secondary">Cancel</button>
      } />

      <div className="grid grid-cols-3 gap-5">

        {/* LEFT: Patient + form */}
        <div className="col-span-1 space-y-4">

          {/* Patient selector */}
          <div className="card p-4">
            <h3 className="font-medium text-sm text-gray-700 mb-3">Patient</h3>

            {patient ? (
              <div className="flex items-start justify-between bg-blue-50 rounded-lg p-3">
                <div>
                  <p className="font-semibold text-gray-900 text-sm">{patient.name}</p>
                  <p className="text-xs text-gray-500 mt-0.5">{patient.phone}</p>
                  {patient.age && <p className="text-xs text-gray-400">{patient.age} yrs · {patient.gender}</p>}
                </div>
                <button onClick={() => setPatient(null)} className="text-gray-400 hover:text-red-500">
                  <X size={15} />
                </button>
              </div>
            ) : (
              <>
                <div className="relative mb-2">
                  <Search size={14} className="absolute left-2.5 top-2.5 text-gray-400" />
                  <input className="input pl-8 text-sm" placeholder="Search patient…"
                    value={searchQ} onChange={(e) => setSearchQ(e.target.value)} />
                </div>

                {searching && <p className="text-xs text-gray-400 px-1">Searching…</p>}

                {searchResults.map(p => (
                  <button key={p.id} onClick={() => { setPatient(p); setSearchQ('') }}
                    className="w-full text-left px-3 py-2 rounded-lg hover:bg-gray-100 transition-colors">
                    <p className="font-medium text-sm text-gray-800">{p.name}</p>
                    <p className="text-xs text-gray-400">{p.phone}</p>
                  </button>
                ))}

                <button onClick={() => navigate('/patients/new')}
                  className="mt-2 text-xs text-blue-600 hover:underline flex items-center gap-1">
                  <UserPlus size={12} /> Register new patient
                </button>
              </>
            )}
          </div>

          {/* Order details form */}
          <div className="card p-4 space-y-3">
            <h3 className="font-medium text-sm text-gray-700">Order Details</h3>

            <div>
              <label className="label text-xs">Referred By</label>
              <input className="input text-sm" placeholder="Dr. Name (optional)"
                value={form.referredBy} onChange={e => setForm({...form, referredBy: e.target.value})} />
            </div>

            <div>
              <label className="label text-xs">Collection Type</label>
              <select className="input text-sm" value={form.collectionType}
                onChange={e => setForm({...form, collectionType: e.target.value})}>
                <option value="WALK_IN">Walk-in</option>
                <option value="HOME_COLLECTION">Home Collection</option>
              </select>
            </div>

            <div>
              <label className="label text-xs">Discount (₹)</label>
              <input className="input text-sm" type="number" placeholder="0" min="0"
                value={form.discountAmount} onChange={e => setForm({...form, discountAmount: e.target.value})} />
            </div>

            <div>
              <label className="label text-xs">Notes</label>
              <textarea className="input text-sm resize-none" rows={2} placeholder="Optional"
                value={form.notes} onChange={e => setForm({...form, notes: e.target.value})} />
            </div>
          </div>

          {/* Bill summary */}
          {selectedTests.length > 0 && (
            <div className="card p-4">
              <h3 className="font-medium text-sm text-gray-700 mb-3">Bill Summary</h3>
              {selectedTests.map(t => (
                <div key={t.id} className="flex justify-between text-sm py-1">
                  <span className="text-gray-600 truncate pr-2">{t.name}</span>
                  <span className="font-medium">₹{t.price.toFixed(0)}</span>
                </div>
              ))}
              <div className="border-t border-gray-100 mt-2 pt-2">
                {discount > 0 && (
                  <div className="flex justify-between text-sm text-green-600">
                    <span>Discount</span><span>- ₹{discount.toFixed(0)}</span>
                  </div>
                )}
                <div className="flex justify-between font-bold text-base mt-1">
                  <span>Total</span><span>₹{net.toFixed(0)}</span>
                </div>
              </div>

              <button onClick={createOrder} disabled={!canSubmit}
                className="btn-primary w-full justify-center mt-4">
                {isPending ? <><Spinner className="w-4 h-4" /> Creating…</> : 'Create Order'}
              </button>
            </div>
          )}
        </div>

        {/* RIGHT: Test catalog */}
        <div className="col-span-2">
          <div className="card">
            <div className="px-5 py-4 border-b border-gray-100 flex items-center justify-between">
              <h3 className="font-medium text-gray-800">Select Tests</h3>
              <span className="text-xs text-gray-400">{selectedTests.length} selected</span>
            </div>

            {catalogLoading ? <PageLoader /> : (
              <div className="p-4 space-y-4 max-h-[600px] overflow-y-auto">
                {Object.entries(groupedCatalog).map(([category, tests]) => (
                  <div key={category}>
                    <h4 className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-2">{category}</h4>
                    <div className="space-y-1.5">
                      {tests.map(test => {
                        const selected = !!selectedTests.find(t => t.id === test.id)
                        return (
                          <div key={test.id}
                            onClick={() => toggleTest(test)}
                            className={`flex items-center justify-between px-4 py-3 rounded-xl cursor-pointer border transition-all ${
                              selected
                                ? 'bg-blue-50 border-blue-300'
                                : 'bg-gray-50 border-transparent hover:bg-gray-100'
                            }`}
                          >
                            <div className="flex items-center gap-3">
                              <CheckCircle2 size={18} className={selected ? 'text-blue-600' : 'text-gray-300'} />
                              <div>
                                <p className="font-medium text-sm text-gray-900">{test.name}</p>
                                <p className="text-xs text-gray-400">
                                  {test.code} · {test.sampleType} · {test.turnaroundHours}h TAT
                                </p>
                              </div>
                            </div>
                            <span className="font-semibold text-gray-700 text-sm">₹{test.price?.toFixed(0)}</span>
                          </div>
                        )
                      })}
                    </div>
                  </div>
                ))}

                {catalog.length === 0 && (
                  <div className="text-center py-10 text-sm text-gray-400">
                    No tests in catalog. <button onClick={() => navigate('/catalog')} className="text-blue-600 hover:underline">Add tests →</button>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}