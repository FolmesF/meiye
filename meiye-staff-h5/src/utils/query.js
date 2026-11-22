export function isoDate(date) {
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
}

export function monthRange() {
  const end = new Date()
  const start = new Date(end.getFullYear(), end.getMonth(), 1)
  return { startDate: isoDate(start), endDate: isoDate(end) }
}

export function recentRange(days = 7) {
  const end = new Date()
  const start = new Date(end)
  start.setDate(end.getDate() - (days - 1))
  return { startDate: isoDate(start), endDate: isoDate(end) }
}
