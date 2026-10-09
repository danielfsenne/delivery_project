import { useEffect, useState } from 'react'
import { CircleMarker, MapContainer, TileLayer, useMap, useMapEvents } from 'react-leaflet'
import type { LatLngTuple } from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { LocateFixed } from 'lucide-react'
import type { Coordinates } from '@/core/api/types'
import { Button } from '@/shared/components/ui'

/** Centro de São Paulo, onde ficam os restaurantes de demonstração. */
const DEFAULT_CENTER: LatLngTuple = [-23.5505, -46.6333]

function ClickToPlace({ onPick }: { onPick: (c: Coordinates) => void }) {
  useMapEvents({
    click: (e) => onPick({ latitude: e.latlng.lat, longitude: e.latlng.lng }),
  })
  return null
}

/** Leva o mapa até a posição obtida pelo navegador. */
function FlyTo({ target }: { target: LatLngTuple | null }) {
  const map = useMap()
  useEffect(() => {
    if (target) map.flyTo(target, 17)
  }, [map, target])
  return null
}

interface LocationPickerProps {
  value: Coordinates | null
  onChange: (value: Coordinates) => void
  error?: string
}

/**
 * Posição do restaurante: clique no mapa ou use a localização do navegador.
 * É de onde o entregador retira o pedido, usada no cálculo de distância das corridas.
 */
export function LocationPicker({ value, onChange, error }: LocationPickerProps) {
  const [flyTarget, setFlyTarget] = useState<LatLngTuple | null>(null)
  const [geoError, setGeoError] = useState<string>()
  const position: LatLngTuple | null = value ? [value.latitude, value.longitude] : null

  const useMyLocation = () => {
    if (!navigator.geolocation) {
      setGeoError('Seu navegador não informa a localização. Clique no mapa.')
      return
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const coords = { latitude: pos.coords.latitude, longitude: pos.coords.longitude }
        setGeoError(undefined)
        onChange(coords)
        setFlyTarget([coords.latitude, coords.longitude])
      },
      () => setGeoError('Não foi possível obter sua localização. Clique no mapa.'),
      { enableHighAccuracy: true, timeout: 10_000 },
    )
  }

  return (
    <div className="space-y-2">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <p className="text-sm text-gray-600">
          {value
            ? `Posição marcada: ${value.latitude.toFixed(5)}, ${value.longitude.toFixed(5)}`
            : 'Clique no mapa onde fica a entrada do restaurante.'}
        </p>
        <Button type="button" variant="secondary" className="px-3 py-1.5" onClick={useMyLocation}>
          <LocateFixed size={16} /> Usar minha localização
        </Button>
      </div>
      <div className={error ? 'rounded-xl ring-2 ring-red-400' : undefined}>
        <MapContainer
          center={position ?? DEFAULT_CENTER}
          zoom={position ? 16 : 12}
          className="h-72 w-full rounded-xl"
          aria-label="Mapa para marcar a posição do restaurante"
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          <ClickToPlace onPick={onChange} />
          <FlyTo target={flyTarget} />
          {position && (
            <CircleMarker center={position} radius={10} pathOptions={{ color: '#2563eb', fillOpacity: 0.6 }} />
          )}
        </MapContainer>
      </div>
      {(error || geoError) && <p className="text-xs text-red-600">{error ?? geoError}</p>}
    </div>
  )
}
