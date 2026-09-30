import { useState } from 'react'
import type { Order } from '@/core/api/types'
import { toApiError } from '@/core/api/client'
import { useCreateReview, useOrderReview } from './api'
import { Rating, StarInput } from '@/shared/components/Rating'
import { Button, Card, ErrorMessage } from '@/shared/components/ui'

/** Avaliação após a entrega: formulário se ainda não avaliou, resumo se já avaliou. */
export function ReviewCard({ order }: { order: Order }) {
  const { data: review, isLoading } = useOrderReview(order.id, true)
  const create = useCreateReview(order.id)
  const [food, setFood] = useState(0)
  const [delivery, setDelivery] = useState(0)
  const [comment, setComment] = useState('')

  if (isLoading) return null

  if (review) {
    return (
      <Card className="p-5">
        <h2 className="font-semibold">Sua avaliação</h2>
        <div className="mt-2 space-y-1 text-sm">
          <p className="flex items-center gap-2">Comida: <Rating average={review.foodRating} count={1} /></p>
          {review.deliveryRating && (
            <p className="flex items-center gap-2">Entrega: <Rating average={review.deliveryRating} count={1} /></p>
          )}
          {review.comment && <p className="italic text-gray-600">"{review.comment}"</p>}
        </div>
      </Card>
    )
  }

  const submit = () =>
    create.mutate({
      foodRating: food,
      deliveryRating: order.driverId && delivery ? delivery : undefined,
      comment: comment.trim() || undefined,
    })

  return (
    <Card className="p-5 space-y-4">
      <h2 className="font-semibold">Como foi seu pedido?</h2>
      <StarInput label="Comida" value={food} onChange={setFood} />
      {order.driverId && <StarInput label="Entrega" value={delivery} onChange={setDelivery} />}
      <div>
        <label htmlFor="review-comment" className="text-sm font-medium text-gray-700">Comentário (opcional)</label>
        <textarea
          id="review-comment"
          rows={2}
          maxLength={500}
          value={comment}
          onChange={(e) => setComment(e.target.value)}
          className="mt-1 w-full rounded-lg border border-gray-300 p-2 text-sm outline-none focus:border-brand-500"
        />
      </div>
      {create.isError && <ErrorMessage message={toApiError(create.error).message} />}
      <Button onClick={submit} disabled={food === 0} loading={create.isPending}>
        Enviar avaliação
      </Button>
    </Card>
  )
}
