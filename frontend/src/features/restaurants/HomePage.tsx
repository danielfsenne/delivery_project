import { useEffect, useState } from 'react'
import { Search } from 'lucide-react'
import { useRestaurants } from './api'
import { RestaurantCard } from './RestaurantCard'
import { EmptyState, ErrorMessage, Spinner } from '@/shared/components/ui'
import { toApiError } from '@/core/api/client'

function useDebounced<T>(value: T, delay = 300): T {
  const [debounced, setDebounced] = useState(value)
  useEffect(() => {
    const id = setTimeout(() => setDebounced(value), delay)
    return () => clearTimeout(id)
  }, [value, delay])
  return debounced
}

export function HomePage() {
  const [term, setTerm] = useState('')
  const q = useDebounced(term.trim())
  const { data, isLoading, isError, error } = useRestaurants({ q })

  return (
    <div>
      <section className="py-8 text-center">
        <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight">
          Sua comida favorita, <span className="text-brand-500">no caminho certo.</span>
        </h1>
        <p className="mt-2 text-gray-600">Escolha um restaurante e acompanhe seu pedido em tempo real.</p>

        <label className="mt-6 mx-auto max-w-xl flex items-center gap-2 rounded-full bg-white border border-gray-300 px-4 py-3 shadow-sm focus-within:ring-2 focus-within:ring-brand-500/30">
          <Search size={18} className="text-gray-400" />
          <span className="sr-only">Buscar restaurantes</span>
          <input
            value={term}
            onChange={(e) => setTerm(e.target.value)}
            placeholder="Busque por restaurante ou culinária"
            className="flex-1 outline-none bg-transparent text-sm"
          />
        </label>
      </section>

      {isLoading && <Spinner />}
      {isError && <ErrorMessage message={toApiError(error).message} />}
      {data && data.content.length === 0 && (
        <EmptyState title="Nenhum restaurante encontrado" description="Tente buscar por outro termo." />
      )}
      {data && data.content.length > 0 && (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {data.content.map((r) => (
            <RestaurantCard key={r.id} restaurant={r} />
          ))}
        </div>
      )}
    </div>
  )
}
