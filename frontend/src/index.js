import client from './client'

export const catalogApi = {
  getAll:     ()           => client.get('/v1/tests'),
  create:     (data)       => client.post('/v1/tests', data),
  update:     (id, data)   => client.put(`/v1/tests/${id}`, data),
  deactivate: (id)         => client.delete(`/v1/tests/${id}`),
}

export const resultsApi = {
  save:        (orderId, data) => client.post(`/v1/orders/${orderId}/results`, data),
  getByItem:   (orderId, itemId) => client.get(`/v1/orders/${orderId}/results/item/${itemId}`),
}

export const reportsApi = {
  generate:    (orderId)        => client.post(`/v1/orders/${orderId}/report/generate`),
  deliver:     (orderId, data)  => client.post(`/v1/orders/${orderId}/report/deliver`, data),
  listByOrder: (orderId)        => client.get(`/v1/orders/${orderId}/reports`),
  publicUrl:   (token)          => `/api/v1/reports/public/${token}`,
}

export const paymentsApi = {
  record:      (data)    => client.post('/v1/payments', data),
  getByOrder:  (orderId) => client.get(`/v1/payments/order/${orderId}`),
  getPending:  ()        => client.get('/v1/payments/pending'),
}

export const labApi = {
  getMyLab:     ()       => client.get('/v1/lab'),
  getStaff:     ()       => client.get('/v1/lab/staff'),
  addStaff:     (data)   => client.post('/v1/lab/staff', data),
  toggleStaff:  (id)     => client.patch(`/v1/lab/staff/${id}/toggle`),
}