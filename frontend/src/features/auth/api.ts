import { api } from '@/core/api/client'
import type { AuthResponse } from '@/core/api/types'
import type { Role } from '@/core/auth/auth-store'

export interface RegisterInput {
  name: string
  email: string
  password: string
  role: Exclude<Role, 'ADMIN'>
  phone?: string
}

export async function login(email: string, password: string): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>('/auth/login', { email, password })
  return data
}

export async function register(input: RegisterInput): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>('/auth/register', {
    ...input,
    phone: input.phone || undefined,
  })
  return data
}

export async function logout(refreshToken: string): Promise<void> {
  await api.post('/auth/logout', { refreshToken })
}
