import { useEffect, type ReactNode } from 'react'
import { Controller, useForm, type FieldPath } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { LocationPicker } from './LocationPicker'
import { OpeningHoursEditor } from './OpeningHoursEditor'
import { restaurantSchema, toRestaurantRequest, type RestaurantForm as FormData, type RestaurantRequest } from './restaurantForm'
import { toApiError } from '@/core/api/client'
import { Button, Card, ErrorMessage, Input } from '@/shared/components/ui'

function Section({ title, description, children }: { title: string; description?: string; children: ReactNode }) {
  return (
    <Card className="p-5">
      <h2 className="font-semibold">{title}</h2>
      {description && <p className="mt-0.5 text-sm text-gray-500">{description}</p>}
      <div className="mt-4">{children}</div>
    </Card>
  )
}

interface RestaurantDetailsFormProps {
  initial: FormData
  submitLabel: string
  saving: boolean
  error: unknown
  onSubmit: (request: RestaurantRequest) => void
}

export function RestaurantDetailsForm({ initial, submitLabel, saving, error, onSubmit }: RestaurantDetailsFormProps) {
  const { register, handleSubmit, control, formState, setError } = useForm<FormData>({
    resolver: zodResolver(restaurantSchema),
    defaultValues: initial,
  })
  const errors = formState.errors
  const apiError = error ? toApiError(error) : null
  const fieldErrors = Object.entries(apiError?.fields ?? {})

  // Erros de validação do backend (ex.: "address.zipCode") voltam para o campo certo.
  useEffect(() => {
    if (!error) return
    Object.entries(toApiError(error).fields ?? {}).forEach(([field, message]) =>
      setError(field as FieldPath<FormData>, { message }),
    )
  }, [error, setError])

  return (
    <form onSubmit={handleSubmit((data) => onSubmit(toRestaurantRequest(data)))} noValidate className="space-y-4">
      {apiError && fieldErrors.length === 0 && <ErrorMessage message={apiError.message} />}

      <Section title="Dados do restaurante">
        <div className="grid gap-3 sm:grid-cols-2">
          <Input label="Nome" {...register('name')} error={errors.name?.message} />
          <Input label="Tipo de cozinha" placeholder="Pizza, Japonesa, Lanches..." {...register('cuisine')}
                 error={errors.cuisine?.message} />
          <Input label="Telefone (opcional)" type="tel" {...register('phone')} error={errors.phone?.message} />
          <Input label="URL da imagem (opcional)" type="url" placeholder="https://..." {...register('imageUrl')}
                 error={errors.imageUrl?.message} />
          <div className="sm:col-span-2">
            <label htmlFor="description" className="mb-1 block text-sm font-medium text-gray-700">
              Descrição (opcional)
            </label>
            <textarea
              id="description"
              rows={3}
              {...register('description')}
              className="w-full rounded-lg border border-gray-300 px-3 py-2.5 text-sm outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-500/30"
            />
            {errors.description && <p className="mt-1 text-xs text-red-600">{errors.description.message}</p>}
          </div>
        </div>
      </Section>

      <Section title="Entrega">
        <div className="grid gap-3 sm:grid-cols-4">
          <Input label="Taxa de entrega (R$)" inputMode="decimal" {...register('deliveryFee')}
                 error={errors.deliveryFee?.message} />
          <Input label="Pedido mínimo (R$)" inputMode="decimal" {...register('minOrderValue')}
                 error={errors.minOrderValue?.message} />
          <Input label="Tempo mínimo (min)" inputMode="numeric" {...register('deliveryTimeMin')}
                 error={errors.deliveryTimeMin?.message} />
          <Input label="Tempo máximo (min)" inputMode="numeric" {...register('deliveryTimeMax')}
                 error={errors.deliveryTimeMax?.message} />
        </div>
      </Section>

      <Section title="Endereço" description="Onde o entregador retira os pedidos.">
        <div className="grid gap-3 sm:grid-cols-6">
          <Input label="CEP" className="sm:col-span-2" inputMode="numeric" placeholder="00000-000"
                 {...register('address.zipCode')} error={errors.address?.zipCode?.message} />
          <Input label="Rua" className="sm:col-span-4" {...register('address.street')}
                 error={errors.address?.street?.message} />
          <Input label="Número" className="sm:col-span-2" {...register('address.number')}
                 error={errors.address?.number?.message} />
          <Input label="Complemento" className="sm:col-span-4" {...register('address.complement')}
                 error={errors.address?.complement?.message} />
          <Input label="Bairro" className="sm:col-span-2" {...register('address.district')}
                 error={errors.address?.district?.message} />
          <Input label="Cidade" className="sm:col-span-3" {...register('address.city')}
                 error={errors.address?.city?.message} />
          <Input label="UF" maxLength={2} placeholder="SP" {...register('address.state')}
                 error={errors.address?.state?.message} />
        </div>
        <div className="mt-4">
          <Controller
            control={control}
            name="location"
            render={({ field, fieldState }) => (
              <LocationPicker value={field.value} onChange={field.onChange} error={fieldState.error?.message} />
            )}
          />
        </div>
      </Section>

      <Section title="Horário de funcionamento" description="Fora destes horários a loja aparece como fechada.">
        <Controller
          control={control}
          name="openingHours"
          render={({ field, fieldState }) => (
            <OpeningHoursEditor
              value={field.value}
              onChange={field.onChange}
              error={fieldState.error?.message ?? fieldState.error?.root?.message}
            />
          )}
        />
      </Section>

      <div className="flex justify-end">
        <Button type="submit" loading={saving}>{submitLabel}</Button>
      </div>
    </form>
  )
}
