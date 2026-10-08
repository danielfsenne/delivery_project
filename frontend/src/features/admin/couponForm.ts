import { z } from 'zod'
import type { CouponInput } from './api'

/** Texto do campo numérico: vazio vira undefined; aceita vírgula como separador decimal. */
function toNumber(value: string | undefined): number | undefined {
  const text = value?.trim() ?? ''
  return text === '' ? undefined : Number(text.replace(',', '.'))
}

const optionalNumber = z
  .string()
  .optional()
  .refine((v) => {
    const n = toNumber(v)
    return n === undefined || (Number.isFinite(n) && n >= 0)
  }, 'Informe um número válido')

/** O formulário trabalha só com texto; a conversão para números fica em {@link toCouponInput}. */
export const couponSchema = z
  .object({
    code: z.string().trim().regex(/^[A-Za-z0-9_-]{3,30}$/, 'Use de 3 a 30 letras, números, - ou _'),
    description: z.string().trim().max(200).optional(),
    type: z.enum(['PERCENTAGE', 'FIXED']),
    value: optionalNumber.refine((v) => (toNumber(v) ?? 0) > 0, 'Informe o valor do desconto'),
    minOrderValue: optionalNumber,
    maxDiscount: optionalNumber,
    usageLimit: optionalNumber.refine((v) => {
      const n = toNumber(v)
      return n === undefined || Number.isInteger(n)
    }, 'Use um número inteiro'),
    validUntil: z.string().optional(),
  })
  .refine((d) => d.type !== 'PERCENTAGE' || (toNumber(d.value) ?? 0) <= 100, {
    path: ['value'],
    message: 'Percentual vai até 100',
  })

export type CouponForm = z.infer<typeof couponSchema>

export function toCouponInput(d: CouponForm): CouponInput {
  return {
    code: d.code.toUpperCase(),
    description: d.description || undefined,
    type: d.type,
    value: toNumber(d.value)!,
    minOrderValue: toNumber(d.minOrderValue),
    maxDiscount: toNumber(d.maxDiscount),
    usageLimit: toNumber(d.usageLimit),
    // Vale até o fim do dia escolhido, no fuso de quem cadastrou.
    validUntil: d.validUntil ? new Date(`${d.validUntil}T23:59:59`).toISOString() : undefined,
  }
}
