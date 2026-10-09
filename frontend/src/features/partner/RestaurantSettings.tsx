import { useState } from 'react'
import { useManagedRestaurant, useSetRestaurantActive, useUpdateRestaurant } from './api'
import { RestaurantDetailsForm } from './RestaurantDetailsForm'
import { fromDetail } from './restaurantForm'
import { toApiError } from '@/core/api/client'
import { Badge, Button, Card, ErrorMessage, Spinner } from '@/shared/components/ui'

export function RestaurantSettings({ restaurantId }: { restaurantId: number }) {
  const { data: restaurant, isLoading, isError, error } = useManagedRestaurant(restaurantId)
  const update = useUpdateRestaurant(restaurantId)
  const setActive = useSetRestaurantActive(restaurantId)
  const [saved, setSaved] = useState(false)

  if (isLoading) return <Spinner />
  if (isError || !restaurant) return <ErrorMessage message={toApiError(error).message} />

  const status = !restaurant.active
    ? { label: 'Pausado', className: 'bg-gray-200 text-gray-700', hint: 'Não aparece como aberto e não recebe pedidos.' }
    : restaurant.open
      ? { label: 'Aberto agora', className: 'bg-green-100 text-green-800', hint: 'Recebendo pedidos.' }
      : { label: 'Fora do horário', className: 'bg-amber-100 text-amber-800', hint: 'Abre de novo no próximo horário de funcionamento.' }

  return (
    <div className="space-y-4">
      <Card className="flex flex-wrap items-center gap-3 p-5">
        <div className="flex-1">
          <div className="flex items-center gap-2">
            <h2 className="font-semibold">Loja</h2>
            <Badge className={status.className}>{status.label}</Badge>
          </div>
          <p className="mt-0.5 text-sm text-gray-500">{status.hint}</p>
        </div>
        <Button
          variant={restaurant.active ? 'secondary' : 'primary'}
          loading={setActive.isPending}
          onClick={() => setActive.mutate(!restaurant.active)}
        >
          {restaurant.active ? 'Pausar loja' : 'Reabrir loja'}
        </Button>
        {setActive.isError && <ErrorMessage message={toApiError(setActive.error).message} />}
      </Card>

      {saved && !update.isPending && (
        <p role="status" className="rounded-lg border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-800">
          Alterações salvas.
        </p>
      )}
      <RestaurantDetailsForm
        // Recria o formulário ao trocar de restaurante, para não misturar os dados.
        key={restaurant.id}
        initial={fromDetail(restaurant)}
        submitLabel="Salvar alterações"
        saving={update.isPending}
        error={update.error}
        onSubmit={(request) => {
          setSaved(false)
          update.mutate(request, { onSuccess: () => setSaved(true) })
        }}
      />
    </div>
  )
}
