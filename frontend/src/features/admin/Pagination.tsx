import { Button } from '@/shared/components/ui'

interface PaginationProps {
  page: number
  totalPages: number
  onChange: (page: number) => void
}

export function Pagination({ page, totalPages, onChange }: PaginationProps) {
  if (totalPages <= 1) return null
  return (
    <div className="flex items-center justify-end gap-2 p-3 text-sm">
      <Button variant="secondary" className="px-3 py-1.5" disabled={page === 0} onClick={() => onChange(page - 1)}>
        Anterior
      </Button>
      <span className="tabular-nums text-gray-600">
        {page + 1} de {totalPages}
      </span>
      <Button
        variant="secondary"
        className="px-3 py-1.5"
        disabled={page + 1 >= totalPages}
        onClick={() => onChange(page + 1)}
      >
        Próxima
      </Button>
    </div>
  )
}
