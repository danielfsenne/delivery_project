import { useState, type FormEvent } from 'react'
import { Tag, X } from 'lucide-react'
import type { Cart } from '@/core/api/types'
import { toApiError } from '@/core/api/client'
import { useApplyCoupon, useRemoveCoupon } from './api'
import { Button } from '@/shared/components/ui'

export function CouponForm({ cart }: { cart: Cart }) {
  const [code, setCode] = useState('')
  const apply = useApplyCoupon()
  const remove = useRemoveCoupon()

  if (cart.couponCode) {
    return (
      <div className="rounded-lg border border-dashed border-green-300 bg-green-50 p-3 text-sm">
        <div className="flex items-center justify-between">
          <span className="flex items-center gap-2 font-semibold text-green-800">
            <Tag size={16} /> {cart.couponCode}
          </span>
          <button
            onClick={() => remove.mutate()}
            disabled={remove.isPending}
            className="text-gray-500 hover:text-gray-800"
            aria-label="Remover cupom"
          >
            <X size={16} />
          </button>
        </div>
        {cart.couponError && <p className="mt-1 text-amber-700">{cart.couponError}</p>}
      </div>
    )
  }

  const onSubmit = (e: FormEvent) => {
    e.preventDefault()
    if (code.trim()) apply.mutate(code.trim(), { onSuccess: () => setCode('') })
  }

  return (
    <form onSubmit={onSubmit}>
      <label htmlFor="coupon" className="text-sm font-medium text-gray-700">Cupom de desconto</label>
      <div className="mt-1 flex gap-2">
        <input
          id="coupon"
          value={code}
          onChange={(e) => setCode(e.target.value.toUpperCase())}
          placeholder="Ex.: SAVE10"
          maxLength={30}
          className="flex-1 min-w-0 rounded-lg border border-gray-300 px-3 py-2 text-sm uppercase outline-none focus:border-brand-500"
        />
        <Button type="submit" variant="secondary" loading={apply.isPending} disabled={!code.trim()}>
          Aplicar
        </Button>
      </div>
      {apply.isError && <p className="mt-1 text-xs text-red-600">{toApiError(apply.error).message}</p>}
    </form>
  )
}
