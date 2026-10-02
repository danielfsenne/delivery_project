import type { QueryClient } from '@tanstack/react-query'
import type { OrderStatus, Tracking } from '@/core/api/types'

/** Mensagens enviadas pelo notification-service (ver RealtimeMessage no backend). */
export interface OrderUpdate {
  orderId: number
  restaurantId: number
  restaurantName: string
  previousStatus: OrderStatus | null
  status: OrderStatus
  at: string
}

export interface DeliveryUpdate {
  deliveryId: number
  orderId: number
  status: string
  at: string
}

export interface LocationUpdate {
  orderId: number
  latitude: number
  longitude: number
  at: string
}

export type RealtimeMessage =
  | { type: 'order.status'; payload: OrderUpdate }
  | { type: 'restaurant.order'; payload: OrderUpdate }
  | { type: 'deliveries.changed'; payload: DeliveryUpdate }
  | { type: 'driver.location'; payload: LocationUpdate }

/**
 * Aplica uma mensagem ao cache do TanStack Query. O WebSocket só avisa o que mudou;
 * os dados completos vêm da API (que aplica as regras de acesso), exceto a posição do
 * entregador, gravada direto no cache do acompanhamento para o mapa andar sem requisições.
 */
export function applyRealtimeMessage(queryClient: QueryClient, message: RealtimeMessage) {
  switch (message.type) {
    case 'order.status': {
      const { orderId } = message.payload
      queryClient.invalidateQueries({ queryKey: ['order', orderId] })
      queryClient.invalidateQueries({ queryKey: ['orders'], exact: true })
      queryClient.invalidateQueries({ queryKey: ['tracking', orderId] })
      break
    }
    case 'restaurant.order':
      queryClient.invalidateQueries({ queryKey: ['partner', message.payload.restaurantId] })
      break
    case 'deliveries.changed':
      queryClient.invalidateQueries({ queryKey: ['driver'] })
      break
    case 'driver.location': {
      const { orderId, latitude, longitude, at } = message.payload
      queryClient.setQueryData<Tracking>(['tracking', orderId], (current) =>
        current ? { ...current, driverLocation: { latitude, longitude }, driverLocationUpdatedAt: at } : current,
      )
      break
    }
  }
}
