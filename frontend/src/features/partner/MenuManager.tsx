import { useState, type FormEvent } from 'react'
import { ListPlus, Pencil, Plus, Trash2 } from 'lucide-react'
import type { Category, Product } from '@/core/api/types'
import { toApiError } from '@/core/api/client'
import {
  useAddCategory,
  useAddProduct,
  useDeleteCategory,
  useDeleteProduct,
  useManagedRestaurant,
  useRenameCategory,
  useSetAvailability,
  useUpdateProduct,
} from './api'
import { parsePrice } from './price'
import { ProductOptionsEditor } from './ProductOptionsEditor'
import { cn, formatCurrency } from '@/shared/lib/format'
import { Button, Card, ErrorMessage, Spinner } from '@/shared/components/ui'

function ProductRow({ restaurantId, product }: { restaurantId: number; product: Product }) {
  const availability = useSetAvailability(restaurantId)
  const update = useUpdateProduct(restaurantId)
  const remove = useDeleteProduct(restaurantId)
  const [editing, setEditing] = useState(false)
  const [showOptions, setShowOptions] = useState(false)
  const [price, setPrice] = useState(product.price.toFixed(2).replace('.', ','))

  const savePrice = (e: FormEvent) => {
    e.preventDefault()
    const value = parsePrice(price)
    if (value === null) return
    update.mutate(
      { productId: product.id, name: product.name, description: product.description ?? undefined, price: value,
        imageUrl: product.imageUrl ?? undefined },
      { onSuccess: () => setEditing(false) },
    )
  }

  return (
    <li className={cn('flex flex-wrap items-center gap-3 py-3', !product.available && 'opacity-60')}>
      <div className="flex-1 min-w-40">
        <p className="font-medium">{product.name}</p>
        {product.description && <p className="text-xs text-gray-500 line-clamp-1">{product.description}</p>}
      </div>

      {editing ? (
        <form onSubmit={savePrice} className="flex items-center gap-1">
          <span className="text-sm text-gray-500">R$</span>
          <input
            value={price}
            onChange={(e) => setPrice(e.target.value)}
            inputMode="decimal"
            aria-label={`Preço de ${product.name}`}
            className="w-20 rounded border border-gray-300 px-2 py-1 text-sm"
            autoFocus
          />
          <Button type="submit" className="px-2 py-1" loading={update.isPending}>Salvar</Button>
          <Button type="button" variant="ghost" className="px-2 py-1" onClick={() => setEditing(false)}>Cancelar</Button>
        </form>
      ) : (
        <button onClick={() => setEditing(true)} className="text-sm font-semibold hover:text-brand-600" title="Alterar preço">
          {formatCurrency(product.price)}
        </button>
      )}

      <label className="flex items-center gap-2 text-sm">
        <input
          type="checkbox"
          role="switch"
          checked={product.available}
          disabled={availability.isPending}
          onChange={(e) => availability.mutate({ productId: product.id, available: e.target.checked })}
          className="accent-brand-500 h-4 w-4"
        />
        {product.available ? 'Disponível' : 'Esgotado'}
      </label>

      <button
        onClick={() => setShowOptions((v) => !v)}
        aria-expanded={showOptions}
        className="flex items-center gap-1 text-sm text-gray-500 hover:text-brand-600"
        title="Adicionais"
      >
        <ListPlus size={16} />
        {product.options.length > 0 && product.options.length}
      </button>
      <button
        onClick={() => {
          if (window.confirm(`Excluir "${product.name}" do cardápio?`)) remove.mutate(product.id)
        }}
        disabled={remove.isPending}
        aria-label={`Excluir ${product.name}`}
        className="text-gray-400 hover:text-red-600"
      >
        <Trash2 size={16} />
      </button>

      {(update.isError || availability.isError || remove.isError) && (
        <p className="w-full text-xs text-red-600">
          {toApiError(update.error ?? availability.error ?? remove.error).message}
        </p>
      )}
      {showOptions && <ProductOptionsEditor restaurantId={restaurantId} product={product} />}
    </li>
  )
}

function AddProductForm({ restaurantId, category }: { restaurantId: number; category: Category }) {
  const add = useAddProduct(restaurantId)
  const [open, setOpen] = useState(false)
  const [name, setName] = useState('')
  const [price, setPrice] = useState('')
  const [description, setDescription] = useState('')

  if (!open) {
    return (
      <button onClick={() => setOpen(true)} className="mt-2 flex items-center gap-1 text-sm font-medium text-brand-600">
        <Plus size={16} /> Adicionar produto em {category.name}
      </button>
    )
  }

  const submit = (e: FormEvent) => {
    e.preventDefault()
    const value = parsePrice(price)
    if (!name.trim() || value === null) return
    add.mutate(
      { categoryId: category.id, name: name.trim(), price: value, description: description.trim() || undefined },
      {
        onSuccess: () => {
          setName('')
          setPrice('')
          setDescription('')
          setOpen(false)
        },
      },
    )
  }

  return (
    <form onSubmit={submit} className="mt-3 grid gap-2 rounded-lg bg-gray-50 p-3 sm:grid-cols-[1fr_120px]">
      <input value={name} onChange={(e) => setName(e.target.value)} placeholder="Nome do produto" aria-label="Nome do produto"
             className="rounded border border-gray-300 px-2 py-1.5 text-sm" required />
      <input value={price} onChange={(e) => setPrice(e.target.value)} placeholder="Preço (ex.: 29,90)" aria-label="Preço"
             inputMode="decimal" className="rounded border border-gray-300 px-2 py-1.5 text-sm" required />
      <input value={description} onChange={(e) => setDescription(e.target.value)} placeholder="Descrição (opcional)"
             aria-label="Descrição" className="rounded border border-gray-300 px-2 py-1.5 text-sm sm:col-span-2" />
      {add.isError && <div className="sm:col-span-2"><ErrorMessage message={toApiError(add.error).message} /></div>}
      <div className="flex gap-2 sm:col-span-2">
        <Button type="submit" loading={add.isPending}>Adicionar</Button>
        <Button type="button" variant="ghost" onClick={() => setOpen(false)}>Cancelar</Button>
      </div>
    </form>
  )
}

function CategoryHeader({ restaurantId, category }: { restaurantId: number; category: Category }) {
  const rename = useRenameCategory(restaurantId)
  const remove = useDeleteCategory(restaurantId)
  const [editing, setEditing] = useState(false)
  const [name, setName] = useState(category.name)

  const save = (e: FormEvent) => {
    e.preventDefault()
    if (!name.trim()) return
    rename.mutate(
      { categoryId: category.id, name: name.trim(), position: category.position },
      { onSuccess: () => setEditing(false) },
    )
  }

  const confirmDelete = () => {
    const count = category.products.length
    const warning = count > 0 ? ` Os ${count} produto(s) dela também serão excluídos.` : ''
    if (window.confirm(`Excluir a categoria "${category.name}"?${warning}`)) remove.mutate(category.id)
  }

  return (
    <div>
      {editing ? (
        <form onSubmit={save} className="flex items-center gap-2">
          <input value={name} onChange={(e) => setName(e.target.value)} aria-label="Nome da categoria" maxLength={80}
                 className="flex-1 rounded border border-gray-300 px-2 py-1 text-sm font-semibold" autoFocus />
          <Button type="submit" className="px-2 py-1" loading={rename.isPending}>Salvar</Button>
          <Button type="button" variant="ghost" className="px-2 py-1" onClick={() => {
            setName(category.name)
            setEditing(false)
          }}>Cancelar</Button>
        </form>
      ) : (
        <div className="flex items-center gap-2">
          <h3 className="flex-1 font-semibold">{category.name}</h3>
          <button onClick={() => setEditing(true)} aria-label={`Renomear ${category.name}`}
                  className="text-gray-400 hover:text-brand-600">
            <Pencil size={16} />
          </button>
          <button onClick={confirmDelete} disabled={remove.isPending} aria-label={`Excluir ${category.name}`}
                  className="text-gray-400 hover:text-red-600">
            <Trash2 size={16} />
          </button>
        </div>
      )}
      {(rename.isError || remove.isError) && (
        <p className="mt-1 text-xs text-red-600">{toApiError(rename.error ?? remove.error).message}</p>
      )}
    </div>
  )
}

export function MenuManager({ restaurantId }: { restaurantId: number }) {
  const { data: restaurant, isLoading, isError, error } = useManagedRestaurant(restaurantId)
  const addCategory = useAddCategory(restaurantId)
  const [categoryName, setCategoryName] = useState('')

  if (isLoading) return <Spinner />
  if (isError || !restaurant) return <ErrorMessage message={toApiError(error).message} />

  const submitCategory = (e: FormEvent) => {
    e.preventDefault()
    if (categoryName.trim()) addCategory.mutate(categoryName.trim(), { onSuccess: () => setCategoryName('') })
  }

  return (
    <div className="space-y-4">
      {restaurant.categories.map((category) => (
        <Card key={category.id} className="p-4">
          <CategoryHeader restaurantId={restaurantId} category={category} />
          <ul className="divide-y divide-gray-100">
            {category.products.map((product) => (
              <ProductRow key={product.id} restaurantId={restaurantId} product={product} />
            ))}
          </ul>
          <AddProductForm restaurantId={restaurantId} category={category} />
        </Card>
      ))}
      <form onSubmit={submitCategory} className="flex gap-2">
        <input value={categoryName} onChange={(e) => setCategoryName(e.target.value)} placeholder="Nova categoria"
               aria-label="Nova categoria" className="flex-1 rounded-lg border border-gray-300 px-3 py-2 text-sm" />
        <Button type="submit" variant="secondary" loading={addCategory.isPending}>Criar categoria</Button>
      </form>
    </div>
  )
}
