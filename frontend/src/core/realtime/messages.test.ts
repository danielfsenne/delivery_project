import { QueryClient } from '@tanstack/react-query'
import type { Tracking } from '@/core/api/types'
import { applyRealtimeMessage } from './messages'

describe('applyRealtimeMessage', () => {
  it('mudança de status invalida o pedido e a lista', () => {
    const client = new QueryClient()
    client.setQueryData(['order', 7], { id: 7 })
    client.setQueryData(['orders'], { content: [] })
    client.setQueryData(['order', 8], { id: 8 })

    applyRealtimeMessage(client, {
      type: 'order.status',
      payload: { orderId: 7, restaurantId: 1, restaurantName: 'X', previousStatus: 'PAID', status: 'PREPARING', at: '' },
    })

    expect(client.getQueryState(['order', 7])?.isInvalidated).toBe(true)
    expect(client.getQueryState(['orders'])?.isInvalidated).toBe(true)
    expect(client.getQueryState(['order', 8])?.isInvalidated).toBe(false)
  })

  it('posição do entregador atualiza o acompanhamento sem nova requisição', () => {
    const client = new QueryClient()
    const tracking: Tracking = {
      orderId: 7,
      status: 'PICKED_UP',
      driverLocation: { latitude: -20.5, longitude: -47.4 },
      driverLocationUpdatedAt: '2026-10-01T12:00:00Z',
      pickup: null,
      dropoff: null,
    }
    client.setQueryData(['tracking', 7], tracking)

    applyRealtimeMessage(client, {
      type: 'driver.location',
      payload: { orderId: 7, latitude: -20.53, longitude: -47.41, at: '2026-10-01T12:00:10Z' },
    })

    const updated = client.getQueryData<Tracking>(['tracking', 7])
    expect(updated?.driverLocation).toEqual({ latitude: -20.53, longitude: -47.41 })
    expect(updated?.driverLocationUpdatedAt).toBe('2026-10-01T12:00:10Z')
    expect(client.getQueryState(['tracking', 7])?.isInvalidated).toBe(false)
  })

  it('mudança nas corridas invalida o painel do entregador', () => {
    const client = new QueryClient()
    client.setQueryData(['driver', 'available'], [])

    applyRealtimeMessage(client, {
      type: 'deliveries.changed',
      payload: { deliveryId: 1, orderId: 7, status: 'WAITING_DRIVER', at: '' },
    })

    expect(client.getQueryState(['driver', 'available'])?.isInvalidated).toBe(true)
  })
})
