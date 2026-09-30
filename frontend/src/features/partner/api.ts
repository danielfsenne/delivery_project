import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/core/api/client'
import type { Order, OrderStatus, Page, RestaurantDetail, RestaurantStats, RestaurantSummary } from '@/core/api/types'

/** Status que aparecem no quadro de pedidos do restaurante. */
export const boardStatuses: OrderStatus[] = [
  'PAID',
  'RESTAURANT_ACCEPTED',
  'PREPARING',
  'READY_FOR_PICKUP',
  'OUT_FOR_DELIVERY',
]

export function useMyRestaurants() {
  return useQuery({
    queryKey: ['partner', 'restaurants'],
    queryFn: async () => (await api.get<RestaurantSummary[]>('/restaurants/mine')).data,
  })
}

export function useRestaurantStats(restaurantId: number | undefined) {
  return useQuery({
    queryKey: ['partner', restaurantId, 'stats'],
    enabled: !!restaurantId,
    refetchInterval: 15_000,
    queryFn: async () => (await api.get<RestaurantStats>(`/orders/restaurant/${restaurantId}/stats`)).data,
  })
}

/** Pedidos em andamento, atualizados a cada 10s (WebSocket chega na Fase 3). */
export function useRestaurantOrders(restaurantId: number | undefined) {
  return useQuery({
    queryKey: ['partner', restaurantId, 'orders'],
    enabled: !!restaurantId,
    refetchInterval: 10_000,
    queryFn: async () => {
      const params = new URLSearchParams()
      boardStatuses.forEach((s) => params.append('status', s))
      params.set('size', '100')
      return (await api.get<Page<Order>>(`/orders/restaurant/${restaurantId}?${params}`)).data.content
    },
  })
}

export function useChangeOrderStatus(restaurantId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({ orderId, status, reason }: { orderId: number; status: OrderStatus; reason?: string }) =>
      (await api.patch<Order>(`/orders/${orderId}/status`, { status, reason })).data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['partner', restaurantId] }),
  })
}

export function useManagedRestaurant(restaurantId: number | undefined) {
  return useQuery({
    queryKey: ['partner', restaurantId, 'menu'],
    enabled: !!restaurantId,
    queryFn: async () => (await api.get<RestaurantDetail>(`/restaurants/mine/${restaurantId}`)).data,
  })
}

function useMenuMutation<TInput>(restaurantId: number, fn: (input: TInput) => Promise<RestaurantDetail>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: fn,
    onSuccess: (restaurant) => {
      queryClient.setQueryData(['partner', restaurantId, 'menu'], restaurant)
      queryClient.invalidateQueries({ queryKey: ['restaurant', restaurantId] })
    },
  })
}

export interface ProductInput {
  name: string
  description?: string
  price: number
  imageUrl?: string
}

export function useSetAvailability(restaurantId: number) {
  return useMenuMutation(restaurantId, async ({ productId, available }: { productId: number; available: boolean }) =>
    (await api.patch<RestaurantDetail>(`/restaurants/mine/${restaurantId}/products/${productId}/availability`, {
      available,
    })).data,
  )
}

export function useUpdateProduct(restaurantId: number) {
  return useMenuMutation(restaurantId, async ({ productId, ...input }: ProductInput & { productId: number }) =>
    (await api.put<RestaurantDetail>(`/restaurants/mine/${restaurantId}/products/${productId}`, input)).data,
  )
}

export function useAddProduct(restaurantId: number) {
  return useMenuMutation(restaurantId, async ({ categoryId, ...input }: ProductInput & { categoryId: number }) =>
    (await api.post<RestaurantDetail>(`/restaurants/mine/${restaurantId}/categories/${categoryId}/products`, input))
      .data,
  )
}

export function useAddCategory(restaurantId: number) {
  return useMenuMutation(restaurantId, async (name: string) =>
    (await api.post<RestaurantDetail>(`/restaurants/mine/${restaurantId}/categories`, { name })).data,
  )
}
