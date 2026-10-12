import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { MapPin } from 'lucide-react'
import { useRestaurant } from './api'
import { ProductDialog, type ProductSelection } from './ProductDialog'
import { useAddToCart } from '@/features/cart/api'
import { useAuthStore } from '@/core/auth/auth-store'
import { toApiError } from '@/core/api/client'
import type { Product } from '@/core/api/types'
import { cn, formatCurrency } from '@/shared/lib/format'
import { Badge, ErrorMessage, Spinner } from '@/shared/components/ui'
import { Rating } from '@/shared/components/Rating'
import { FoodImage } from '@/shared/components/FoodImage'

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
    <div className="space-y-6">
      <header>
        <div className="relative h-44 overflow-hidden rounded-2xl bg-brand-50 sm:h-56">
          {restaurant.imageUrl && <img src={restaurant.imageUrl} alt="" className="h-full w-full object-cover" />}
          <div className="absolute inset-0 bg-gradient-to-t from-black/40 to-transparent" />
        </div>
        <div className="flex flex-col gap-4 px-2 sm:flex-row sm:items-end sm:justify-between">
          <div className="flex items-end gap-4">
            <FoodImage
              src={restaurant.imageUrl}
              name={restaurant.name}
              className="-mt-10 h-24 w-24 shrink-0 rounded-full border-4 border-white text-4xl shadow-card"
            />
            <div className="pb-1">
              <h1 className="text-2xl font-black leading-tight sm:text-3xl">{restaurant.name}</h1>
              <p className="mt-1 flex flex-wrap items-center gap-x-2 text-sm text-gray-500">
                <Rating average={restaurant.ratingAverage} count={restaurant.ratingCount} />
                <span aria-hidden className="text-gray-300">•</span>
                {restaurant.cuisine}
                {restaurant.minOrderValue > 0 && (
                  <>
                    <span aria-hidden className="text-gray-300">•</span>
                    Pedido mínimo {formatCurrency(restaurant.minOrderValue)}
                  </>
                )}
              </p>
            </div>
          </div>
          <div className="flex shrink-0 items-center gap-4 rounded-xl border border-gray-100 px-4 py-3 text-sm shadow-card">
            <div>
              <p className="text-xs text-gray-500">Entrega</p>
              <p className="font-bold">{restaurant.deliveryTimeMin}-{restaurant.deliveryTimeMax} min</p>
            </div>
            <span className="h-8 w-px bg-gray-100" />
            <div>
              <p className="text-xs text-gray-500">Taxa</p>
              <p className={cn('font-bold', restaurant.deliveryFee === 0 && 'text-emerald-600')}>
                {restaurant.deliveryFee === 0 ? 'Grátis' : formatCurrency(restaurant.deliveryFee)}
              </p>
            </div>
            <span className="h-8 w-px bg-gray-100" />
            <Badge className={restaurant.open ? 'bg-emerald-50 text-emerald-700' : 'bg-gray-100 text-gray-600'}>
              {restaurant.open ? 'Aberto' : 'Fechado'}
            </Badge>
          </div>
        </div>
        {restaurant.description && <p className="mt-4 px-2 text-gray-600">{restaurant.description}</p>}
        <p className="mt-1 flex items-center gap-1 px-2 text-sm text-gray-500">
          <MapPin size={14} /> {restaurant.address.street}, {restaurant.address.number} - {restaurant.address.district}
        </p>
      </header>

      {!restaurant.open && (
        <p className="rounded-xl bg-gray-100 px-4 py-3 text-sm font-semibold text-gray-700">
          Este restaurante está fechado agora. Você pode ver o cardápio, mas não dá para pedir.
        </p>
      )}
      {user && user.role !== 'CUSTOMER' && (
        <p className="text-sm text-gray-500">Entre com uma conta de cliente para fazer pedidos.</p>
      )}

      {restaurant.categories.length > 1 && (
        <nav
          aria-label="Categorias do cardápio"
          className="no-scrollbar sticky top-[124px] z-10 -mx-4 flex gap-6 overflow-x-auto border-b border-gray-100 bg-white px-4 md:top-[72px]"
        >
          {restaurant.categories.map((category) => (
            <a
              key={category.id}
              href={`#cat-${category.id}`}
              className="shrink-0 border-b-2 border-transparent py-3 text-sm font-bold text-gray-600 hover:border-brand-500 hover:text-brand-500"
            >
              {category.name}
            </a>
          ))}
        </nav>
      )}

      <div className="space-y-10">
        {restaurant.categories.map((category) => (
          <section key={category.id} id={`cat-${category.id}`} aria-labelledby={`cat-title-${category.id}`}
                   className="scroll-mt-[180px] md:scroll-mt-[130px]">
            <h2 id={`cat-title-${category.id}`} className="mb-4 text-xl font-extrabold">{category.name}</h2>
            <div className="grid gap-4 sm:grid-cols-2">
              {category.products.map((product) => (
                <button
                  key={product.id}
                  disabled={!product.available || (!!user && !canOrder)}
                  onClick={() => openProduct(product)}
                  className={cn(
                    'flex min-h-[132px] gap-4 rounded-2xl border border-gray-100 bg-white p-4 text-left shadow-card transition',
                    product.available ? 'hover:border-brand-200 hover:shadow-md' : 'cursor-not-allowed opacity-50',
                  )}
                >
                  <div className="flex flex-1 flex-col">
                    <p className="font-bold">{product.name}</p>
                    {product.description && <p className="mt-1 text-sm text-gray-500 line-clamp-2">{product.description}</p>}
                    <p className="mt-auto pt-2 font-bold text-gray-900">
                      {product.available ? formatCurrency(product.price) : 'Indisponível'}
                    </p>
                  </div>
                  {product.imageUrl && (
                    <FoodImage src={product.imageUrl} name={product.name} className="h-24 w-24 shrink-0 rounded-xl" />
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
