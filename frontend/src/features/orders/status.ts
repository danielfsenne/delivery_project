import type { OrderStatus, PaymentMethod } from '@/core/api/types'

export const statusLabel: Record<OrderStatus, string> = {
  CREATED: 'Pedido criado',
  PAYMENT_PENDING: 'Aguardando pagamento',
  PAID: 'Pagamento aprovado',
  RESTAURANT_ACCEPTED: 'Restaurante confirmou',
  PREPARING: 'Preparando',
  READY_FOR_PICKUP: 'Pronto para retirada',
  OUT_FOR_DELIVERY: 'Saiu para entrega',
  DELIVERED: 'Entregue',
  CANCELLED: 'Cancelado',
}

export const statusColor: Record<OrderStatus, string> = {
  CREATED: 'bg-gray-100 text-gray-700',
  PAYMENT_PENDING: 'bg-amber-100 text-amber-800',
  PAID: 'bg-blue-100 text-blue-800',
  RESTAURANT_ACCEPTED: 'bg-blue-100 text-blue-800',
  PREPARING: 'bg-indigo-100 text-indigo-800',
  READY_FOR_PICKUP: 'bg-purple-100 text-purple-800',
  OUT_FOR_DELIVERY: 'bg-brand-100 text-brand-700',
  DELIVERED: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-red-100 text-red-700',
}

/** Caminho feliz exibido na linha do tempo do pedido. */
export const trackingSteps: OrderStatus[] = [
  'PAYMENT_PENDING',
  'PAID',
  'RESTAURANT_ACCEPTED',
  'PREPARING',
  'READY_FOR_PICKUP',
  'OUT_FOR_DELIVERY',
  'DELIVERED',
]

export const paymentLabel: Record<PaymentMethod, string> = {
  PIX: 'Pix',
  CREDIT_CARD: 'Cartão de crédito',
  CASH: 'Dinheiro',
}

export const customerCanCancel = (status: OrderStatus) =>
  status === 'CREATED' || status === 'PAYMENT_PENDING' || status === 'PAID'
