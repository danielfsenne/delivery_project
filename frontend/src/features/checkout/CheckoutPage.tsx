import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Navigate, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { Banknote, CreditCard, LocateFixed, QrCode } from 'lucide-react'
import { useCart } from '@/features/cart/api'
import { CartSummary } from '@/features/cart/CartSummary'
import { useCheckout } from '@/features/orders/api'
import { toApiError } from '@/core/api/client'
import type { Coordinates, PaymentMethod } from '@/core/api/types'
import { Button, Card, ErrorMessage, Input, Spinner } from '@/shared/components/ui'
import { cn } from '@/shared/lib/format'

const schema = z.object({
  street: z.string().trim().min(3, 'Informe a rua'),
  number: z.string().trim().min(1, 'Informe o número'),
  complement: z.string().optional(),
  district: z.string().trim().min(2, 'Informe o bairro'),
  city: z.string().trim().min(2, 'Informe a cidade'),
  state: z.string().trim().toUpperCase().regex(/^[A-Z]{2}$/, 'UF com 2 letras'),
  zipCode: z.string().trim().regex(/^\d{5}-?\d{3}$/, 'CEP inválido'),
  paymentMethod: z.enum(['PIX', 'CREDIT_CARD', 'CASH']),
  notes: z.string().max(300).optional(),
})

type FormData = z.infer<typeof schema>

const paymentOptions: { value: PaymentMethod; label: string; icon: typeof QrCode }[] = [
  { value: 'PIX', label: 'Pix', icon: QrCode },
  { value: 'CREDIT_CARD', label: 'Cartão', icon: CreditCard },
  { value: 'CASH', label: 'Dinheiro', icon: Banknote },
]

export function CheckoutPage() {
  const navigate = useNavigate()
  const { data: cart, isLoading } = useCart()
  const checkout = useCheckout()
  const [coords, setCoords] = useState<Coordinates | null>(null)
  const [locating, setLocating] = useState(false)
  const [locationError, setLocationError] = useState<string | null>(null)

  const locate = () => {
    if (!navigator.geolocation) {
      setLocationError('Seu navegador não permite obter a localização')
      return
    }
    setLocating(true)
    setLocationError(null)
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setCoords({ latitude: pos.coords.latitude, longitude: pos.coords.longitude })
        setLocating(false)
      },
      () => {
        setLocationError('Não foi possível obter sua localização')
        setLocating(false)
      },
      { enableHighAccuracy: true, timeout: 10_000 },
    )
  }

  const { register, handleSubmit, watch, setValue, formState } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { city: 'Franca', state: 'SP', paymentMethod: 'PIX' },
  })
  const payment = watch('paymentMethod')

  if (isLoading) return <Spinner />
  if (!cart || cart.items.length === 0) {
    return checkout.isSuccess ? <Spinner /> : <Navigate to="/cart" replace />
  }

  const onSubmit = ({ paymentMethod, notes, ...address }: FormData) =>
    checkout.mutate(
      { deliveryAddress: { ...address, ...coords }, paymentMethod, notes: notes || undefined },
      { onSuccess: (order) => navigate(`/orders/${order.id}`, { replace: true }) },
    )

  const e = formState.errors

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate className="grid gap-6 lg:grid-cols-[1fr_320px]">
      <div className="space-y-6">
        <Card className="p-5">
          <div className="flex items-center justify-between gap-2">
            <h1 className="text-xl font-bold">Endereço de entrega</h1>
            <Button type="button" variant="ghost" onClick={locate} loading={locating}>
              <LocateFixed size={16} /> {coords ? 'Localização obtida' : 'Usar minha localização'}
            </Button>
          </div>
          {locationError && <p className="mt-1 text-xs text-amber-700">{locationError}</p>}
          {coords && (
            <p className="mt-1 text-xs text-gray-500">
              O entregador verá a distância até você ({coords.latitude.toFixed(4)}, {coords.longitude.toFixed(4)}).
            </p>
          )}
          <div className="mt-4 grid gap-4 sm:grid-cols-6">
            <Input className="sm:col-span-4" label="Rua" {...register('street')} error={e.street?.message} />
            <Input className="sm:col-span-2" label="Número" {...register('number')} error={e.number?.message} />
            <Input className="sm:col-span-3" label="Complemento" {...register('complement')} />
            <Input className="sm:col-span-3" label="Bairro" {...register('district')} error={e.district?.message} />
            <Input className="sm:col-span-3" label="Cidade" {...register('city')} error={e.city?.message} />
            <Input className="sm:col-span-1" label="UF" maxLength={2} {...register('state')} error={e.state?.message} />
            <Input className="sm:col-span-2" label="CEP" inputMode="numeric" {...register('zipCode')}
                   error={e.zipCode?.message} />
          </div>
        </Card>

        <Card className="p-5">
          <h2 className="text-lg font-bold">Pagamento</h2>
          <div className="mt-3 grid grid-cols-3 gap-2" role="radiogroup">
            {paymentOptions.map(({ value, label, icon: Icon }) => (
              <button
                type="button"
                role="radio"
                aria-checked={payment === value}
                key={value}
                onClick={() => setValue('paymentMethod', value)}
                className={cn(
                  'flex flex-col items-center gap-1 rounded-lg border p-3 text-sm font-medium transition',
                  payment === value ? 'border-brand-500 bg-brand-50 text-brand-700' : 'border-gray-300 hover:border-gray-400',
                )}
              >
                <Icon size={20} /> {label}
              </button>
            ))}
          </div>
          <div className="mt-4">
            <label htmlFor="notes" className="text-sm font-medium text-gray-700">Observações para o restaurante</label>
            <textarea id="notes" rows={2} maxLength={300} {...register('notes')}
                      className="mt-1 w-full rounded-lg border border-gray-300 p-2 text-sm outline-none focus:border-brand-500" />
          </div>
        </Card>
      </div>

      <div className="space-y-4">
        <CartSummary cart={cart} />
        {checkout.isError && <ErrorMessage message={toApiError(checkout.error).message} />}
        <Button type="submit" className="w-full" loading={checkout.isPending} disabled={!cart.reachesMinimumOrder}>
          Fazer pedido
        </Button>
      </div>
    </form>
  )
}
