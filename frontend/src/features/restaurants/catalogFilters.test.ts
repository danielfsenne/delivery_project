import type { RestaurantSummary } from '@/core/api/types'
import { applyCatalogFilters, type CatalogFilters } from './catalogFilters'

function restaurant(id: number, overrides: Partial<RestaurantSummary> = {}): RestaurantSummary {
  return {
    id,
    name: `Restaurante ${id}`,
    description: null,
    cuisine: 'Pizza',
    imageUrl: null,
    deliveryFee: 5,
    minOrderValue: 0,
    deliveryTimeMin: 30,
    deliveryTimeMax: 45,
    city: 'Franca',
    district: 'Centro',
    active: true,
    open: true,
    ratingAverage: null,
    ratingCount: 0,
    ...overrides,
  }
}

const none: CatalogFilters = { freeDelivery: false, fast: false, sort: 'relevance' }
const ids = (list: RestaurantSummary[]) => list.map((r) => r.id)

describe('applyCatalogFilters', () => {
  it('mantém a ordem da API e separa os fechados', () => {
    const result = applyCatalogFilters([restaurant(1), restaurant(2, { open: false }), restaurant(3)], none)

    expect(ids(result.open)).toEqual([1, 3])
    expect(ids(result.closed)).toEqual([2])
  })

  it('filtra entrega grátis e entrega rápida', () => {
    const list = [
      restaurant(1, { deliveryFee: 0, deliveryTimeMax: 60 }),
      restaurant(2, { deliveryFee: 0, deliveryTimeMax: 40 }),
      restaurant(3, { deliveryFee: 4, deliveryTimeMax: 30 }),
    ]

    expect(ids(applyCatalogFilters(list, { ...none, freeDelivery: true }).open)).toEqual([1, 2])
    expect(ids(applyCatalogFilters(list, { ...none, fast: true }).open)).toEqual([2, 3])
    expect(ids(applyCatalogFilters(list, { ...none, freeDelivery: true, fast: true }).open)).toEqual([2])
  })

  it('ordena por avaliação deixando quem não tem nota no fim', () => {
    const list = [
      restaurant(1),
      restaurant(2, { ratingAverage: 4.2, ratingCount: 10 }),
      restaurant(3, { ratingAverage: 4.8, ratingCount: 3 }),
      restaurant(4, { ratingAverage: 4.8, ratingCount: 50 }),
    ]

    expect(ids(applyCatalogFilters(list, { ...none, sort: 'rating' }).open)).toEqual([4, 3, 2, 1])
  })

  it('ordena por tempo e por taxa sem alterar a lista original', () => {
    const list = [
      restaurant(1, { deliveryTimeMax: 50, deliveryFee: 0 }),
      restaurant(2, { deliveryTimeMax: 30, deliveryFee: 8 }),
      restaurant(3, { deliveryTimeMax: 40, deliveryFee: 3 }),
    ]

    expect(ids(applyCatalogFilters(list, { ...none, sort: 'time' }).open)).toEqual([2, 3, 1])
    expect(ids(applyCatalogFilters(list, { ...none, sort: 'fee' }).open)).toEqual([1, 3, 2])
    expect(ids(list)).toEqual([1, 2, 3])
  })
})
