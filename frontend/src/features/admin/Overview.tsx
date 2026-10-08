import { usePlatformStats } from './api'
import { toApiError } from '@/core/api/client'
import { formatCurrency } from '@/shared/lib/format'
import { Card, ErrorMessage } from '@/shared/components/ui'

export function Overview() {
  const { data: stats, isError, error } = usePlatformStats()

  if (isError) return <ErrorMessage message={toApiError(error).message} />

  const items = [
    { label: 'Pedidos hoje', value: stats ? String(stats.ordersToday) : '–' },
    { label: 'Faturamento', value: stats ? formatCurrency(stats.revenueToday) : '–' },
    { label: 'Ticket médio', value: stats ? formatCurrency(stats.averageTicket) : '–' },
    { label: 'Em andamento', value: stats ? String(stats.inProgress) : '–' },
    { label: 'Aguardando pagamento', value: stats ? String(stats.awaitingPayment) : '–' },
    { label: 'Cancelados hoje', value: stats ? String(stats.cancelledToday) : '–' },
  ]

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6">
        {items.map((item) => (
          <Card key={item.label} className="p-4">
            <p className="text-xs font-medium uppercase tracking-wide text-gray-500">{item.label}</p>
            <p className="mt-1 text-2xl font-bold tabular-nums">{item.value}</p>
          </Card>
        ))}
      </div>

      <Card>
        <h2 className="border-b border-gray-100 px-4 py-3 font-semibold">Restaurantes que mais venderam hoje</h2>
        {stats && stats.topRestaurants.length === 0 ? (
          <p className="px-4 py-6 text-sm text-gray-500">Nenhuma venda hoje ainda.</p>
        ) : (
          <table className="w-full text-sm">
            <thead className="text-left text-xs uppercase tracking-wide text-gray-500">
              <tr>
                <th className="px-4 py-2 font-medium">Restaurante</th>
                <th className="px-4 py-2 text-right font-medium">Pedidos</th>
                <th className="px-4 py-2 text-right font-medium">Faturamento</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {stats?.topRestaurants.map((r) => (
                <tr key={r.restaurantId}>
                  <td className="px-4 py-2">{r.restaurantName}</td>
                  <td className="px-4 py-2 text-right tabular-nums">{r.orders}</td>
                  <td className="px-4 py-2 text-right tabular-nums">{formatCurrency(r.revenue)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>
    </div>
  )
}
