import { Link, useSearchParams } from 'react-router-dom'
import { Plus } from 'lucide-react'
import { useMyRestaurants, useRestaurantOrders, useRestaurantStats } from './api'
import { StatsCards } from './StatsCards'
import { OrdersBoard } from './OrdersBoard'
import { MenuManager } from './MenuManager'
import { RestaurantSettings } from './RestaurantSettings'
import { toApiError } from '@/core/api/client'
import { cn } from '@/shared/lib/format'
import { EmptyState, ErrorMessage, Spinner } from '@/shared/components/ui'

const tabs = [
  ['orders', 'Pedidos'],
  ['menu', 'Cardápio'],
  ['settings', 'Restaurante'],
] as const

type Tab = (typeof tabs)[number][0]

const newRestaurantLink = (
  <Link
    to="/partner/new"
    className="inline-flex items-center gap-1 rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-semibold text-white hover:bg-brand-600"
  >
    <Plus size={16} /> Cadastrar restaurante
  </Link>
)

export function PartnerPage() {
  const { data: restaurants, isLoading, isError, error } = useMyRestaurants()
  // Restaurante e aba ficam na URL: sobrevivem ao recarregar e permitem link direto (ex.: após o cadastro).
  const [params, setParams] = useSearchParams()
  const tab: Tab = tabs.some(([value]) => value === params.get('aba')) ? (params.get('aba') as Tab) : 'orders'
  const requestedId = Number(params.get('restaurante'))
  const restaurantId = restaurants?.some((r) => r.id === requestedId) ? requestedId : restaurants?.[0]?.id

  const select = (changes: { restaurante?: number; aba?: Tab }) => {
    const next = new URLSearchParams(params)
    if (changes.restaurante !== undefined) next.set('restaurante', String(changes.restaurante))
    if (changes.aba !== undefined) next.set('aba', changes.aba)
    setParams(next, { replace: true })
  }

  const { data: stats } = useRestaurantStats(restaurantId)
  const { data: orders, isLoading: loadingOrders } = useRestaurantOrders(restaurantId)

  if (isLoading) return <Spinner />
  if (isError) return <ErrorMessage message={toApiError(error).message} />
  if (!restaurants?.length || !restaurantId) {
    return (
      <EmptyState
        title="Você ainda não tem restaurantes cadastrados"
        description="Cadastre o seu para montar o cardápio e começar a receber pedidos."
        action={newRestaurantLink}
      />
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-2xl font-bold">Painel do restaurante</h1>
        <div className="flex items-center gap-2">
          {restaurants.length > 1 && (
            <select
              value={restaurantId}
              onChange={(e) => select({ restaurante: Number(e.target.value) })}
              aria-label="Restaurante"
              className="rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm"
            >
              {restaurants.map((r) => (
                <option key={r.id} value={r.id}>{r.name}</option>
              ))}
            </select>
          )}
          <Link
            to="/partner/new"
            className="inline-flex items-center gap-1 rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm font-semibold hover:bg-gray-50"
          >
            <Plus size={16} /> Novo restaurante
          </Link>
        </div>
      </div>

      <StatsCards stats={stats} />

      <div role="tablist" className="flex gap-1 border-b border-gray-200">
        {tabs.map(([value, label]) => (
          <button
            key={value}
            role="tab"
            aria-selected={tab === value}
            onClick={() => select({ aba: value })}
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
      {tab === 'settings' && <RestaurantSettings key={restaurantId} restaurantId={restaurantId} />}
    </div>
  )
}
