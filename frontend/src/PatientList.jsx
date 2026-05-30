import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Search, UserPlus, Phone, Calendar } from 'lucide-react'
import { patientsApi } from './patients.api'
import { SectionHead, EmptyState, PageLoader } from '../ui'
import { format } from 'date-fns'

export default function PatientList() {
  const navigate = useNavigate()
  const [query, setQuery] = useState('')

  const { data: patients = [], isLoading } = useQuery({
    queryKey: ['patients', 'search', query],
    queryFn: () => patientsApi.search(query),
    enabled: query.trim().length >= 2,   // only search if 2+ chars
  })

  return (
    <div>
      <SectionHead
        title="Patients"
        action={
          <button onClick={() => navigate('/patients/new')} className="btn-primary">
            <UserPlus size={16} /> Register Patient
          </button>
        }
      />

      {/* Search bar */}
      <div className="card p-4 mb-6">
        <div className="relative max-w-md">
          <Search size={16} className="absolute left-3 top-2.5 text-gray-400" />
          <input
            type="text"
            className="input pl-9"
            placeholder="Search by name or phone number…"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            autoFocus
          />
        </div>
        {query.length > 0 && query.length < 2 && (
          <p className="text-xs text-gray-400 mt-2 pl-1">Type at least 2 characters to search</p>
        )}
      </div>

      {/* Results */}
      {isLoading && query.length >= 2 && <PageLoader />}

      {!isLoading && query.length >= 2 && patients.length === 0 && (
        <EmptyState
          message={`No patients found for "${query}"`}
          action={
            <button onClick={() => navigate('/patients/new')} className="btn-primary btn-sm">
              <UserPlus size={14} /> Register New Patient
            </button>
          }
        />
      )}

      {patients.length > 0 && (
        <div className="card table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Phone</th>
                <th>Age / Gender</th>
                <th>Registered</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {patients.map((p) => (
                <tr key={p.id}>
                  <td>
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 bg-blue-100 text-blue-700 rounded-full flex items-center justify-center font-semibold text-sm">
                        {p.name?.charAt(0).toUpperCase()}
                      </div>
                      <span className="font-medium">{p.name}</span>
                    </div>
                  </td>
                  <td>
                    <div className="flex items-center gap-1.5 text-gray-600">
                      <Phone size={13} /> {p.phone}
                    </div>
                  </td>
                  <td className="text-gray-600">
                    {p.age ? `${p.age} yrs` : '—'} {p.gender ? `/ ${p.gender}` : ''}
                  </td>
                  <td className="text-gray-400 text-xs">
                    {p.createdAt ? format(new Date(p.createdAt), 'dd MMM yyyy') : '—'}
                  </td>
                  <td>
                    <button
                      onClick={() => navigate('/orders/new', { state: { patient: p } })}
                      className="btn-secondary btn-sm"
                    >
                      New Order
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {query.length === 0 && (
        <div className="text-center py-16">
          <Search size={40} className="text-gray-200 mx-auto mb-3" />
          <p className="text-gray-400 text-sm">Start typing to search for a patient</p>
        </div>
      )}
    </div>
  )
}