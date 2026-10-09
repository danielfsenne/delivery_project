import { Link, useNavigate } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { useCreateRestaurant } from './api'
import { RestaurantDetailsForm } from './RestaurantDetailsForm'
import { emptyRestaurantForm } from './restaurantForm'

export function NewRestaurantPage() {
  const navigate = useNavigate()
  const create = useCreateRestaurant()

  return (
    <div className="mx-auto max-w-3xl space-y-4">
      <Link to="/partner" className="inline-flex items-center gap-1 text-sm text-gray-600 hover:text-gray-900">
        <ArrowLeft size={16} /> Painel do restaurante
      </Link>
      <div>
        <h1 className="text-2xl font-bold">Cadastrar restaurante</h1>
        <p className="text-gray-500">Depois de salvar, monte o cardápio na aba Cardápio do painel.</p>
      </div>
      <RestaurantDetailsForm
        initial={emptyRestaurantForm}
        submitLabel="Cadastrar restaurante"
        saving={create.isPending}
        error={create.error}
        onSubmit={(request) =>
          create.mutate(request, {
            onSuccess: (restaurant) => navigate(`/partner?restaurante=${restaurant.id}&aba=menu`),
          })
        }
      />
    </div>
  )
}
