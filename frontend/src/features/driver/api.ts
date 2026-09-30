import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/core/api/client'
import type { Coordinates, Delivery, DeliveryHistory, DriverProfile, DriverStatus } from '@/core/api/types'

const meKey = ['driver', 'me'] as const

export function useDriverMe() {
  return useQuery({
    queryKey: meKey,
    refetchInterval: 10_000,
    queryFn: async () => (await api.get<DriverProfile>('/deliveries/driver/me')).data,
  })
}

export function useAvailableDeliveries(enabled: boolean) {
  return useQuery({
    queryKey: ['driver', 'available'],
    enabled,
    refetchInterval: 8_000,
    queryFn: async () => (await api.get<Delivery[]>('/deliveries/available')).data,
  })
}

export function useDriverHistory() {
  return useQuery({
    queryKey: ['driver', 'history'],
    queryFn: async () => (await api.get<DeliveryHistory>('/deliveries/driver/history')).data,
  })
}

export function useSetDriverStatus() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (status: Exclude<DriverStatus, 'BUSY'>) =>
      (await api.put<DriverProfile>('/deliveries/driver/status', { status })).data,
    onSuccess: (me) => {
      queryClient.setQueryData(meKey, me)
      queryClient.invalidateQueries({ queryKey: ['driver', 'available'] })
    },
  })
}

export function useUpdateLocation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (coords: Coordinates) =>
      (await api.put<DriverProfile>('/deliveries/driver/location', coords)).data,
    onSuccess: (me) => {
      queryClient.setQueryData(meKey, me)
      queryClient.invalidateQueries({ queryKey: ['driver', 'available'] })
    },
  })
}

/** Aceitar, retirar e concluir seguem o mesmo padrão: ação na corrida e atualização do painel. */
export function useDeliveryAction(action: 'accept' | 'pickup' | 'complete') {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (deliveryId: number) => (await api.post<Delivery>(`/deliveries/${deliveryId}/${action}`)).data,
    onSettled: () => queryClient.invalidateQueries({ queryKey: ['driver'] }),
  })
}
