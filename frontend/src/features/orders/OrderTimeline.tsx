import { Check, Circle, X } from 'lucide-react'
import type { Order } from '@/core/api/types'
import { statusLabel, trackingSteps } from './status'
import { cn, formatTime } from '@/shared/lib/format'

/**
 * Linha do tempo do pedido. Etapas concluídas mostram o horário registrado no histórico.
 */
export function OrderTimeline({ order }: { order: Order }) {
  if (order.status === 'CANCELLED') {
    const cancelled = order.history.find((h) => h.newStatus === 'CANCELLED')
    return (
      <div className="flex items-start gap-3 rounded-lg bg-red-50 p-4 text-red-800">
        <X className="mt-0.5" size={18} />
        <div>
          <p className="font-semibold">Pedido cancelado</p>
          {cancelled?.reason && <p className="text-sm">Motivo: {cancelled.reason}</p>}
        </div>
      </div>
    )
  }

  const reachedAt = new Map(order.history.map((h) => [h.newStatus, h.createdAt]))
  const currentIndex = trackingSteps.indexOf(order.status)

  return (
    <ol className="space-y-0">
      {trackingSteps.map((step, index) => {
        const done = index < currentIndex || order.status === 'DELIVERED'
        const current = index === currentIndex && order.status !== 'DELIVERED'
        const time = reachedAt.get(step)
        return (
          <li key={step} className="flex gap-3">
            <div className="flex flex-col items-center">
              <span
                className={cn(
                  'flex h-6 w-6 items-center justify-center rounded-full',
                  done && 'bg-green-500 text-white',
                  current && 'bg-brand-500 text-white animate-pulse',
                  !done && !current && 'bg-gray-200 text-gray-400',
                )}
              >
                {done ? <Check size={14} /> : <Circle size={8} fill="currentColor" />}
              </span>
              {index < trackingSteps.length - 1 && (
                <span className={cn('w-0.5 flex-1 min-h-6', done ? 'bg-green-500' : 'bg-gray-200')} />
              )}
            </div>
            <div className="pb-4">
              <p className={cn('text-sm', (done || current) ? 'font-semibold text-gray-900' : 'text-gray-400')}>
                {statusLabel[step]}
              </p>
              {time && <p className="text-xs text-gray-500">{formatTime(time)}</p>}
            </div>
          </li>
        )
      })}
    </ol>
  )
}
