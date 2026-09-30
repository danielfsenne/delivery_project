import { useParams } from 'react-router-dom'
import { RefreshCw } from 'lucide-react'
import { useCancelOrder, useOrder, usePayOrder } from './api'
import { OrderTimeline } from './OrderTimeline'
import { ReviewCard } from './ReviewCard'
import { TrackingCard } from './TrackingCard'
import { customerCanCancel, paymentLabel, statusColor, statusLabel } from './status'
import { toApiError } from '@/core/api/client'
import { useAuthStore } from '@/core/auth/auth-store'
import { formatCurrency, formatDateTime } from '@/shared/lib/format'
import { Badge, Button, Card, ErrorMessage, Spinner } from '@/shared/components/ui'

export function OrderDetailPage() {
  const orderId = Number(useParams().id)
  const isCustomer = useAuthStore((s) => s.user?.role === 'CUSTOMER')
  const { data: order, isLoading, isError, error } = useOrder(orderId)
  const cancel = useCancelOrder(orderId)
  const pay = usePayOrder(orderId)

  if (isLoading) return <Spinner />
  if (isError || !order) return <ErrorMessage message={toApiError(error).message} />

  const onCancel = () => {
    const reason = window.prompt('Por que deseja cancelar? (opcional)')
    if (reason !== null) cancel.mutate(reason || undefined)
  }

  const a = order.deliveryAddress
  const onTheWay = order.status === 'READY_FOR_PICKUP' || order.status === 'OUT_FOR_DELIVERY'
  const actionError = cancel.error ?? pay.error

  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_340px]">
      <div className="space-y-4">
        <Card className="p-5">
          <div className="flex items-start justify-between gap-4">
            <div>
              <h1 className="text-xl font-bold">Pedido #{order.id}</h1>
              <p className="text-sm text-gray-500">
                {order.restaurantName} · {formatDateTime(order.createdAt)}
              </p>
            </div>
            <Badge className={statusColor[order.status]}>{statusLabel[order.status]}</Badge>
          </div>

          <div className="mt-6">
            <OrderTimeline order={order} />
          </div>

          {isCustomer && (
            <div className="mt-2 flex flex-wrap gap-2">
              {actionError && <div className="w-full"><ErrorMessage message={toApiError(actionError).message} /></div>}
              {order.status === 'PAYMENT_PENDING' && (
                <Button onClick={() => pay.mutate()} loading={pay.isPending}>
                  <RefreshCw size={16} /> Tentar pagar novamente
                </Button>
              )}
              {customerCanCancel(order.status) && (
                <Button variant="secondary" onClick={onCancel} loading={cancel.isPending}>
                  Cancelar pedido
                </Button>
              )}
            </div>
          )}
        </Card>

        {onTheWay && <TrackingCard orderId={order.id} />}
        {isCustomer && order.status === 'DELIVERED' && <ReviewCard order={order} />}
      </div>

      <div className="space-y-4">
        <Card className="p-5">
          <h2 className="font-semibold">Itens</h2>
          <ul className="mt-3 space-y-3 text-sm">
            {order.items.map((item, i) => (
              <li key={i} className="flex justify-between gap-3">
                <div>
                  <p>
                    <span className="font-semibold">{item.quantity}x</span> {item.name}
                  </p>
                  {item.options && <p className="text-gray-500">{item.options}</p>}
                  {item.notes && <p className="text-gray-500 italic">"{item.notes}"</p>}
                </div>
                <span>{formatCurrency(item.totalPrice)}</span>
              </li>
            ))}
          </ul>
          <dl className="mt-4 border-t border-gray-100 pt-3 space-y-1 text-sm">
            <div className="flex justify-between"><dt className="text-gray-600">Subtotal</dt><dd>{formatCurrency(order.subtotal)}</dd></div>
            <div className="flex justify-between"><dt className="text-gray-600">Entrega</dt><dd>{formatCurrency(order.deliveryFee)}</dd></div>
            {order.discount > 0 && (
              <div className="flex justify-between text-green-700">
                <dt>Desconto{order.couponCode ? ` (${order.couponCode})` : ''}</dt>
                <dd>- {formatCurrency(order.discount)}</dd>
              </div>
            )}
            <div className="flex justify-between font-bold text-base"><dt>Total</dt><dd>{formatCurrency(order.total)}</dd></div>
          </dl>
        </Card>

        <Card className="p-5 text-sm space-y-2">
          <p><span className="font-semibold">Pagamento:</span> {paymentLabel[order.paymentMethod]}</p>
          <p>
            <span className="font-semibold">Entrega:</span> {a.street}, {a.number}
            {a.complement ? ` - ${a.complement}` : ''}, {a.district}, {a.city}/{a.state}
          </p>
          {order.notes && <p><span className="font-semibold">Observações:</span> {order.notes}</p>}
        </Card>
      </div>
    </div>
  )
}
