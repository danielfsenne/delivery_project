import type { RestaurantStats } from '@/core/api/types'
import { formatCurrency } from '@/shared/lib/format'
import { Card } from '@/shared/components/ui'

export function StatsCards({ stats }: { stats: RestaurantStats | undefined }) {
  const items = [
    { label: 'Pedidos hoje', value: stats ? String(stats.ordersToday) : '–' },
    { label: 'Faturamento', value: stats ? formatCurrency(stats.revenueToday) : '–' },
    { label: 'Ticket médio', value: stats ? formatCurrency(stats.averageTicket) : '–' },
    { label: 'Em andamento', value: stats ? String(stats.inProgress) : '–' },
    { label: 'Cancelados hoje', value: stats ? String(stats.cancelledToday) : '–' },
  ]
  return (
    <div className="grid grid-cols-2 gap-3 sm:grid-cols-5">
      {items.map((item) => (
        <Card key={item.label} className="p-4">
          <p className="text-xs font-medium uppercase tracking-wide text-gray-500">{item.label}</p>
          <p className="mt-1 text-2xl font-bold tabular-nums">{item.value}</p>
        </Card>
      ))}
    </div>
  )
}
