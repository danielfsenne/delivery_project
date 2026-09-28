import type { Cart } from '@/core/api/types'
import { formatCurrency } from '@/shared/lib/format'
import { Card } from '@/shared/components/ui'

export function CartSummary({ cart }: { cart: Cart }) {
  return (
    <Card className="p-5">
      <h2 className="font-semibold">Resumo</h2>
      <dl className="mt-3 space-y-2 text-sm">
        <div className="flex justify-between">
          <dt className="text-gray-600">Subtotal</dt>
          <dd>{formatCurrency(cart.subtotal)}</dd>
        </div>
        <div className="flex justify-between">
          <dt className="text-gray-600">Taxa de entrega</dt>
          <dd>{cart.deliveryFee === 0 ? 'Grátis' : formatCurrency(cart.deliveryFee)}</dd>
        </div>
        {cart.discount > 0 && (
          <div className="flex justify-between text-green-700">
            <dt>Desconto</dt>
            <dd>- {formatCurrency(cart.discount)}</dd>
          </div>
        )}
        <div className="flex justify-between border-t border-gray-100 pt-2 text-base font-bold">
          <dt>Total</dt>
          <dd>{formatCurrency(cart.total)}</dd>
        </div>
      </dl>
      {!cart.reachesMinimumOrder && (
        <p className="mt-3 text-xs text-amber-700 bg-amber-50 rounded p-2">
          Pedido mínimo de {formatCurrency(cart.minOrderValue)}. Faltam{' '}
          {formatCurrency(cart.minOrderValue - cart.subtotal)}.
        </p>
      )}
    </Card>
  )
}
