import { Star } from 'lucide-react'
import { cn } from '@/shared/lib/format'

/** Média de avaliações ("Novo" quando ainda não há nenhuma). */
export function Rating({ average, count, className }: { average: number | null; count: number; className?: string }) {
  if (average === null || count === 0) {
    return <span className={cn('text-xs font-semibold text-brand-600', className)}>Novo</span>
  }
  return (
    <span className={cn('inline-flex items-center gap-1 text-sm', className)} title={`${count} avaliações`}>
      <Star size={14} className="fill-amber-400 text-amber-400" aria-hidden />
      <span className="font-semibold text-amber-600">{average.toFixed(1)}</span>
      <span className="text-gray-400">({count})</span>
    </span>
  )
}

/** Seletor de 1 a 5 estrelas acessível por teclado. */
export function StarInput({ value, onChange, label }: { value: number; onChange: (v: number) => void; label: string }) {
  return (
    <fieldset>
      <legend className="text-sm font-medium text-gray-700">{label}</legend>
      <div className="mt-1 flex gap-1" role="radiogroup" aria-label={label}>
        {[1, 2, 3, 4, 5].map((n) => (
          <button
            key={n}
            type="button"
            role="radio"
            aria-checked={value === n}
            aria-label={`${n} ${n === 1 ? 'estrela' : 'estrelas'}`}
            onClick={() => onChange(n)}
            className="p-0.5"
          >
            <Star size={28} className={n <= value ? 'fill-amber-400 text-amber-400' : 'text-gray-300'} />
          </button>
        ))}
      </div>
    </fieldset>
  )
}
