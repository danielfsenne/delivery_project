const currency = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const dateTime = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
const time = new Intl.DateTimeFormat('pt-BR', { timeStyle: 'short' })

export function formatCurrency(value: number): string {
  return currency.format(value)
}

export function formatDateTime(iso: string): string {
  return dateTime.format(new Date(iso))
}

export function formatTime(iso: string): string {
  return time.format(new Date(iso))
}

export function cn(...classes: Array<string | false | null | undefined>): string {
  return classes.filter(Boolean).join(' ')
}
