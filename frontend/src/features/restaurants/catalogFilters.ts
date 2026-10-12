import type { RestaurantSummary } from '@/core/api/types'

export type SortOption = 'relevance' | 'rating' | 'time' | 'fee'

export interface CatalogFilters {
  freeDelivery: boolean
  fast: boolean
  sort: SortOption
}

export const sortLabels: Record<SortOption, string> = {
  relevance: 'Ordenar',
  rating: 'Avaliação',
  time: 'Tempo de entrega',
  fee: 'Taxa de entrega',
}

/** Entrega "rápida": o limite superior da estimativa cabe em 40 minutos. */
export const FAST_DELIVERY_MAX_MINUTES = 40

const comparators: Record<Exclude<SortOption, 'relevance'>, (a: RestaurantSummary, b: RestaurantSummary) => number> = {
  // Sem avaliações vai para o fim; empate desempata pelo número de avaliações.
  rating: (a, b) => (b.ratingAverage ?? -1) - (a.ratingAverage ?? -1) || b.ratingCount - a.ratingCount,
  time: (a, b) => a.deliveryTimeMax - b.deliveryTimeMax || a.deliveryTimeMin - b.deliveryTimeMin,
  fee: (a, b) => a.deliveryFee - b.deliveryFee,
}

/**
 * Aplica os filtros rápidos da home e separa abertos de fechados,
 * como nos apps de delivery: quem não atende agora vai para o fim da página.
 */
export function applyCatalogFilters(restaurants: RestaurantSummary[], filters: CatalogFilters) {
  const filtered = restaurants.filter(
    (r) =>
      (!filters.freeDelivery || r.deliveryFee === 0) &&
      (!filters.fast || r.deliveryTimeMax <= FAST_DELIVERY_MAX_MINUTES),
  )
  const sorted = filters.sort === 'relevance' ? filtered : [...filtered].sort(comparators[filters.sort])
  return {
    open: sorted.filter((r) => r.open),
    closed: sorted.filter((r) => !r.open),
  }
}
