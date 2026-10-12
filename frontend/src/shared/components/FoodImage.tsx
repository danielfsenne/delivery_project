import { useState } from 'react'
import { cn } from '@/shared/lib/format'

/** Foto de restaurante ou produto; sem URL, ou se a imagem falhar, mostra a inicial do nome. */
export function FoodImage({ src, name, className }: { src: string | null | undefined; name: string; className?: string }) {
  const [failed, setFailed] = useState(false)

  if (!src || failed) {
    return (
      <span
        aria-hidden
        className={cn('flex items-center justify-center bg-brand-50 font-black text-brand-400', className)}
      >
        {name.trim().charAt(0).toUpperCase()}
      </span>
    )
  }
  return <img src={src} alt="" loading="lazy" onError={() => setFailed(true)} className={cn('object-cover', className)} />
}
