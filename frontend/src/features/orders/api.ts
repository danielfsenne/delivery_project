import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import axios from 'axios'
import { api } from '@/core/api/client'
import type { Address, Order, OrderSummary, Page, PaymentMethod, Review, Tracking } from '@/core/api/types'
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

/** Nova tentativa de pagamento, quando o serviço de pagamento estava indisponível. */
export function usePayOrder(id: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async () => (await api.post<Order>(`/orders/${id}/pay`)).data,
    onSuccess: (order) => queryClient.setQueryData(['order', id], order),
  })
}

/** Avaliação do pedido; null quando ainda não foi avaliado. */
export function useOrderReview(id: number, enabled: boolean) {
  return useQuery({
    queryKey: ['order', id, 'review'],
    enabled,
    queryFn: async () => {
      try {
        return (await api.get<Review>(`/orders/${id}/review`)).data
      } catch (error) {
        if (axios.isAxiosError(error) && error.response?.status === 404) return null
        throw error
      }
    },
  })
}

export interface ReviewInput {
  foodRating: number
  deliveryRating?: number
  comment?: string
}

export function useCreateReview(id: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (input: ReviewInput) => (await api.post<Review>(`/orders/${id}/review`, input)).data,
    onSuccess: (review) => queryClient.setQueryData(['order', id, 'review'], review),
  })
}

/** Posição do entregador enquanto o pedido está a caminho. */
export function useTracking(orderId: number, enabled: boolean) {
  return useQuery({
    queryKey: ['tracking', orderId],
    enabled,
    retry: false,
    refetchInterval: 5_000,
    queryFn: async () => (await api.get<Tracking>(`/deliveries/order/${orderId}`)).data,
  })
}
