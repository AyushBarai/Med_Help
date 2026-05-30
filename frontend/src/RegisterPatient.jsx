import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { UserPlus, ArrowLeft } from 'lucide-react'
import { patientsApi } from './patients.api'
import { SectionHead, Spinner } from '../ui'

const INITIAL = { name: '', phone: '', email: '', dob: '', gender: '', address: '' }

export default function RegisterPatient() {
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [form, setForm] = useState(INITIAL)

  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value })

  const { mutate, isPending, error } = useMutation({
    mutationFn: () => patientsApi.register({
      ...form,
      dob:    form.dob    || null,
      gender: form.gender || null,
    }),
    onSuccess: (patient) => {
      qc.invalidateQueries({ queryKey: ['patients'] })
      toast.success(`${patient.name} registered successfully!`)
      navigate('/orders/new', { state: { patient } })  // go straight to create order
    },
    onError: (err) => toast.error(err.message)
  })

  const handleSubmit = (e) => { e.preventDefault(); mutate() }

  return (
    <div>
      <SectionHead
        title="Register Patient"
        action={
          <button onClick={() => navigate(-1)} className="btn-secondary">
            <ArrowLeft size={15} /> Back
          </button>
        }
      />

      <div className="card max-w-2xl p-6">
        {error && (
          <div className="mb-4 rounded-lg bg-red-50 border border-red-200 px-3 py-2 text-sm text-red-700">
            {error.message}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2">
              <label className="label">Full Name *</label>
              <input className="input" placeholder="Patient full name" value={form.name} onChange={set('name')} required />
            </div>

            <div>
              <label className="label">Phone Number *</label>
              <input className="input" placeholder="10-digit mobile" value={form.phone} onChange={set('phone')}
                pattern="[0-9]{10}" maxLength={10} required />
            </div>

            <div>
              <label className="label">Email</label>
              <input className="input" type="email" placeholder="Optional" value={form.email} onChange={set('email')} />
            </div>

            <div>
              <label className="label">Date of Birth</label>
              <input className="input" type="date" value={form.dob} onChange={set('dob')}
                max={new Date().toISOString().split('T')[0]} />
            </div>

            <div>
              <label className="label">Gender</label>
              <select className="input" value={form.gender} onChange={set('gender')}>
                <option value="">Select gender</option>
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="OTHER">Other</option>
              </select>
            </div>

            <div className="col-span-2">
              <label className="label">Address</label>
              <textarea className="input resize-none" rows={2} placeholder="Optional address"
                value={form.address} onChange={set('address')} />
            </div>
          </div>

          <div className="flex gap-3 pt-2">
            <button type="submit" disabled={isPending} className="btn-primary">
              {isPending ? <><Spinner className="w-4 h-4" /> Registering…</> : <><UserPlus size={15} /> Register Patient</>}
            </button>
            <button type="button" onClick={() => navigate(-1)} className="btn-secondary">Cancel</button>
          </div>
        </form>
      </div>
    </div>
  )
}