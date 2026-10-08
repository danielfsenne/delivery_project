import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAdminOrders } from './api'
import { Pagination } from './Pagination'
import type { OrderStatus } from '@/core/api/types'
import { toApiError } from '@/core/api/client'
import { statusColor, statusLabel } from '@/features/orders/status'
import { formatCurrency, formatDateTime } from '@/shared/lib/format'
import { Badge, Card, ErrorMessage, Spinner } from '@/shared/components/ui'

const statuses = Object.keys(statusLabel) as OrderStatus[]

export function AdminOrders() {
  const [status, setStatus] = useState<OrderStatus | ''>('')
  const [page, setPage] = useState(0)
  const { data, isLoading, isError, error } = useAdminOrders(status, page)

  return (
    <Card>
      <div className="flex items-center justify-between gap-3 border-b border-gray-100 p-3">
        <h2 className="font-semibold">Todos os pedidos</h2>
        <select
          value={status}
          onChange={(e) => {
            setStatus(e.target.value as OrderStatus | '')
            setPage(0)
          }}
          aria-label="Filtrar por status"
          className="rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm"
        >
          <option value="">Todos os status</option>
          {statuses.map((s) => (
            <option key={s} value={s}>{statusLabel[s]}</option>
          ))}
        </select>
      </div>

      {isLoading && <Spinner />}
      {isError && <div className="p-3"><ErrorMessage message={toApiError(error).message} /></div>}
      {data && data.content.length === 0 && <p className="p-6 text-sm text-gray-500">Nenhum pedido encontrado.</p>}
      {data && data.content.length > 0 && (
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="text-left text-xs uppercase tracking-wide text-gray-500">
              <tr>
                <th className="px-4 py-2 font-medium">Pedido</th>
                <th className="px-4 py-2 font-medium">Restaurante</th>
                <th className="px-4 py-2 font-medium">Data</th>
                <th className="px-4 py-2 font-medium">Status</th>
                <th className="px-4 py-2 text-right font-medium">Total</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {data.content.map((order) => (
                <tr key={order.id} className="hover:bg-gray-50">
                  <td className="px-4 py-2">
                    <Link to={`/orders/${order.id}`} className="font-semibold text-brand-600 hover:underline">
                      #{order.id}
                    </Link>
                  </td>
                  <td className="px-4 py-2">{order.restaurantName}</td>
                  <td className="px-4 py-2 whitespace-nowrap text-gray-600">{formatDateTime(order.createdAt)}</td>
                  <td className="px-4 py-2">
                    <Badge className={statusColor[order.status]}>{statusLabel[order.status]}</Badge>
                  </td>
                  <td className="px-4 py-2 text-right tabular-nums">{formatCurrency(order.total)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage} />}
    </Card>
  )
}
