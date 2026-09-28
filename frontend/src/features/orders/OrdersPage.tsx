import { Link } from 'react-router-dom'
import { ChevronRight } from 'lucide-react'
import { useMyOrders } from './api'
import { statusColor, statusLabel } from './status'
import { toApiError } from '@/core/api/client'
import { formatCurrency, formatDateTime } from '@/shared/lib/format'
import { Badge, Card, EmptyState, ErrorMessage, Spinner } from '@/shared/components/ui'

export function OrdersPage() {
  const { data, isLoading, isError, error } = useMyOrders()

  if (isLoading) return <Spinner />
  if (isError || !data) return <ErrorMessage message={toApiError(error).message} />
  if (data.content.length === 0) {
    return <EmptyState title="Você ainda não fez pedidos" action={<Link to="/" className="text-brand-600 font-semibold">Ver restaurantes</Link>} />
  }

  return (
    <div className="max-w-2xl mx-auto">
      <h1 className="text-2xl font-bold mb-4">Meus pedidos</h1>
      <Card>
        <ul className="divide-y divide-gray-100">
          {data.content.map((order) => (
            <li key={order.id}>
              <Link to={`/orders/${order.id}`} className="flex items-center gap-4 p-4 hover:bg-gray-50">
                <div className="flex-1">
                  <p className="font-semibold">{order.restaurantName}</p>
                  <p className="text-sm text-gray-500">
                    Pedido #{order.id} · {formatDateTime(order.createdAt)} · {order.itemCount}{' '}
                    {order.itemCount === 1 ? 'item' : 'itens'}
                  </p>
                </div>
                <div className="text-right">
                  <Badge className={statusColor[order.status]}>{statusLabel[order.status]}</Badge>
                  <p className="mt-1 text-sm font-semibold">{formatCurrency(order.total)}</p>
                </div>
                <ChevronRight size={18} className="text-gray-400" />
              </Link>
            </li>
          ))}
        </ul>
      </Card>
    </div>
  )
}
