import { useEffect, useMemo, useState, type ReactNode } from 'react'
import { useSearchParams } from 'react-router-dom'
import { ChevronDown, Clock, X } from 'lucide-react'
import { useRestaurants } from './api'
import { RestaurantCard } from './RestaurantCard'
import { CuisineCarousel } from './CuisineCarousel'
import { PromoBanners } from './PromoBanners'
import { applyCatalogFilters, FAST_DELIVERY_MAX_MINUTES, sortLabels, type CatalogFilters, type SortOption } from './catalogFilters'
import { EmptyState, ErrorMessage, Spinner } from '@/shared/components/ui'
import { toApiError } from '@/core/api/client'
import { cn } from '@/shared/lib/format'

function useDebounced<T>(value: T, delay = 300): T {
  const [debounced, setDebounced] = useState(value)
  useEffect(() => {
    const id = setTimeout(() => setDebounced(value), delay)
    return () => clearTimeout(id)
  }, [value, delay])
  return debounced
}

function FilterChip({ active, onClick, children }: { active: boolean; onClick: () => void; children: ReactNode }) {
  return (
    <button
      onClick={onClick}
      aria-pressed={active}
      className={cn(
        'flex shrink-0 items-center gap-1 rounded-full border px-4 py-1.5 text-sm font-semibold transition',
        active ? 'border-brand-500 bg-brand-500 text-white' : 'border-gray-200 bg-white text-gray-700 hover:border-gray-300',
      )}
    >
      {children}
    </button>
  )
}

const noFilters: CatalogFilters = { freeDelivery: false, fast: false, sort: 'relevance' }

export function HomePage() {
  // Busca (vinda do cabeçalho) e culinária ficam na URL; os filtros rápidos são locais.
  const [params, setParams] = useSearchParams()
  const term = params.get('q') ?? ''
  const cuisine = params.get('culinaria')
  const q = useDebounced(term.trim())
  const [filters, setFilters] = useState<CatalogFilters>(noFilters)
  const { data, isLoading, isError, error } = useRestaurants({ q, cuisine: cuisine ?? undefined })

  const { open, closed } = useMemo(() => applyCatalogFilters(data?.content ?? [], filters), [data, filters])

  const selectCuisine = (value: string | null) => {
    const next = new URLSearchParams(params)
    if (value) next.set('culinaria', value)
    else next.delete('culinaria')
    setParams(next, { replace: true })
  }

  const clearAll = () => {
    setFilters(noFilters)
    setParams({}, { replace: true })
  }

  const searching = !!term || !!cuisine
  const hasFilters = searching || filters.freeDelivery || filters.fast || filters.sort !== 'relevance'
  const title = term ? `Resultados para "${term}"` : cuisine ?? 'Lojas'

  return (
    <div className="space-y-8">
      {!searching && <PromoBanners />}

      <CuisineCarousel selected={cuisine} onSelect={selectCuisine} />

      <section aria-labelledby="stores-title" className="space-y-4">
        <h2 id="stores-title" className="text-xl font-extrabold">{title}</h2>

        <div className="no-scrollbar -mx-4 flex items-center gap-2 overflow-x-auto px-4">
          <label
            className={cn(
              'relative flex shrink-0 items-center rounded-full border text-sm font-semibold transition',
              filters.sort !== 'relevance' ? 'border-brand-500 bg-brand-500 text-white' : 'border-gray-200 text-gray-700',
            )}
          >
            <span className="sr-only">Ordenar por</span>
            <select
              value={filters.sort}
              onChange={(e) => setFilters((f) => ({ ...f, sort: e.target.value as SortOption }))}
              className="cursor-pointer appearance-none bg-transparent py-1.5 pl-4 pr-8 outline-none"
            >
              {Object.entries(sortLabels).map(([value, label]) => (
                <option key={value} value={value} className="text-gray-900">{label}</option>
              ))}
            </select>
            <ChevronDown size={16} className="pointer-events-none absolute right-3" />
          </label>
          <FilterChip active={filters.freeDelivery} onClick={() => setFilters((f) => ({ ...f, freeDelivery: !f.freeDelivery }))}>
            Entrega grátis
          </FilterChip>
          <FilterChip active={filters.fast} onClick={() => setFilters((f) => ({ ...f, fast: !f.fast }))}>
            <Clock size={14} /> Até {FAST_DELIVERY_MAX_MINUTES} min
          </FilterChip>
          {hasFilters && (
            <button onClick={clearAll} className="flex shrink-0 items-center gap-1 px-2 text-sm font-semibold text-brand-500">
              <X size={14} /> Limpar
            </button>
          )}
        </div>

        {isLoading && <Spinner />}
        {isError && <ErrorMessage message={toApiError(error).message} />}
        {data && open.length + closed.length === 0 && (
          <EmptyState title="Nenhum restaurante encontrado" description="Tente outra busca ou remova os filtros." />
        )}
        {open.length > 0 && (
          <div className="grid gap-x-6 gap-y-1 sm:grid-cols-2 lg:grid-cols-3">
            {open.map((r) => <RestaurantCard key={r.id} restaurant={r} />)}
          </div>
        )}
      </section>

      {closed.length > 0 && (
        <section aria-labelledby="closed-title" className="space-y-4">
          <h2 id="closed-title" className="text-lg font-extrabold text-gray-500">Fechados agora</h2>
          <div className="grid gap-x-6 gap-y-1 sm:grid-cols-2 lg:grid-cols-3">
            {closed.map((r) => <RestaurantCard key={r.id} restaurant={r} />)}
          </div>
        </section>
      )}
    </div>
  )
}
