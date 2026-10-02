import { useEffect } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { Client } from '@stomp/stompjs'
import { freshAccessToken } from '@/core/api/client'
import { useAuthStore } from '@/core/auth/auth-store'
import { statusLabel } from '@/features/orders/status'
import { useToasts } from '@/shared/lib/toasts'
import { applyRealtimeMessage, type RealtimeMessage } from './messages'
import { useRealtimeStatus } from './status'

/** Status que merecem aviso na tela do cliente; os demais só atualizam os dados. */
const notifyCustomer = new Set(['PAID', 'RESTAURANT_ACCEPTED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED'])

function brokerUrl() {
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  return `${protocol}://${window.location.host}/ws`
}

/**
 * Uma conexão STOMP por sessão. O token vai no CONNECT e é renovado antes de cada
 * (re)conexão; a reconexão é automática se o servidor cair.
 */
export function RealtimeProvider() {
  const queryClient = useQueryClient()
  const userId = useAuthStore((s) => s.user?.id)
  const role = useAuthStore((s) => s.user?.role)
  const pushToast = useToasts((s) => s.push)

  useEffect(() => {
    if (!userId) return

    const onMessage = (body: string) => {
      const message = JSON.parse(body) as RealtimeMessage
      applyRealtimeMessage(queryClient, message)
      if (message.type === 'order.status' && notifyCustomer.has(message.payload.status)) {
        pushToast({
          title: `Pedido #${message.payload.orderId}: ${statusLabel[message.payload.status]}`,
          description: message.payload.restaurantName,
          href: `/orders/${message.payload.orderId}`,
        })
      }
      if (message.type === 'restaurant.order' && message.payload.status === 'PAID') {
        pushToast({ title: `Novo pedido #${message.payload.orderId}`, description: 'Aguardando sua confirmação' })
      }
    }

    let connectedBefore = false
    const client = new Client({
      brokerURL: brokerUrl(),
      reconnectDelay: 5_000,
      heartbeatIncoming: 10_000,
      heartbeatOutgoing: 10_000,
      beforeConnect: async () => {
        const token = await freshAccessToken()
        client.connectHeaders = token ? { Authorization: `Bearer ${token}` } : {}
      },
      onConnect: () => {
        useRealtimeStatus.setState({ connected: true })
        client.subscribe('/user/queue/events', (frame) => onMessage(frame.body))
        if (role === 'DRIVER') {
          client.subscribe('/topic/drivers/deliveries', (frame) => onMessage(frame.body))
        }
        // Reconectou: algo pode ter mudado enquanto a conexão estava fora.
        if (connectedBefore) queryClient.invalidateQueries()
        connectedBefore = true
      },
      onWebSocketClose: () => useRealtimeStatus.setState({ connected: false }),
    })
    client.activate()

    return () => {
      useRealtimeStatus.setState({ connected: false })
      void client.deactivate()
    }
  }, [userId, role, queryClient, pushToast])

  return null
}
