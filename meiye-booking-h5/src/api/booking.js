import http from './http'

export function listStores() {
  return http.get('/catalog/stores')
}

export function listServices(storeId) {
  return http.get('/catalog/services', { params: { storeId } })
}

export function listBeauticians(params, config = {}) {
  return http.get('/catalog/beauticians', { params, ...config })
}

export function beauticianDetail(id) {
  return http.get(`/catalog/beauticians/${id}`)
}

export function beauticianReviews(id) {
  return http.get(`/catalog/beauticians/${id}/reviews`)
}

export function slotBoard(params) {
  return http.get('/availability/slots', { params })
}

export function createAppointment(payload) {
  return http.post('/appointments', payload)
}

export function cancelAppointment(id, reason) {
  return http.post(`/appointments/${id}/cancel`, { reason })
}

export function createReview(payload) {
  return http.post('/reviews', payload)
}

export function myAppointments(group, page = 1, config = {}) {
  return http.get('/me/appointments', { params: { group, page, size: 20 }, ...config })
}
