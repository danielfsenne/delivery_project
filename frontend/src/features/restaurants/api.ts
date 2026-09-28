import { useQuery } from '@tanstack/react-query'
import { api } from '@/core/api/client'
import type { Page, RestaurantDetail, RestaurantSummary } from '@/core/api/types'

export interface RestaurantFilters {
  q?: string
  city?: string
}

export function useRestaurants(filters: RestaurantFilters) {
  return useQuery({
    queryKey: ['restaurants', filters],
    queryFn: async () => {
      const { data } = await api.get<Page<RestaurantSummary>>('/restaurants', {
        params: { q: filters.q || undefined, city: filters.city || undefined, size: 50 },
      })
      return data
    },
    placeholderData: (previous) => previous,
  })
}

export function useRestaurant(id: number) {
  return useQuery({
    queryKey: ['restaurant', id],
    queryFn: async () => (await api.get<RestaurantDetail>(`/restaurants/${id}`)).data,
    enabled: Number.isFinite(id),
  })
}
