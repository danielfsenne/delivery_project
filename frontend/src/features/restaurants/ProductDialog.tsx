import { useEffect, useRef, useState } from 'react'
import { Minus, Plus, X } from 'lucide-react'
import type { Product } from '@/core/api/types'
import { formatCurrency } from '@/shared/lib/format'
import { Button, ErrorMessage } from '@/shared/components/ui'

export interface ProductSelection {
  quantity: number
  optionIds: number[]
  notes: string
}

interface Props {
  product: Product
  onClose: () => void
  onConfirm: (selection: ProductSelection) => void
  loading: boolean
  error?: string
}

export function ProductDialog({ product, onClose, onConfirm, loading, error }: Props) {
  const dialogRef = useRef<HTMLDialogElement>(null)
  const [quantity, setQuantity] = useState(1)
  const [optionIds, setOptionIds] = useState<number[]>([])
  const [notes, setNotes] = useState('')

  useEffect(() => {
    dialogRef.current?.showModal()
  }, [])

  const optionsTotal = product.options
    .filter((o) => optionIds.includes(o.id))
    .reduce((sum, o) => sum + o.price, 0)
  const total = (product.price + optionsTotal) * quantity

  const toggleOption = (id: number) =>
    setOptionIds((ids) => (ids.includes(id) ? ids.filter((x) => x !== id) : [...ids, id]))

  return (
    <dialog
      ref={dialogRef}
      onClose={onClose}
      className="w-full max-w-lg rounded-2xl p-0 backdrop:bg-black/50"
      aria-labelledby="product-title"
    >
      <div className="relative">
        {product.imageUrl && <img src={product.imageUrl} alt="" className="h-48 w-full object-cover" />}
        <button
          onClick={() => dialogRef.current?.close()}
          className="absolute top-3 right-3 rounded-full bg-white/90 p-1.5 shadow"
          aria-label="Fechar"
        >
          <X size={18} />
        </button>
      </div>

      <div className="p-5 space-y-4">
        <div>
          <h2 id="product-title" className="text-xl font-bold">{product.name}</h2>
          {product.description && <p className="text-sm text-gray-600 mt-1">{product.description}</p>}
          <p className="mt-2 font-semibold text-brand-600">{formatCurrency(product.price)}</p>
        </div>

        {product.options.length > 0 && (
          <fieldset>
            <legend className="text-sm font-semibold text-gray-800 mb-2">Adicionais</legend>
            <div className="divide-y divide-gray-100 rounded-lg border border-gray-200">
              {product.options.map((option) => (
                <label key={option.id} className="flex items-center justify-between px-3 py-2.5 cursor-pointer">
                  <span className="flex items-center gap-2 text-sm">
                    <input
                      type="checkbox"
                      checked={optionIds.includes(option.id)}
                      onChange={() => toggleOption(option.id)}
                      className="accent-brand-500 h-4 w-4"
                    />
                    {option.name}
                  </span>
                  <span className="text-sm text-gray-600">+ {formatCurrency(option.price)}</span>
                </label>
              ))}
            </div>
          </fieldset>
        )}

        <div>
          <label htmlFor="notes" className="text-sm font-semibold text-gray-800">
            Alguma observação?
          </label>
          <textarea
            id="notes"
            value={notes}
            maxLength={200}
            onChange={(e) => setNotes(e.target.value)}
            placeholder="Ex.: tirar a cebola, ponto da carne..."
            className="mt-1 w-full rounded-lg border border-gray-300 p-2 text-sm outline-none focus:border-brand-500"
            rows={2}
          />
        </div>

        {error && <ErrorMessage message={error} />}

        <div className="flex items-center gap-3">
          <div className="flex items-center rounded-lg border border-gray-300">
            <button
              className="p-2.5 disabled:opacity-40"
              onClick={() => setQuantity((q) => q - 1)}
              disabled={quantity <= 1}
              aria-label="Diminuir quantidade"
            >
              <Minus size={16} />
            </button>
            <span className="w-8 text-center font-semibold" aria-live="polite">{quantity}</span>
            <button
              className="p-2.5 disabled:opacity-40"
              onClick={() => setQuantity((q) => q + 1)}
              disabled={quantity >= 50}
              aria-label="Aumentar quantidade"
            >
              <Plus size={16} />
            </button>
          </div>
          <Button
            className="flex-1"
            loading={loading}
            onClick={() => onConfirm({ quantity, optionIds, notes })}
          >
            Adicionar · {formatCurrency(total)}
          </Button>
        </div>
      </div>
    </dialog>
  )
}
