import client from './client'

export const patientsApi = {
  search:   (q)         => client.get(`/v1/patients/search?q=${encodeURIComponent(q)}`),
  getById:  (id)        => client.get(`/v1/patients/${id}`),
  register: (data)      => client.post('/v1/patients', data),
  update:   (id, data)  => client.put(`/v1/patients/${id}`, data),
  getOrders:(id)        => client.get(`/v1/orders?patientId=${id}`),
}