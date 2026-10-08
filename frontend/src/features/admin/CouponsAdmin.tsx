import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Plus } from 'lucide-react'
import { useCoupons, useCreateCoupon, useSetCouponActive } from './api'
import { couponSchema, toCouponInput, type CouponForm as CouponFormData } from './couponForm'
import type { Coupon } from '@/core/api/types'
import { toApiError } from '@/core/api/client'
import { cn, formatCurrency, formatDateTime } from '@/shared/lib/format'
import { Badge, Button, Card, ErrorMessage, Input, Spinner } from '@/shared/components/ui'

function CouponForm({ onDone }: { onDone: () => void }) {
  const create = useCreateCoupon()
  const { register, handleSubmit, watch, formState, setError } = useForm<CouponFormData>({
    resolver: zodResolver(couponSchema),
    defaultValues: { type: 'PERCENTAGE', value: '', minOrderValue: '', maxDiscount: '', usageLimit: '' },
  })
  const type = watch('type')
  const errors = formState.errors

  const submit = handleSubmit((data) =>
    create.mutate(toCouponInput(data), {
      onSuccess: onDone,
      onError: (error) => {
        Object.entries(toApiError(error).fields ?? {}).forEach(([field, message]) =>
          setError(field as keyof CouponFormData, { message }),
        )
      },
    }),
  )

  return (
    <form onSubmit={submit} noValidate className="space-y-4 border-b border-gray-100 p-4">
      {create.isError && <ErrorMessage message={toApiError(create.error).message} />}
      <div className="grid gap-3 sm:grid-cols-3">
        <Input label="Código" placeholder="BEMVINDO" {...register('code')} error={errors.code?.message} />
        <div>
          <label htmlFor="coupon-type" className="mb-1 block text-sm font-medium text-gray-700">Tipo</label>
          <select
            id="coupon-type"
            {...register('type')}
            className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm"
          >
            <option value="PERCENTAGE">Percentual</option>
            <option value="FIXED">Valor fixo</option>
          </select>
        </div>
        <Input
          label={type === 'PERCENTAGE' ? 'Desconto (%)' : 'Desconto (R$)'}
          inputMode="decimal"
          {...register('value')}
          error={errors.value?.message}
        />
        <Input label="Pedido mínimo (R$)" inputMode="decimal" {...register('minOrderValue')}
               error={errors.minOrderValue?.message} />
        <Input label="Desconto máximo (R$)" inputMode="decimal" {...register('maxDiscount')}
               error={errors.maxDiscount?.message} />
        <Input label="Limite de usos" inputMode="numeric" {...register('usageLimit')}
               error={errors.usageLimit?.message} />
        <Input label="Válido até" type="date" {...register('validUntil')} />
        <Input label="Descrição" className="sm:col-span-2" {...register('description')}
               error={errors.description?.message} />
      </div>
      <div className="flex justify-end gap-2">
        <Button type="button" variant="ghost" onClick={onDone}>Cancelar</Button>
        <Button type="submit" loading={create.isPending}>Criar cupom</Button>
      </div>
    </form>
  )
}

function describe(coupon: Coupon): string {
  const discount = coupon.type === 'PERCENTAGE' ? `${coupon.value}%` : formatCurrency(coupon.value)
  const parts = [discount]
  if (coupon.minOrderValue > 0) parts.push(`mín. ${formatCurrency(coupon.minOrderValue)}`)
  if (coupon.maxDiscount) parts.push(`até ${formatCurrency(coupon.maxDiscount)}`)
  if (coupon.restaurantId) parts.push(`só no restaurante #${coupon.restaurantId}`)
  return parts.join(' · ')
}

export function CouponsAdmin() {
  const { data: coupons, isLoading, isError, error } = useCoupons()
  const setActive = useSetCouponActive()
  const [creating, setCreating] = useState(false)

  return (
    <Card>
      <div className="flex items-center justify-between gap-3 border-b border-gray-100 p-3">
        <h2 className="font-semibold">Cupons</h2>
        {!creating && (
          <Button className="px-3 py-2" onClick={() => setCreating(true)}>
            <Plus size={16} /> Novo cupom
          </Button>
        )}
      </div>

      {creating && <CouponForm onDone={() => setCreating(false)} />}
      {isLoading && <Spinner />}
      {isError && <div className="p-3"><ErrorMessage message={toApiError(error).message} /></div>}
      {setActive.isError && <div className="p-3"><ErrorMessage message={toApiError(setActive.error).message} /></div>}

      <ul className="divide-y divide-gray-100">
        {coupons?.map((coupon) => (
          <li key={coupon.id} className={cn('flex flex-wrap items-center gap-3 p-4', !coupon.active && 'opacity-60')}>
            <div className="min-w-48 flex-1">
              <p className="font-mono font-semibold">{coupon.code}</p>
              <p className="text-sm text-gray-600">{describe(coupon)}</p>
              {coupon.description && <p className="text-xs text-gray-500">{coupon.description}</p>}
            </div>
            <div className="text-right text-sm text-gray-600">
              <p className="tabular-nums">
                {coupon.usedCount}
                {coupon.usageLimit ? ` / ${coupon.usageLimit}` : ''} usos
              </p>
              <p className="text-xs">
                {coupon.validUntil ? `até ${formatDateTime(coupon.validUntil)}` : 'sem validade'}
              </p>
            </div>
            <Badge className={coupon.active ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-600'}>
              {coupon.active ? 'Ativo' : 'Inativo'}
            </Badge>
            <Button
              variant="secondary"
              className="px-3 py-1.5"
              loading={setActive.isPending && setActive.variables?.id === coupon.id}
              onClick={() => setActive.mutate({ id: coupon.id, active: !coupon.active })}
            >
              {coupon.active ? 'Desativar' : 'Ativar'}
            </Button>
          </li>
        ))}
      </ul>
    </Card>
  )
}
