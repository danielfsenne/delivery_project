import { create } from 'zustand'

/** Indica se o WebSocket está conectado (o polling das telas vira só reserva). */
export const useRealtimeStatus = create<{ connected: boolean }>(() => ({ connected: false }))

const FALLBACK_POLLING_MS = 30_000

/**
 * Intervalo de polling de uma tela: com o WebSocket conectado, as mudanças chegam por ele
 * e o polling só cobre algum evento perdido; sem conexão, volta ao intervalo curto.
 */
export function usePollingInterval(whenDisconnectedMs: number) {
  const connected = useRealtimeStatus((s) => s.connected)
  return connected ? Math.max(whenDisconnectedMs, FALLBACK_POLLING_MS) : whenDisconnectedMs
}
