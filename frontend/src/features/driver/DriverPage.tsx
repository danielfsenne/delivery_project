import { MapPin, Navigation, Power, Store } from 'lucide-react'
import type { Delivery, DeliveryStatus } from '@/core/api/types'
import { toApiError } from '@/core/api/client'
import {
  useAvailableDeliveries,
  useDeliveryAction,
  useDriverHistory,
  useDriverMe,
  useSetDriverStatus,
  useUpdateLocation,
} from './api'
import { useLocationSharing } from './useLocationSharing'
import { cn, formatCurrency, formatDateTime } from '@/shared/lib/format'
import { Badge, Button, Card, EmptyState, ErrorMessage, Spinner } from '@/shared/components/ui'

/** Centro de Franca/SP, para testar sem GPS. */
const DEMO_LOCATION = { latitude: -20.5395, longitude: -47.4015 }

const statusBadge = {
  ONLINE: { label: 'Online', className: 'bg-green-100 text-green-800' },
  OFFLINE: { label: 'Offline', className: 'bg-gray-200 text-gray-700' },
  BUSY: { label: 'Em entrega', className: 'bg-brand-100 text-brand-700' },
} as const

const stepLabel: Partial<Record<DeliveryStatus, string>> = {
  ASSIGNED: 'Vá até o restaurante e retire o pedido',
  PICKED_UP: 'Leve o pedido até o cliente',
}

function km(value: number | null) {
  return value === null ? '–' : value < 1 ? `${Math.round(value * 1000)} m` : `${value.toFixed(1)} km`
}

function directionsUrl(address: string | null, coords: { latitude: number; longitude: number } | null) {
  const destination = coords ? `${coords.latitude},${coords.longitude}` : encodeURIComponent(address ?? '')
  return `https://www.google.com/maps/dir/?api=1&destination=${destination}`
}

function CurrentDelivery({ delivery }: { delivery: Delivery }) {
  const pickup = useDeliveryAction('pickup')
  const complete = useDeliveryAction('complete')
  const action = delivery.status === 'ASSIGNED' ? pickup : complete
  const goingToCustomer = delivery.status === 'PICKED_UP'

  return (
    <Card className="p-5 border-brand-500 border-2">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-bold">Entrega atual · pedido #{delivery.orderId}</h2>
        <span className="font-semibold text-green-700">{formatCurrency(delivery.driverFee)}</span>
      </div>
      <p className="mt-1 text-sm text-brand-700 font-medium">{stepLabel[delivery.status]}</p>

      <ol className="mt-4 space-y-3 text-sm">
        <li className={cn('flex gap-3', goingToCustomer && 'opacity-50')}>
          <Store size={18} className="mt-0.5 shrink-0" />
          <div>
            <p className="font-semibold">{delivery.restaurantName}</p>
            <p className="text-gray-600">{delivery.pickupAddress}</p>
          </div>
        </li>
        <li className="flex gap-3">
          <MapPin size={18} className="mt-0.5 shrink-0" />
          <div>
            <p className="font-semibold">Cliente</p>
            <p className="text-gray-600">{delivery.dropoffAddress}</p>
            {delivery.tripDistanceKm !== null && <p className="text-xs text-gray-500">{km(delivery.tripDistanceKm)} do restaurante</p>}
          </div>
        </li>
      </ol>

      {action.isError && <div className="mt-3"><ErrorMessage message={toApiError(action.error).message} /></div>}
      <div className="mt-4 flex flex-wrap gap-2">
        <Button onClick={() => action.mutate(delivery.id)} loading={action.isPending} className="flex-1">
          {goingToCustomer ? 'Confirmar entrega' : 'Retirei o pedido'}
        </Button>
        <a
          href={goingToCustomer ? directionsUrl(delivery.dropoffAddress, delivery.dropoff) : directionsUrl(delivery.pickupAddress, delivery.pickup)}
          target="_blank"
          rel="noreferrer"
          className="inline-flex items-center gap-1 rounded-lg border border-gray-300 px-4 py-2.5 text-sm font-semibold hover:bg-gray-50"
        >
          <Navigation size={16} /> Rota
        </a>
      </div>
    </Card>
  )
}

function AvailableList({ enabled }: { enabled: boolean }) {
  const { data: deliveries, isLoading } = useAvailableDeliveries(enabled)
  const accept = useDeliveryAction('accept')

  if (!enabled) return null
  if (isLoading) return <Spinner label="Procurando entregas..." />

  return (
    <section>
      <h2 className="mb-3 font-semibold">Entregas disponíveis</h2>
      {accept.isError && <div className="mb-3"><ErrorMessage message={toApiError(accept.error).message} /></div>}
      {!deliveries?.length ? (
        <EmptyState title="Nenhuma entrega por perto" description="A lista atualiza sozinha a cada poucos segundos." />
      ) : (
        <div className="grid gap-3 sm:grid-cols-2">
          {deliveries.map((d) => (
            <Card key={d.id} className="p-4 text-sm">
              <div className="flex items-start justify-between">
                <div>
                  <p className="font-semibold">{d.restaurantName}</p>
                  <p className="text-gray-500">{d.pickupAddress}</p>
                </div>
                <span className="text-lg font-bold text-green-700">{formatCurrency(d.driverFee)}</span>
              </div>
              <p className="mt-2 text-gray-600">
                Até o restaurante: <strong>{km(d.distanceToPickupKm)}</strong> · Corrida: <strong>{km(d.tripDistanceKm)}</strong>
              </p>
              <p className="text-gray-500 truncate">Destino: {d.dropoffAddress}</p>
              <Button
                className="mt-3 w-full"
                loading={accept.isPending && accept.variables === d.id}
                disabled={accept.isPending}
                onClick={() => accept.mutate(d.id)}
              >
                Aceitar entrega
              </Button>
            </Card>
          ))}
        </div>
      )}
    </section>
  )
}

function History() {
  const { data } = useDriverHistory()
  if (!data || data.deliveries.length === 0) return null
  return (
    <section>
      <h2 className="mb-3 font-semibold">Histórico</h2>
      <Card>
        <ul className="divide-y divide-gray-100 text-sm">
          {data.deliveries.map((d) => (
            <li key={d.id} className="flex justify-between p-3">
              <span>
                #{d.orderId} · {d.restaurantName}
                <span className="block text-xs text-gray-500">{d.deliveredAt && formatDateTime(d.deliveredAt)}</span>
              </span>
              <span className="font-semibold text-green-700">{formatCurrency(d.driverFee)}</span>
            </li>
          ))}
        </ul>
      </Card>
    </section>
  )
}

export function DriverPage() {
  const { data: me, isLoading, isError, error } = useDriverMe()
  const setStatus = useSetDriverStatus()
  const updateLocation = useUpdateLocation()
  const sharing = me?.status === 'ONLINE' || me?.status === 'BUSY'
  const locationError = useLocationSharing(sharing, (coords) => updateLocation.mutate(coords))

  if (isLoading) return <Spinner />
  if (isError || !me) return <ErrorMessage message={toApiError(error).message} />

  const badge = statusBadge[me.status]

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <Card className="p-5">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h1 className="text-2xl font-bold">Painel do entregador</h1>
            <Badge className={cn('mt-1', badge.className)}>{badge.label}</Badge>
          </div>
          {me.status !== 'BUSY' && (
            <Button
              variant={me.status === 'ONLINE' ? 'secondary' : 'primary'}
              loading={setStatus.isPending}
              onClick={() => setStatus.mutate(me.status === 'ONLINE' ? 'OFFLINE' : 'ONLINE')}
            >
              <Power size={16} /> {me.status === 'ONLINE' ? 'Ficar offline' : 'Ficar online'}
            </Button>
          )}
        </div>

        <div className="mt-4 grid grid-cols-2 gap-3 text-center">
          <div className="rounded-lg bg-gray-50 p-3">
            <p className="text-xs uppercase text-gray-500">Entregas</p>
            <p className="text-xl font-bold">{me.completedDeliveries}</p>
          </div>
          <div className="rounded-lg bg-gray-50 p-3">
            <p className="text-xs uppercase text-gray-500">Ganhos</p>
            <p className="text-xl font-bold text-green-700">{formatCurrency(me.totalEarnings)}</p>
          </div>
        </div>

        <div className="mt-4 flex flex-wrap items-center gap-2 text-sm text-gray-600">
          <MapPin size={16} />
          {me.location ? `Localização: ${me.location.latitude.toFixed(4)}, ${me.location.longitude.toFixed(4)}` : 'Localização não enviada'}
          <button
            className="text-brand-600 font-medium hover:underline"
            onClick={() => updateLocation.mutate(DEMO_LOCATION)}
          >
            Usar posição de demonstração
          </button>
        </div>
        {setStatus.isError && <div className="mt-3"><ErrorMessage message={toApiError(setStatus.error).message} /></div>}
        {sharing && locationError && <p className="mt-2 text-xs text-amber-700">{locationError}</p>}
      </Card>

      {me.currentDelivery && <CurrentDelivery delivery={me.currentDelivery} />}
      <AvailableList enabled={me.status === 'ONLINE' && !me.currentDelivery} />
      {me.status === 'OFFLINE' && (
        <EmptyState title="Você está offline" description="Fique online para receber entregas próximas." />
      )}
      <History />
    </div>
  )
}
