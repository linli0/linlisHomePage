export function formatDate(dateStr?: string | null): string {
  if (!dateStr) return '--'
  const d = new Date(dateStr)
  if (Number.isNaN(d.getTime())) return '--'
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

export function formatPrice(price: number | null | undefined): string {
  if (price == null || Number.isNaN(price)) return '--'
  return price.toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}
