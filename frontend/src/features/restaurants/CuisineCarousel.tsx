import { cn } from '@/shared/lib/format'
import { cuisines } from './cuisines'

interface CuisineCarouselProps {
  selected: string | null
  onSelect: (cuisine: string | null) => void
}

/** Fileira de culinárias com foto redonda; clicar de novo na selecionada limpa o filtro. */
export function CuisineCarousel({ selected, onSelect }: CuisineCarouselProps) {
  return (
    <div className="no-scrollbar -mx-4 flex gap-4 overflow-x-auto px-4 pb-1" role="group" aria-label="Culinárias">
      {cuisines.map(({ name, image }) => {
        const active = selected?.toLowerCase() === name.toLowerCase()
        return (
          <button
            key={name}
            onClick={() => onSelect(active ? null : name)}
            aria-pressed={active}
            className="group flex w-20 shrink-0 flex-col items-center gap-2"
          >
            <span
              className={cn(
                'h-20 w-20 overflow-hidden rounded-full bg-brand-50 ring-offset-2 transition',
                active ? 'ring-[3px] ring-brand-500' : 'group-hover:ring-2 group-hover:ring-brand-200',
              )}
            >
              <img src={image} alt="" loading="lazy" className="h-full w-full object-cover transition group-hover:scale-110" />
            </span>
            <span className={cn('text-xs font-semibold', active ? 'text-brand-600' : 'text-gray-700')}>{name}</span>
          </button>
        )
      })}
    </div>
  )
}
