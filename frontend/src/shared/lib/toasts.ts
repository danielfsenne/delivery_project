import { create } from 'zustand'

export interface Toast {
  id: number
  title: string
  description?: string
  href?: string
}

interface ToastState {
  toasts: Toast[]
  push: (toast: Omit<Toast, 'id'>) => void
  dismiss: (id: number) => void
}

let nextId = 1

/** Fila de avisos exibidos pelo {@link Toaster}; mantém no máximo os 4 mais recentes. */
export const useToasts = create<ToastState>((set) => ({
  toasts: [],
  push: (toast) => set((s) => ({ toasts: [...s.toasts.slice(-3), { ...toast, id: nextId++ }] })),
  dismiss: (id) => set((s) => ({ toasts: s.toasts.filter((t) => t.id !== id) })),
}))
