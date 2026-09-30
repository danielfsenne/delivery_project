import type { Order, OrderStatus } from '@/core/api/types'
import { toApiError } from '@/core/api/client'
import { useChangeOrderStatus } from './api'
import { paymentLabel } from '@/features/orders/status'
import { formatCurrency, formatTime } from '@/shared/lib/format'
import { Button, Card, ErrorMessage } from '@/shared/components/ui'

interface Column {
  title: string
  statuses: OrderStatus[]
}

const columns: Column[] = [
  { title: 'Novos', statuses: ['PAID'] },
  { title: 'Em preparo', statuses: ['RESTAURANT_ACCEPTED', 'PREPARING'] },
  { title: 'Aguardando entregador', statuses: ['READY_FOR_PICKUP'] },
  { title: 'Saiu para entrega', statuses: ['OUT_FOR_DELIVERY'] },
]

/** Próxima ação do restaurante para cada status. */
const nextAction: Partial<Record<OrderStatus, { label: string; status: OrderStatus }>> = {
  PAID: { label: 'Aceitar', status: 'RESTAURANT_ACCEPTED' },
  RESTAURANT_ACCEPTED: { label: 'Iniciar preparo', status: 'PREPARING' },
  PREPARING: { label: 'Pronto para retirada', status: 'READY_FOR_PICKUP' },
}

export function OrdersBoard({ restaurantId, orders }: { restaurantId: number; orders: Order[] }) {
  const change = useChangeOrderStatus(restaurantId)

  const reject = (order: Order) => {
    const reason = window.prompt(`Motivo da recusa do pedido #${order.id}:`)
    if (reason) change.mutate({ orderId: order.id, status: 'CANCELLED', reason })
  }

  return (
    <div>
      {change.isError && <div className="mb-3"><ErrorMessage message={toApiError(change.error).message} /></div>}
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        {columns.map((column) => {
          const list = orders.filter((o) => column.statuses.includes(o.status))
          return (
            <section key={column.title} aria-label={column.title} className="rounded-xl bg-gray-100 p-3">
              <h2 className="mb-3 flex items-center justify-between text-sm font-semibold text-gray-700">
                {column.title}
                <span className="rounded-full bg-white px-2 text-xs">{list.length}</span>
              </h2>
              <div className="space-y-3">
                {list.length === 0 && <p className="py-6 text-center text-xs text-gray-400">Nenhum pedido</p>}
                {list.map((order) => {
                  const action = nextAction[order.status]
                  const busy = change.isPending && change.variables?.orderId === order.id
                  return (
                    <Card key={order.id} className="p-3 text-sm">
                      <div className="flex justify-between">
                        <span className="font-bold">#{order.id}</span>
                        <span className="text-gray-500">{formatTime(order.createdAt)}</span>
                      </div>
                      <ul className="mt-2 space-y-0.5">
                        {order.items.map((item, i) => (
                          <li key={i}>
                            <span className="font-semibold">{item.quantity}x</span> {item.name}
                            {item.options && <span className="text-gray-500"> ({item.options})</span>}
                            {item.notes && <span className="block text-xs italic text-gray-500">"{item.notes}"</span>}
                          </li>
                        ))}
                      </ul>
                      {order.notes && <p className="mt-2 rounded bg-amber-50 p-1.5 text-xs text-amber-800">{order.notes}</p>}
                      <p className="mt-2 text-gray-600">
                        {formatCurrency(order.total)} · {paymentLabel[order.paymentMethod]}
                      </p>
                      {order.status === 'PREPARING' && (
                        <p className="text-xs text-gray-500">Ao marcar como pronto, um entregador é chamado.</p>
                      )}
                      <div className="mt-3 flex gap-2">
                        {action && (
                          <Button
                            className="flex-1 py-1.5"
                            loading={busy}
                            onClick={() => change.mutate({ orderId: order.id, status: action.status })}
                          >
                            {action.label}
                          </Button>
                        )}
                        {(order.status === 'PAID' || order.status === 'RESTAURANT_ACCEPTED') && (
                          <Button variant="secondary" className="py-1.5" disabled={busy} onClick={() => reject(order)}>
                            Recusar
                          </Button>
                        )}
                      </div>
                    </Card>
                  )
                })}
              </div>
            </section>
          )
        })}
      </div>
    </div>
  )
}
