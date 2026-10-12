import { Link } from 'react-router-dom'
import { Star } from 'lucide-react'
import type { RestaurantSummary } from '@/core/api/types'
import { cn, formatCurrency } from '@/shared/lib/format'
import { FoodImage } from '@/shared/components/FoodImage'

const Dot = () => <span aria-hidden className="text-gray-300">•</span>

export function RestaurantCard({ restaurant: r }: { restaurant: RestaurantSummary }) {
  return (
    <Link
      to={`/restaurants/${r.id}`}
      className={cn(
        'group flex items-center gap-4 rounded-2xl p-3 transition hover:bg-gray-50',
        !r.open && 'opacity-60 grayscale',
      )}
    >
      <FoodImage
        src={r.imageUrl}
        name={r.name}
        className="h-20 w-20 shrink-0 rounded-2xl border border-gray-100 text-3xl transition group-hover:scale-[1.03]"
      />
      <div className="min-w-0 flex-1">
        <h3 className="truncate font-bold text-gray-900">{r.name}</h3>
        <p className="mt-0.5 flex flex-wrap items-center gap-x-1.5 text-sm text-gray-500">
          {r.ratingAverage !== null && r.ratingCount > 0 ? (
            <span className="flex items-center gap-0.5 font-bold text-amber-500" title={`${r.ratingCount} avaliações`}>
              <Star size={13} className="fill-amber-400 text-amber-400" aria-hidden /> {r.ratingAverage.toFixed(1)}
            </span>
          ) : (
            <span className="font-bold text-brand-500">Novo</span>
          )}
          <Dot />
          <span>{r.cuisine}</span>
          <Dot />
          <span className="truncate">{r.district}</span>
        </p>
        <p className="mt-0.5 flex items-center gap-1.5 text-sm text-gray-500">
          {r.open ? `${r.deliveryTimeMin}-${r.deliveryTimeMax} min` : <span className="font-semibold">Fechado agora</span>}
          <Dot />
          {r.deliveryFee === 0 ? (
            <span className="font-bold text-emerald-600">Entrega grátis</span>
          ) : (
            formatCurrency(r.deliveryFee)
          )}
        </p>
      </div>
    </Link>
  )
}
