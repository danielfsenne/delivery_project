import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/core/api/client'
import type { Cart } from '@/core/api/types'
import { useAuthStore } from '@/core/auth/auth-store'

export const cartKey = ['cart'] as const

export interface AddItemInput {
  restaurantId: number
  productId: number
  quantity: number
  optionIds: number[]
  notes?: string
  replaceCart?: boolean
}

/** O carrinho só existe para clientes logados. */
export function useCart() {
  const isCustomer = useAuthStore((s) => s.user?.role === 'CUSTOMER')
  return useQuery({
    queryKey: cartKey,
    queryFn: async () => (await api.get<Cart>('/cart')).data,
    enabled: isCustomer,
  })
}

function useCartMutation<TInput>(fn: (input: TInput) => Promise<Cart>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: fn,
    onSuccess: (cart) => queryClient.setQueryData(cartKey, cart),
  })
}

export function useAddToCart() {
  return useCartMutation(async (input: AddItemInput) => (await api.post<Cart>('/cart/items', input)).data)
}

export function useUpdateCartItem() {
  return useCartMutation(
    async ({ itemId, quantity }: { itemId: string; quantity: number }) =>
      (await api.patch<Cart>(`/cart/items/${itemId}`, { quantity })).data,
  )
}

export function useRemoveCartItem() {
  return useCartMutation(async (itemId: string) => (await api.delete<Cart>(`/cart/items/${itemId}`)).data)
}
