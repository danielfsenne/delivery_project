import { useEffect, useRef, useState } from 'react'
import type { Coordinates } from '@/core/api/types'

const MIN_INTERVAL_MS = 15_000

/**
 * Envia a posição do navegador enquanto {@code enabled} for verdadeiro,
 * no máximo a cada 15 segundos para não sobrecarregar a API.
 */
export function useLocationSharing(enabled: boolean, send: (coords: Coordinates) => void) {
  const [error, setError] = useState<string | null>(null)
  const lastSent = useRef(0)
  const sendRef = useRef(send)
  sendRef.current = send

  useEffect(() => {
    if (!enabled) return
    if (!navigator.geolocation) {
      setError('Seu navegador não permite compartilhar a localização')
      return
    }
    const id = navigator.geolocation.watchPosition(
      (pos) => {
        setError(null)
        const now = Date.now()
        if (now - lastSent.current < MIN_INTERVAL_MS) return
        lastSent.current = now
        sendRef.current({ latitude: pos.coords.latitude, longitude: pos.coords.longitude })
      },
      () => setError('Permita o acesso à localização ou use a posição de demonstração'),
      { enableHighAccuracy: true, maximumAge: 10_000 },
    )
    return () => navigator.geolocation.clearWatch(id)
  }, [enabled])

  return error
}
