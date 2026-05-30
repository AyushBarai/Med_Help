import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { Plus, Edit2, Trash2, X, Save } from 'lucide-react'
import { catalogApi } from './index'
import { SectionHead, PageLoader, EmptyState, Spinner } from '../ui'

const EMPTY_FORM = { name: '', code: '', category: '', price: '', turnaroundHours: '24', sampleType: '', referenceRanges: '' }

export default function TestCatalog() {
  const qc = useQueryClient()
  const [showForm, setShowForm] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [form, setForm] = useState(EMPTY_FORM)

  const { data: tests = [], isLoading } = useQuery({
    queryKey: ['catalog'],
    queryFn: catalogApi.getAll,
  })

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const openNew   = () => { setForm(EMPTY_FORM); setEditingId(null); setShowForm(true) }
  const openEdit  = (t) => {
    setForm({ name: t.name, code: t.code||'', category: t.category||'', price: t.price||'',
              turnaroundHours: t.turnaroundHours||'24', sampleType: t.sampleType||'',
              referenceRanges: t.referenceRanges||'' })
    setEditingId(t.id)
    setShowForm(true)
  }

  const { mutate: save, isPending: saving } = useMutation({
    mutationFn: () => {
      const data = { ...form, price: parseFloat(form.price), turnaroundHours: parseInt(form.turnaroundHours) }
      return editingId ? catalogApi.update(editingId, data) : catalogApi.create(data)
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['catalog'] })
      toast.success(editingId ? 'Test updated!' : 'Test added!')
      setShowForm(false)
    },
    onError: (err) => toast.error(err.message)
  })

  const { mutate: deactivate } = useMutation({
    mutationFn: catalogApi.deactivate,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['catalog'] }); toast.success('Test removed') },
    onError: (err) => toast.error(err.message)
  })

  const grouped = tests.reduce((acc, t) => {
    const cat = t.category || 'General'; if (!acc[cat]) acc[cat] = []; acc[cat].push(t); return acc
  }, {})

  if (isLoading) return <PageLoader />

  return (
    <div>
      <SectionHead title="Test Catalog" action={
        <button onClick={openNew} className="btn-primary"><Plus size={16} /> Add Test</button>
      } />

      {/* Add/Edit form */}
      {showForm && (
        <div className="card p-5 mb-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-medium text-gray-800">{editingId ? 'Edit Test' : 'Add New Test'}</h3>
            <button onClick={() => setShowForm(false)}><X size={18} className="text-gray-400" /></button>
          </div>
          <div className="grid grid-cols-3 gap-3">
            <div className="col-span-2"><label className="label">Test Name *</label>
              <input className="input" placeholder="Complete Blood Count" value={form.name} onChange={set('name')} required /></div>
            <div><label className="label">Code</label>
              <input className="input" placeholder="CBC" value={form.code} onChange={set('code')} /></div>
            <div><label className="label">Category</label>
              <input className="input" placeholder="Hematology" value={form.category} onChange={set('category')} /></div>
            <div><label className="label">Price (₹) *</label>
              <input className="input" type="number" placeholder="350" value={form.price} onChange={set('price')} required /></div>
            <div><label className="label">TAT (hours) *</label>
              <input className="input" type="number" placeholder="24" value={form.turnaroundHours} onChange={set('turnaroundHours')} required /></div>
            <div className="col-span-2"><label className="label">Sample Type</label>
              <input className="input" placeholder="Blood / Urine / Stool" value={form.sampleType} onChange={set('sampleType')} /></div>
            <div className="col-span-3"><label className="label">Reference Ranges (JSON)</label>
              <textarea className="input font-mono text-xs resize-none" rows={2}
                placeholder='{"Hemoglobin": {"min": 12, "max": 17, "unit": "g/dL"}}'
                value={form.referenceRanges} onChange={set('referenceRanges')} /></div>
          </div>
          <div className="flex gap-2 mt-4">
            <button onClick={() => save()} disabled={saving} className="btn-primary">
              {saving ? <Spinner className="w-4 h-4" /> : <Save size={15} />}
              {editingId ? 'Update' : 'Add Test'}
            </button>
            <button onClick={() => setShowForm(false)} className="btn-secondary">Cancel</button>
          </div>
        </div>
      )}

      {tests.length === 0 && <EmptyState message="No tests yet. Add your first test." action={<button onClick={openNew} className="btn-primary btn-sm"><Plus size={14} /> Add Test</button>} />}

      {Object.entries(grouped).map(([cat, catTests]) => (
        <div key={cat} className="mb-5">
          <h3 className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-2 pl-1">{cat}</h3>
          <div className="card table-wrap">
            <table className="table">
              <thead><tr><th>Test Name</th><th>Code</th><th>Sample</th><th>Price</th><th>TAT</th><th></th></tr></thead>
              <tbody>
                {catTests.map(t => (
                  <tr key={t.id}>
                    <td className="font-medium">{t.name}</td>
                    <td className="font-mono text-xs text-gray-400">{t.code || '—'}</td>
                    <td className="text-gray-500 text-sm">{t.sampleType || '—'}</td>
                    <td className="font-semibold">₹{t.price?.toFixed(0)}</td>
                    <td className="text-gray-500 text-sm">{t.turnaroundHours}h</td>
                    <td>
                      <div className="flex gap-1">
                        <button onClick={() => openEdit(t)} className="btn-secondary btn-sm"><Edit2 size={12} /></button>
                        <button onClick={() => { if (confirm('Remove this test?')) deactivate(t.id) }} className="btn-secondary btn-sm text-red-500"><Trash2 size={12} /></button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ))}
    </div>
  )
}