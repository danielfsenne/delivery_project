import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/core/api/client'
import type { AdminUser, Coupon, CouponType, OrderStatus, OrderSummary, Page, PlatformStats } from '@/core/api/types'
import type { Role } from '@/core/auth/auth-store'
import { usePollingInterval } from '@/core/realtime/status'

export function usePlatformStats() {
  const refetchInterval = usePollingInterval(30_000)
  return useQuery({
    queryKey: ['admin', 'stats'],
    refetchInterval,
    queryFn: async () => (await api.get<PlatformStats>('/orders/admin/stats')).data,
  })
}

export function useAdminOrders(status: OrderStatus | '', page: number) {
  return useQuery({
    queryKey: ['admin', 'orders', status, page],
    placeholderData: keepPreviousData,
    queryFn: async () => {
      const params = new URLSearchParams({ page: String(page), size: '20' })
      if (status) params.set('status', status)
      return (await api.get<Page<OrderSummary>>(`/orders/admin?${params}`)).data
    },
  })
}

export function useCoupons() {
  return useQuery({
    queryKey: ['admin', 'coupons'],
    queryFn: async () => (await api.get<Coupon[]>('/orders/coupons')).data,
  })
}

export interface CouponInput {
  code: string
  description?: string
  type: CouponType
  value: number
  minOrderValue?: number
  maxDiscount?: number
  validUntil?: string
  usageLimit?: number
}

export function useCreateCoupon() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (input: CouponInput) => (await api.post<Coupon>('/orders/coupons', input)).data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin', 'coupons'] }),
  })
}

export function useSetCouponActive() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({ id, active }: { id: number; active: boolean }) =>
      (await api.patch<Coupon>(`/orders/coupons/${id}/status`, { active })).data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin', 'coupons'] }),
  })
}

export function useAdminUsers(role: Role | '', search: string, page: number) {
  return useQuery({
    queryKey: ['admin', 'users', role, search, page],
    placeholderData: keepPreviousData,
    queryFn: async () => {
      const params = new URLSearchParams({ page: String(page), size: '20' })
      if (role) params.set('role', role)
      if (search.trim()) params.set('q', search.trim())
      return (await api.get<Page<AdminUser>>(`/auth/admin/users?${params}`)).data
    },
  })
}

export function useSetUserActive() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({ id, active }: { id: number; active: boolean }) =>
      (await api.patch<AdminUser>(`/auth/admin/users/${id}/status`, { active })).data,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin', 'users'] }),
  })
}
