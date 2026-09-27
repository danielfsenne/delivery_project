import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export type Role = 'CUSTOMER' | 'RESTAURANT' | 'DRIVER' | 'ADMIN'

export interface AuthUser {
  id: number
  name: string
  email: string
  role: Role
}

interface AuthState {
  user: AuthUser | null
  accessToken: string | null
  refreshToken: string | null
  setSession: (session: { user: AuthUser; accessToken: string; refreshToken: string }) => void
  setTokens: (accessToken: string, refreshToken: string) => void
  logout: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      setSession: ({ user, accessToken, refreshToken }) => set({ user, accessToken, refreshToken }),
      setTokens: (accessToken, refreshToken) => set({ accessToken, refreshToken }),
      logout: () => set({ user: null, accessToken: null, refreshToken: null }),
    }),
    { name: 'rota-auth' },
  ),
)
