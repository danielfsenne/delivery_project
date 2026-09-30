import { Link, useNavigate } from 'react-router-dom'
import { Minus, Plus, Trash2 } from 'lucide-react'
import { useCart, useRemoveCartItem, useUpdateCartItem } from './api'
import { toApiError } from '@/core/api/client'
import { formatCurrency } from '@/shared/lib/format'
import { Button, Card, EmptyState, ErrorMessage, Spinner } from '@/shared/components/ui'
import { CartSummary } from './CartSummary'
import { CouponForm } from './CouponForm'

export function CartPage() {
  const navigate = useNavigate()
  const { data: cart, isLoading, isError, error } = useCart()
  const update = useUpdateCartItem()
  const remove = useRemoveCartItem()
  const mutationError = update.error ?? remove.error

  if (isLoading) return <Spinner />
  if (isError || !cart) return <ErrorMessage message={toApiError(error).message} />

  if (cart.items.length === 0) {
    return (
      <EmptyState
        title="Seu carrinho está vazio"
        description="Escolha um restaurante e adicione itens."
        action={<Link to="/" className="font-semibold text-brand-600 hover:underline">Ver restaurantes</Link>}
      />
    )
  }

  const busy = update.isPending || remove.isPending

  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_320px]">
      <Card className="p-5">
        <h1 className="text-xl font-bold">Seu pedido</h1>
        <Link to={`/restaurants/${cart.restaurantId}`} className="text-sm text-brand-600 hover:underline">
          {cart.restaurantName}
        </Link>

        {mutationError && <div className="mt-3"><ErrorMessage message={toApiError(mutationError).message} /></div>}

        <ul className="mt-4 divide-y divide-gray-100">
          {cart.items.map((item) => (
            <li key={item.id} className="py-4 flex gap-4">
              <div className="flex-1">
                <p className="font-semibold">{item.name}</p>
                {item.options.length > 0 && (
                  <p className="text-sm text-gray-500">{item.options.map((o) => o.name).join(', ')}</p>
                )}
                {item.notes && <p className="text-sm text-gray-500 italic">"{item.notes}"</p>}
                <p className="mt-1 text-sm font-medium">{formatCurrency(item.unitPrice * item.quantity)}</p>
              </div>
              <div className="flex items-center gap-1">
                <button
                  className="p-2 rounded hover:bg-gray-100 disabled:opacity-40"
                  disabled={busy}
                  onClick={() => update.mutate({ itemId: item.id, quantity: item.quantity - 1 })}
                  aria-label={`Diminuir ${item.name}`}
                >
                  {item.quantity === 1 ? <Trash2 size={16} /> : <Minus size={16} />}
                </button>
                <span className="w-6 text-center font-semibold">{item.quantity}</span>
                <button
                  className="p-2 rounded hover:bg-gray-100 disabled:opacity-40"
                  disabled={busy || item.quantity >= 50}
                  onClick={() => update.mutate({ itemId: item.id, quantity: item.quantity + 1 })}
                  aria-label={`Aumentar ${item.name}`}
                >
                  <Plus size={16} />
                </button>
              </div>
            </li>
          ))}
        </ul>
      </Card>

      <div className="space-y-4">
        <CartSummary cart={cart} />
        <CouponForm cart={cart} />
        <Button className="w-full" disabled={!cart.reachesMinimumOrder} onClick={() => navigate('/checkout')}>
          Continuar
        </Button>
      </div>
    </div>
  )
}
