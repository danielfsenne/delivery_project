import { useState, type FormEvent } from 'react'
import type { Product } from '@/core/api/types'
import type { ProductInput } from './api'
import { parsePrice } from './price'
import { Button, ErrorMessage } from '@/shared/components/ui'

interface ProductFormProps {
  product?: Product
  submitLabel: string
  pending: boolean
  error?: string
  onSubmit: (input: ProductInput) => void
  onCancel: () => void
}

const inputClass = 'rounded border border-gray-300 px-2 py-1.5 text-sm'

/** Formulário de produto usado tanto para adicionar quanto para editar um item do cardápio. */
export function ProductForm({ product, submitLabel, pending, error, onSubmit, onCancel }: ProductFormProps) {
  const [name, setName] = useState(product?.name ?? '')
  const [price, setPrice] = useState(product ? product.price.toFixed(2).replace('.', ',') : '')
  const [description, setDescription] = useState(product?.description ?? '')
  const [imageUrl, setImageUrl] = useState(product?.imageUrl ?? '')
  const [priceError, setPriceError] = useState(false)

  const submit = (e: FormEvent) => {
    e.preventDefault()
    const value = parsePrice(price)
    setPriceError(value === null)
    if (!name.trim() || value === null) return
    onSubmit({
      name: name.trim(),
      price: value,
      description: description.trim() || undefined,
      imageUrl: imageUrl.trim() || undefined,
    })
  }

  return (
    <form onSubmit={submit} className="mt-3 grid w-full gap-2 rounded-lg bg-gray-50 p-3 sm:grid-cols-[1fr_140px]">
      <input value={name} onChange={(e) => setName(e.target.value)} placeholder="Nome do produto"
             aria-label="Nome do produto" maxLength={120} className={inputClass} required />
      <div>
        <input value={price} onChange={(e) => setPrice(e.target.value)} placeholder="Preço (ex.: 29,90)"
               aria-label="Preço" aria-invalid={priceError} inputMode="decimal" className={`${inputClass} w-full`}
               required />
        {priceError && <p className="mt-1 text-xs text-red-600">Preço inválido</p>}
      </div>
      <textarea value={description} onChange={(e) => setDescription(e.target.value)} placeholder="Descrição (opcional)"
                aria-label="Descrição" maxLength={500} rows={2} className={`${inputClass} sm:col-span-2`} />
      <input value={imageUrl} onChange={(e) => setImageUrl(e.target.value)} placeholder="URL da imagem (opcional)"
             aria-label="URL da imagem" type="url" maxLength={500} className={`${inputClass} sm:col-span-2`} />
      {error && <div className="sm:col-span-2"><ErrorMessage message={error} /></div>}
      <div className="flex gap-2 sm:col-span-2">
        <Button type="submit" loading={pending}>{submitLabel}</Button>
        <Button type="button" variant="ghost" onClick={onCancel}>Cancelar</Button>
      </div>
    </form>
  )
}
