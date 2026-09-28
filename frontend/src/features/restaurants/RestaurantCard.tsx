import { Link } from 'react-router-dom'
import { Clock, Bike } from 'lucide-react'
import type { RestaurantSummary } from '@/core/api/types'
import { formatCurrency } from '@/shared/lib/format'
import { Badge } from '@/shared/components/ui'

export function RestaurantCard({ restaurant }: { restaurant: RestaurantSummary }) {
  const r = restaurant
  return (
    <Link
      to={`/restaurants/${r.id}`}
      className="group block rounded-xl bg-white border border-gray-200 overflow-hidden shadow-sm hover:shadow-md transition"
    >
      <div className="relative h-36 bg-gray-100">
        {r.imageUrl && (
          <img src={r.imageUrl} alt="" loading="lazy" className="h-full w-full object-cover group-hover:scale-105 transition" />
        )}
        {!r.open && (
          <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
            <Badge className="bg-white text-gray-900">Fechado</Badge>
          </div>
        )}
      </div>
      <div className="p-4">
        <h3 className="font-semibold text-lg leading-tight">{r.name}</h3>
        <p className="text-sm text-gray-500">
          {r.cuisine} · {r.district}
        </p>
        <div className="mt-3 flex items-center gap-4 text-sm text-gray-600">
          <span className="flex items-center gap-1">
            <Clock size={15} /> {r.deliveryTimeMin}-{r.deliveryTimeMax} min
          </span>
          <span className="flex items-center gap-1">
            <Bike size={15} />
            {r.deliveryFee === 0 ? (
              <span className="text-green-600 font-medium">Grátis</span>
            ) : (
              formatCurrency(r.deliveryFee)
            )}
          </span>
        </div>
      </div>
    </Link>
  )
}
