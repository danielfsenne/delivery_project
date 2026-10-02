import { lazy, Suspense } from 'react'
import { Bike, MapPin } from 'lucide-react'
import type { Coordinates, DeliveryStatus } from '@/core/api/types'
import { useTracking } from './api'
import { formatTime } from '@/shared/lib/format'
import { Card } from '@/shared/components/ui'

// Leaflet só é baixado quando há uma entrega para mostrar.
const TrackingMap = lazy(() => import('./TrackingMap'))

const deliveryLabel: Record<DeliveryStatus, string> = {
  WAITING_DRIVER: 'Procurando um entregador próximo...',
  ASSIGNED: 'Entregador a caminho do restaurante',
  PICKED_UP: 'Entregador a caminho do seu endereço',
  DELIVERED: 'Pedido entregue',
}

function distanceKm(a: Coordinates, b: Coordinates): number {
  const rad = (d: number) => (d * Math.PI) / 180
  const dLat = rad(b.latitude - a.latitude)
  const dLon = rad(b.longitude - a.longitude)
  const h = Math.sin(dLat / 2) ** 2 + Math.cos(rad(a.latitude)) * Math.cos(rad(b.latitude)) * Math.sin(dLon / 2) ** 2
  return 2 * 6371 * Math.asin(Math.sqrt(h))
}

/**
 * Acompanhamento da entrega. A posição do entregador chega pelo WebSocket e o mapa
 * se move sozinho; sem conexão, o polling de reserva mantém os dados.
 */
export function TrackingCard({ orderId }: { orderId: number }) {
  const { data: tracking } = useTracking(orderId, true)
  if (!tracking) return null

  const target = tracking.status === 'PICKED_UP' ? tracking.dropoff : tracking.pickup
  const remaining = tracking.driverLocation && target ? distanceKm(tracking.driverLocation, target) : null

  return (
    <Card className="p-5">
      <div className="flex items-center gap-3">
        <span className="flex h-10 w-10 items-center justify-center rounded-full bg-brand-100 text-brand-600">
          <Bike size={20} />
        </span>
        <div>
          <p className="font-semibold">{deliveryLabel[tracking.status]}</p>
          {remaining !== null && (
            <p className="text-sm text-gray-600 flex items-center gap-1">
              <MapPin size={14} /> a {remaining < 1 ? `${Math.round(remaining * 1000)} m` : `${remaining.toFixed(1)} km`}
              {tracking.driverLocationUpdatedAt && ` · atualizado às ${formatTime(tracking.driverLocationUpdatedAt)}`}
            </p>
          )}
        </div>
      </div>
      {tracking.status !== 'DELIVERED' && (
        <div className="mt-4">
          <Suspense fallback={<div className="h-64 animate-pulse rounded-lg bg-gray-100" />}>
            <TrackingMap tracking={tracking} />
          </Suspense>
        </div>
      )}
    </Card>
  )
}
