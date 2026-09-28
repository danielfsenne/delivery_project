import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/core/api/client'
import type { Address, Order, OrderSummary, Page, PaymentMethod } from '@/core/api/types'
import { cartKey } from '@/features/cart/api'

export interface CheckoutInput {
  deliveryAddress: Address
  paymentMethod: PaymentMethod
  notes?: string
}

export function useCheckout() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (input: CheckoutInput) => (await api.post<Order>('/orders', input)).data,
    onSuccess: (order) => {
      queryClient.invalidateQueries({ queryKey: cartKey })
      queryClient.invalidateQueries({ queryKey: ['orders'] })
      queryClient.setQueryData(['order', order.id], order)
    },
  })
}

export function useMyOrders() {
  return useQuery({
    queryKey: ['orders'],
    queryFn: async () => (await api.get<Page<OrderSummary>>('/orders', { params: { size: 50 } })).data,
  })
}

/** Atualiza a cada 10s enquanto o pedido está em andamento (WebSocket chega na Fase 3). */
export function useOrder(id: number) {
  return useQuery({
    queryKey: ['order', id],
    queryFn: async () => (await api.get<Order>(`/orders/${id}`)).data,
    refetchInterval: (query) => {
      const status = query.state.data?.status
      return status === 'DELIVERED' || status === 'CANCELLED' ? false : 10_000
    },
  })
}

export function useCancelOrder(id: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (reason?: string) => (await api.post<Order>(`/orders/${id}/cancel`, { reason })).data,
    onSuccess: (order) => {
      queryClient.setQueryData(['order', id], order)
      queryClient.invalidateQueries({ queryKey: ['orders'] })
    },
  })
}
