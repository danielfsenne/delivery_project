import { useEffect } from 'react'
import { CircleMarker, MapContainer, Polyline, TileLayer, Tooltip, useMap } from 'react-leaflet'
import type { LatLngTuple } from 'leaflet'
import 'leaflet/dist/leaflet.css'
import type { Tracking } from '@/core/api/types'

const colors = { driver: '#ea580c', pickup: '#2563eb', dropoff: '#16a34a' }

function point(c: { latitude: number; longitude: number } | null): LatLngTuple | null {
  return c ? [c.latitude, c.longitude] : null
}

/** Enquadra os pontos na primeira renderização e acompanha o entregador quando ele se move. */
function FollowDriver({ points, driver }: { points: LatLngTuple[]; driver: LatLngTuple | null }) {
  const map = useMap()
  useEffect(() => {
    if (points.length > 1) map.fitBounds(points, { padding: [32, 32], maxZoom: 16 })
    // Enquadrar só uma vez: depois o usuário pode mover o mapa livremente.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [map])
  useEffect(() => {
    if (driver && !map.getBounds().pad(-0.1).contains(driver)) map.panTo(driver)
  }, [map, driver])
  return null
}

/**
 * Mapa da entrega: restaurante, endereço do cliente e entregador. A posição do entregador
 * vem do cache do acompanhamento, atualizado pelo WebSocket.
 */
export default function TrackingMap({ tracking }: { tracking: Tracking }) {
  const driver = point(tracking.driverLocation)
  const pickup = point(tracking.pickup)
  const dropoff = point(tracking.dropoff)
  const points = [driver, pickup, dropoff].filter((p): p is LatLngTuple => p !== null)
  if (points.length === 0) return null

  const target = tracking.status === 'PICKED_UP' ? dropoff : pickup

  return (
    <MapContainer center={points[0]} zoom={15} scrollWheelZoom={false} className="h-64 w-full rounded-lg z-0">
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      {pickup && (
        <CircleMarker center={pickup} radius={8} pathOptions={{ color: colors.pickup, fillOpacity: 0.9 }}>
          <Tooltip>Restaurante</Tooltip>
        </CircleMarker>
      )}
      {dropoff && (
        <CircleMarker center={dropoff} radius={8} pathOptions={{ color: colors.dropoff, fillOpacity: 0.9 }}>
          <Tooltip>Seu endereço</Tooltip>
        </CircleMarker>
      )}
      {driver && target && (
        <Polyline positions={[driver, target]} pathOptions={{ color: colors.driver, dashArray: '6 8', weight: 3 }} />
      )}
      {driver && (
        <CircleMarker center={driver} radius={10} pathOptions={{ color: '#fff', weight: 3, fillColor: colors.driver, fillOpacity: 1 }}>
          <Tooltip permanent direction="top" offset={[0, -10]}>Entregador</Tooltip>
        </CircleMarker>
      )}
      <FollowDriver points={points} driver={driver} />
    </MapContainer>
  )
}
