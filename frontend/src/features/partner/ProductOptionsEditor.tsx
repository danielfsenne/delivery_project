import { useState, type FormEvent } from 'react'
import { Plus, X } from 'lucide-react'
import type { Product } from '@/core/api/types'
import { toApiError } from '@/core/api/client'
import { useAddOption, useDeleteOption } from './api'
import { parsePrice } from './price'
import { formatCurrency } from '@/shared/lib/format'
import { Button } from '@/shared/components/ui'

/** Adicionais do produto (ex.: "Bacon extra"), escolhidos pelo cliente ao montar o item. */
export function ProductOptionsEditor({ restaurantId, product }: { restaurantId: number; product: Product }) {
  const add = useAddOption(restaurantId)
  const remove = useDeleteOption(restaurantId)
  const [name, setName] = useState('')
  const [price, setPrice] = useState('')

  const submit = (e: FormEvent) => {
    e.preventDefault()
    const value = parsePrice(price || '0')
    if (!name.trim() || value === null) return
    add.mutate(
      { productId: product.id, name: name.trim(), price: value },
      {
        onSuccess: () => {
          setName('')
          setPrice('')
        },
      },
    )
  }

  const error = add.error ?? remove.error

  return (
    <div className="w-full rounded-lg bg-gray-50 p-3">
      <p className="mb-2 text-xs font-semibold uppercase text-gray-500">Adicionais</p>
      {product.options.length > 0 ? (
        <ul className="mb-2 flex flex-wrap gap-2">
          {product.options.map((option) => (
            <li key={option.id} className="flex items-center gap-1 rounded-full bg-white px-3 py-1 text-sm shadow-sm">
              {option.name}
              <span className="text-gray-500">+{formatCurrency(option.price)}</span>
              <button
                onClick={() => remove.mutate({ productId: product.id, optionId: option.id })}
                disabled={remove.isPending}
                aria-label={`Remover ${option.name}`}
                className="ml-1 text-gray-400 hover:text-red-600"
              >
                <X size={14} />
              </button>
            </li>
          ))}
        </ul>
      ) : (
        <p className="mb-2 text-sm text-gray-500">Nenhum adicional cadastrado.</p>
      )}
      <form onSubmit={submit} className="flex flex-wrap gap-2">
        <input value={name} onChange={(e) => setName(e.target.value)} placeholder="Nome (ex.: Bacon extra)"
               aria-label="Nome do adicional" maxLength={80}
               className="min-w-40 flex-1 rounded border border-gray-300 px-2 py-1 text-sm" required />
        <input value={price} onChange={(e) => setPrice(e.target.value)} placeholder="Preço (0 = grátis)"
               aria-label="Preço do adicional" inputMode="decimal"
               className="w-32 rounded border border-gray-300 px-2 py-1 text-sm" />
        <Button type="submit" variant="secondary" className="px-2 py-1" loading={add.isPending}>
          <Plus size={14} /> Adicionar
        </Button>
      </form>
      {error && <p className="mt-2 text-xs text-red-600">{toApiError(error).message}</p>}
    </div>
  )
}
