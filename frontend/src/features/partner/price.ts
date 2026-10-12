/** Lê um preço digitado com vírgula ou ponto ("29,90"); devolve null se não for um valor válido. */
export function parsePrice(value: string): number | null {
  const text = value.trim()
  if (text === '') return null
  const n = Number(text.replace(',', '.'))
  return Number.isFinite(n) && n >= 0 ? Math.round(n * 100) / 100 : null
}
