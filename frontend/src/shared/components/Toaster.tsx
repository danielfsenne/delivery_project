import { useEffect } from 'react'
import { Link } from 'react-router-dom'
import { X } from 'lucide-react'
import { useToasts, type Toast } from '@/shared/lib/toasts'

const DURATION_MS = 6_000

function ToastItem({ toast }: { toast: Toast }) {
  const dismiss = useToasts((s) => s.dismiss)
  useEffect(() => {
    const timer = setTimeout(() => dismiss(toast.id), DURATION_MS)
    return () => clearTimeout(timer)
  }, [toast.id, dismiss])

  const content = (
    <>
      <p className="font-semibold">{toast.title}</p>
      {toast.description && <p className="text-sm text-gray-600">{toast.description}</p>}
    </>
  )

  return (
    <div role="status" className="flex items-start gap-3 rounded-lg border border-gray-200 bg-white p-4 shadow-lg">
      <div className="flex-1">
        {toast.href ? (
          <Link to={toast.href} onClick={() => dismiss(toast.id)} className="block hover:underline">
            {content}
          </Link>
        ) : (
          content
        )}
      </div>
      <button aria-label="Fechar aviso" onClick={() => dismiss(toast.id)} className="text-gray-400 hover:text-gray-600">
        <X size={16} />
      </button>
    </div>
  )
}

/** Avisos no canto da tela, usados pelas notificações em tempo real. */
export function Toaster() {
  const toasts = useToasts((s) => s.toasts)
  return (
    <div className="fixed bottom-4 right-4 z-50 flex w-80 max-w-[calc(100vw-2rem)] flex-col gap-2">
      {toasts.map((t) => (
        <ToastItem key={t.id} toast={t} />
      ))}
    </div>
  )
}
