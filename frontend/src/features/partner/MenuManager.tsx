import { useState, type FormEvent } from 'react'
import { ArrowDown, ArrowUp, ListPlus, Pencil, Plus, Trash2 } from 'lucide-react'
import type { Category, Product } from '@/core/api/types'
import { toApiError } from '@/core/api/client'
import {
  useAddCategory,
  useAddProduct,
  useDeleteCategory,
  useDeleteProduct,
  useManagedRestaurant,
  useRenameCategory,
  useReorderCategories,
  useSetAvailability,
  useUpdateProduct,
} from './api'
import { moveItem } from './menuOrder'
import { ProductForm } from './ProductForm'
import { ProductOptionsEditor } from './ProductOptionsEditor'
import { cn, formatCurrency } from '@/shared/lib/format'
import { Button, Card, ErrorMessage, Spinner } from '@/shared/components/ui'

function ProductRow({ restaurantId, product }: { restaurantId: number; product: Product }) {
  const availability = useSetAvailability(restaurantId)
  const update = useUpdateProduct(restaurantId)
  const remove = useDeleteProduct(restaurantId)
  const [editing, setEditing] = useState(false)
  const [showOptions, setShowOptions] = useState(false)

  if (editing) {
    return (
      <li className="py-3">
        <ProductForm
          product={product}
          submitLabel="Salvar"
          pending={update.isPending}
          error={update.isError ? toApiError(update.error).message : undefined}
          onSubmit={(input) => update.mutate({ productId: product.id, ...input }, { onSuccess: () => setEditing(false) })}
          onCancel={() => {
            update.reset()
            setEditing(false)
          }}
        />
      </li>
    )
  }

  return (
    <li className={cn('flex flex-wrap items-center gap-3 py-3', !product.available && 'opacity-60')}>
      {product.imageUrl && (
        <img src={product.imageUrl} alt="" className="h-12 w-12 shrink-0 rounded-lg object-cover" loading="lazy" />
      )}
      <div className="flex-1 min-w-40">
        <p className="font-medium">{product.name}</p>
        {product.description && <p className="text-xs text-gray-500 line-clamp-1">{product.description}</p>}
      </div>

      <span className="text-sm font-semibold">{formatCurrency(product.price)}</span>

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
      <button onClick={() => setEditing(true)} aria-label={`Editar ${product.name}`}
              className="text-gray-400 hover:text-brand-600">
        <Pencil size={16} />
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

      {(availability.isError || remove.isError) && (
        <p className="w-full text-xs text-red-600">{toApiError(availability.error ?? remove.error).message}</p>
      )}
      {showOptions && <ProductOptionsEditor restaurantId={restaurantId} product={product} />}
    </li>
  )
}

function AddProductForm({ restaurantId, category }: { restaurantId: number; category: Category }) {
  const add = useAddProduct(restaurantId)
  const [open, setOpen] = useState(false)

  if (!open) {
    return (
      <button onClick={() => setOpen(true)} className="mt-2 flex items-center gap-1 text-sm font-medium text-brand-600">
        <Plus size={16} /> Adicionar produto em {category.name}
      </button>
    )
  }

  return (
    <ProductForm
      submitLabel="Adicionar"
      pending={add.isPending}
      error={add.isError ? toApiError(add.error).message : undefined}
      onSubmit={(input) => add.mutate({ categoryId: category.id, ...input }, { onSuccess: () => setOpen(false) })}
      onCancel={() => {
        add.reset()
        setOpen(false)
      }}
    />
  )
}

interface CategoryHeaderProps {
  restaurantId: number
  category: Category
  /** Ausente quando a categoria já está na ponta ou há outra reordenação em andamento. */
  onMoveUp?: () => void
  onMoveDown?: () => void
}

function CategoryHeader({ restaurantId, category, onMoveUp, onMoveDown }: CategoryHeaderProps) {
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
          <button onClick={onMoveUp} disabled={!onMoveUp} aria-label={`Subir ${category.name}`}
                  className="text-gray-400 hover:text-brand-600 disabled:opacity-30 disabled:hover:text-gray-400">
            <ArrowUp size={16} />
          </button>
          <button onClick={onMoveDown} disabled={!onMoveDown} aria-label={`Descer ${category.name}`}
                  className="text-gray-400 hover:text-brand-600 disabled:opacity-30 disabled:hover:text-gray-400">
            <ArrowDown size={16} />
          </button>
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
  const reorder = useReorderCategories(restaurantId)
  const [categoryName, setCategoryName] = useState('')

  if (isLoading) return <Spinner />
  if (isError || !restaurant) return <ErrorMessage message={toApiError(error).message} />

  const submitCategory = (e: FormEvent) => {
    e.preventDefault()
    if (categoryName.trim()) addCategory.mutate(categoryName.trim(), { onSuccess: () => setCategoryName('') })
  }

  const ids = restaurant.categories.map((c) => c.id)
  const move = (index: number, direction: -1 | 1) => () => reorder.mutate(moveItem(ids, index, direction))
  const last = ids.length - 1

  return (
    <div className="space-y-4">
      {reorder.isError && <ErrorMessage message={toApiError(reorder.error).message} />}
      {restaurant.categories.map((category, index) => (
        <Card key={category.id} className="p-4">
          <CategoryHeader
            restaurantId={restaurantId}
            category={category}
            onMoveUp={index > 0 && !reorder.isPending ? move(index, -1) : undefined}
            onMoveDown={index < last && !reorder.isPending ? move(index, 1) : undefined}
          />
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
