import http from './http'

export function listProspects(page = 1, config = {}) {
  return http.get('/prospects', { params: { page, size: 20 }, ...config })
}

export function createProspect(payload) {
  return http.post('/prospects', payload, { timeout: 30000 })
}

export function listTemplates() {
  return http.get('/visit-templates')
}

export function saveTemplate(id, payload) {
  if (id) return http.put(`/visit-templates/${id}`, payload)
  return http.post('/visit-templates', payload)
}

export function deleteTemplate(id) {
  return http.delete(`/visit-templates/${id}`)
}
