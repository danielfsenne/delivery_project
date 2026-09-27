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

export function toApiError(error: unknown): ApiError {
  if (axios.isAxiosError(error) && error.response?.data) {
    const data = error.response.data as Partial<ApiError>
    return {
      status: error.response.status,
      message: data.message ?? 'Erro inesperado',
      fields: data.fields,
    }
  }
  return { status: 0, message: 'Não foi possível conectar ao servidor' }
}
