import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Clock, Bike, MapPin } from 'lucide-react'
import { useRestaurant } from './api'
import { ProductDialog, type ProductSelection } from './ProductDialog'
import { useAddToCart } from '@/features/cart/api'
import { useAuthStore } from '@/core/auth/auth-store'
import { toApiError } from '@/core/api/client'
import type { Product } from '@/core/api/types'
import { cn, formatCurrency } from '@/shared/lib/format'
import { Badge, ErrorMessage, Spinner } from '@/shared/components/ui'
import { Rating } from '@/shared/components/Rating'

export function RestaurantPage() {
  const { id } = useParams()
  const restaurantId = Number(id)
  const navigate = useNavigate()
  const user = useAuthStore((s) => s.user)
  const { data: restaurant, isLoading, isError, error } = useRestaurant(restaurantId)
  const addToCart = useAddToCart()
  const [selected, setSelected] = useState<Product | null>(null)

  if (isLoading) return <Spinner />
  if (isError || !restaurant) return <ErrorMessage message={toApiError(error).message} />

  const openProduct = (product: Product) => {
    if (!user) {
      navigate('/login', { state: { from: `/restaurants/${restaurantId}` } })
      return
    }
    addToCart.reset()
    setSelected(product)
  }

  const confirm = (selection: ProductSelection, replaceCart = false) => {
    if (!selected) return
    addToCart.mutate(
      { restaurantId, productId: selected.id, ...selection, replaceCart },
      {
        onSuccess: () => setSelected(null),
        onError: (err) => {
          const apiError = toApiError(err)
          if (apiError.status === 409 && window.confirm(`${apiError.message}\n\nDeseja esvaziar o carrinho e continuar?`)) {
            confirm(selection, true)
          }
        },
      },
    )
  }

  const canOrder = user?.role === 'CUSTOMER' && restaurant.open
  const addError = addToCart.isError && toApiError(addToCart.error).status !== 409
    ? toApiError(addToCart.error).message
    : undefined

  return (
    <div>
      <header className="rounded-2xl overflow-hidden bg-white border border-gray-200">
        {restaurant.imageUrl && <img src={restaurant.imageUrl} alt="" className="h-48 w-full object-cover" />}
        <div className="p-5">
          <div className="flex items-start justify-between gap-4">
            <div>
              <h1 className="text-2xl font-bold">{restaurant.name}</h1>
              <p className="text-gray-600">{restaurant.description}</p>
              <Rating average={restaurant.ratingAverage} count={restaurant.ratingCount} className="mt-1" />
            </div>
            <Badge className={restaurant.open ? 'bg-green-100 text-green-800' : 'bg-gray-200 text-gray-700'}>
              {restaurant.open ? 'Aberto' : 'Fechado'}
            </Badge>
          </div>
          <div className="mt-3 flex flex-wrap gap-x-5 gap-y-1 text-sm text-gray-600">
            <span className="flex items-center gap-1">
              <Clock size={15} /> {restaurant.deliveryTimeMin}-{restaurant.deliveryTimeMax} min
            </span>
            <span className="flex items-center gap-1">
              <Bike size={15} /> {restaurant.deliveryFee === 0 ? 'Entrega grátis' : formatCurrency(restaurant.deliveryFee)}
            </span>
            <span className="flex items-center gap-1">
              <MapPin size={15} /> {restaurant.address.street}, {restaurant.address.number} - {restaurant.address.district}
            </span>
            {restaurant.minOrderValue > 0 && <span>Pedido mínimo {formatCurrency(restaurant.minOrderValue)}</span>}
          </div>
        </div>
      </header>

      {user && user.role !== 'CUSTOMER' && (
        <p className="mt-4 text-sm text-gray-500">Entre com uma conta de cliente para fazer pedidos.</p>
      )}

      <div className="mt-6 space-y-8">
        {restaurant.categories.map((category) => (
          <section key={category.id} aria-labelledby={`cat-${category.id}`}>
            <h2 id={`cat-${category.id}`} className="text-lg font-bold mb-3">{category.name}</h2>
            <div className="grid gap-3 sm:grid-cols-2">
              {category.products.map((product) => (
                <button
                  key={product.id}
                  disabled={!product.available || (!!user && !canOrder)}
                  onClick={() => openProduct(product)}
                  className={cn(
                    'text-left rounded-xl bg-white border border-gray-200 p-4 flex gap-4 transition',
                    product.available ? 'hover:border-brand-500 hover:shadow-sm' : 'opacity-50 cursor-not-allowed',
                  )}
                >
                  <div className="flex-1">
                    <p className="font-semibold">{product.name}</p>
                    {product.description && <p className="text-sm text-gray-500 line-clamp-2">{product.description}</p>}
                    <p className="mt-2 font-semibold text-gray-900">
                      {product.available ? formatCurrency(product.price) : 'Indisponível'}
                    </p>
                  </div>
                  {product.imageUrl && (
                    <img src={product.imageUrl} alt="" className="h-20 w-20 rounded-lg object-cover" loading="lazy" />
                  )}
                </button>
              ))}
            </div>
          </section>
        ))}
      </div>

      {selected && (
        <ProductDialog
          product={selected}
          onClose={() => setSelected(null)}
          onConfirm={(selection) => confirm(selection)}
          loading={addToCart.isPending}
          error={addError}
        />
      )}
    </div>
  )
}
