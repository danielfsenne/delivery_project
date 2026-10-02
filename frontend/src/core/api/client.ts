import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { useAuthStore } from '@/core/auth/auth-store'

export const api = axios.create({ baseURL: '/api' })

api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Renova o access token uma única vez quando várias requisições recebem 401 ao mesmo tempo.
let refreshing: Promise<string | null> | null = null

async function refreshAccessToken(): Promise<string | null> {
  const { refreshToken, setTokens, logout } = useAuthStore.getState()
  if (!refreshToken) return null
  try {
    const { data } = await axios.post('/api/auth/refresh', { refreshToken })
    setTokens(data.accessToken, data.refreshToken)
    return data.accessToken as string
  } catch {
    logout()
    return null
  }
}

/** Segundos até o token expirar, lidos do próprio JWT (sem validar a assinatura). */
function secondsUntilExpiry(token: string): number {
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))
    return payload.exp - Date.now() / 1000
  } catch {
    return 0
  }
}

/**
 * Access token válido para uso fora do axios (ex.: CONNECT do WebSocket),
 * renovando antes se faltar menos de 30 segundos para expirar.
 */
export async function freshAccessToken(): Promise<string | null> {
  const token = useAuthStore.getState().accessToken
  if (token && secondsUntilExpiry(token) > 30) return token
  refreshing ??= refreshAccessToken().finally(() => (refreshing = null))
  return refreshing
}

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined
    if (error.response?.status !== 401 || !original || original._retry) {
      return Promise.reject(error)
    }
    original._retry = true
    refreshing ??= refreshAccessToken().finally(() => (refreshing = null))
    const token = await refreshing
    if (!token) return Promise.reject(error)
    original.headers.Authorization = `Bearer ${token}`
    return api(original)
  },
)

export interface ApiError {
  status: number
  message: string
  fields?: Record<string, string>
}

/** Respostas do gateway que chegam sem corpo JSON. */
const statusMessages: Record<number, string> = {
  429: 'Muitas requisições em pouco tempo. Aguarde alguns segundos e tente de novo.',
  502: 'Serviço indisponível no momento. Tente novamente em instantes.',
  503: 'Serviço indisponível no momento. Tente novamente em instantes.',
  504: 'O serviço demorou para responder. Tente novamente.',
}

export function toApiError(error: unknown): ApiError {
  if (axios.isAxiosError(error) && error.response) {
    const { status } = error.response
    const data = (typeof error.response.data === 'object' ? error.response.data : null) as Partial<ApiError> | null
    return {
      status,
      message: data?.message ?? statusMessages[status] ?? 'Erro inesperado',
      fields: data?.fields,
    }
  }
  return { status: 0, message: 'Não foi possível conectar ao servidor' }
}
