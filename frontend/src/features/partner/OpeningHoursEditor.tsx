import { Plus, X } from 'lucide-react'
import type { DayOfWeek } from '@/core/api/types'
import { days } from './restaurantForm'

interface TimeWindow {
  dayOfWeek: DayOfWeek
  opensAt: string
  closesAt: string
}

interface OpeningHoursEditorProps {
  value: TimeWindow[]
  onChange: (value: TimeWindow[]) => void
  error?: string
}

const timeInput = 'rounded border border-gray-300 px-2 py-1 text-sm tabular-nums'

/**
 * Uma linha por dia, com uma ou mais janelas (ex.: almoço e jantar). Dia sem janela fica fechado.
 * Fechamento antes da abertura atravessa a meia-noite.
 */
export function OpeningHoursEditor({ value, onChange, error }: OpeningHoursEditorProps) {
  const update = (index: number, patch: Partial<TimeWindow>) =>
    onChange(value.map((w, i) => (i === index ? { ...w, ...patch } : w)))
  const remove = (index: number) => onChange(value.filter((_, i) => i !== index))
  const add = (day: DayOfWeek) => {
    const last = value.filter((w) => w.dayOfWeek === day).pop()
    onChange([...value, last ? { ...last } : { dayOfWeek: day, opensAt: '11:00', closesAt: '22:00' }])
  }
  const copyToAll = (day: DayOfWeek) => {
    const windows = value.filter((w) => w.dayOfWeek === day)
    onChange(days.flatMap((d) => windows.map((w) => ({ ...w, dayOfWeek: d.value }))))
  }

  return (
    <div className="space-y-2">
      <ul className="divide-y divide-gray-100 rounded-lg border border-gray-200">
        {days.map((day) => {
          const windows = value.map((w, index) => ({ ...w, index })).filter((w) => w.dayOfWeek === day.value)
          return (
            <li key={day.value} className="flex flex-wrap items-center gap-2 px-3 py-2">
              <span className="w-20 text-sm font-medium">{day.label}</span>
              <div className="flex flex-1 flex-wrap items-center gap-2">
                {windows.length === 0 && <span className="text-sm text-gray-400">Fechado</span>}
                {windows.map((w) => (
                  <span key={w.index} className="inline-flex items-center gap-1 rounded-lg bg-gray-50 px-2 py-1">
                    <input
                      type="time"
                      value={w.opensAt}
                      onChange={(e) => update(w.index, { opensAt: e.target.value })}
                      aria-label={`${day.label}: abre às`}
                      className={timeInput}
                    />
                    <span className="text-xs text-gray-500">às</span>
                    <input
                      type="time"
                      value={w.closesAt}
                      onChange={(e) => update(w.index, { closesAt: e.target.value })}
                      aria-label={`${day.label}: fecha às`}
                      className={timeInput}
                    />
                    {w.closesAt < w.opensAt && <span className="text-xs text-gray-500">(dia seguinte)</span>}
                    <button
                      type="button"
                      onClick={() => remove(w.index)}
                      aria-label={`Remover horário de ${day.label}`}
                      className="rounded p-0.5 text-gray-400 hover:bg-gray-200 hover:text-gray-700"
                    >
                      <X size={14} />
                    </button>
                  </span>
                ))}
              </div>
              <button
                type="button"
                onClick={() => add(day.value)}
                className="inline-flex items-center gap-1 text-xs font-semibold text-brand-600 hover:text-brand-700"
              >
                <Plus size={14} /> Horário
              </button>
              {windows.length > 0 && (
                <button
                  type="button"
                  onClick={() => copyToAll(day.value)}
                  className="text-xs font-semibold text-gray-500 hover:text-gray-800"
                >
                  Copiar para todos
                </button>
              )}
            </li>
          )
        })}
      </ul>
      {error && <p className="text-xs text-red-600">{error}</p>}
    </div>
  )
}
