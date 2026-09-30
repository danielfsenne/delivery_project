import { useState } from 'react'
import { useMyRestaurants, useRestaurantOrders, useRestaurantStats } from './api'
import { StatsCards } from './StatsCards'
import { OrdersBoard } from './OrdersBoard'
import { MenuManager } from './MenuManager'
import { toApiError } from '@/core/api/client'
import { cn } from '@/shared/lib/format'
import { EmptyState, ErrorMessage, Spinner } from '@/shared/components/ui'

type Tab = 'orders' | 'menu'

export function PartnerPage() {
  const { data: restaurants, isLoading, isError, error } = useMyRestaurants()
  const [selectedId, setSelectedId] = useState<number>()
  const [tab, setTab] = useState<Tab>('orders')

  const restaurantId = selectedId ?? restaurants?.[0]?.id
  const { data: stats } = useRestaurantStats(restaurantId)
  const { data: orders, isLoading: loadingOrders } = useRestaurantOrders(restaurantId)

  if (isLoading) return <Spinner />
  if (isError) return <ErrorMessage message={toApiError(error).message} />
  if (!restaurants?.length || !restaurantId) {
    return <EmptyState title="Você ainda não tem restaurantes cadastrados" />
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-2xl font-bold">Painel do restaurante</h1>
        {restaurants.length > 1 && (
          <select
            value={restaurantId}
            onChange={(e) => setSelectedId(Number(e.target.value))}
            aria-label="Restaurante"
            className="rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm"
          >
            {restaurants.map((r) => (
              <option key={r.id} value={r.id}>{r.name}</option>
            ))}
          </select>
        )}
      </div>

      <StatsCards stats={stats} />

      <div role="tablist" className="flex gap-1 border-b border-gray-200">
        {([['orders', 'Pedidos'], ['menu', 'Cardápio']] as const).map(([value, label]) => (
          <button
            key={value}
            role="tab"
            aria-selected={tab === value}
            onClick={() => setTab(value)}
            className={cn(
              '-mb-px border-b-2 px-4 py-2 text-sm font-semibold',
              tab === value ? 'border-brand-500 text-brand-600' : 'border-transparent text-gray-500 hover:text-gray-800',
            )}
          >
            {label}
          </button>
        ))}
      </div>

      {tab === 'orders' && (loadingOrders ? <Spinner /> : <OrdersBoard restaurantId={restaurantId} orders={orders ?? []} />)}
      {tab === 'menu' && <MenuManager key={restaurantId} restaurantId={restaurantId} />}
    </div>
  )
}
