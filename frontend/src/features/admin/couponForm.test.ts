import { couponSchema, toCouponInput } from './couponForm'

const base = { code: 'bemvindo', type: 'FIXED' as const, value: '15', minOrderValue: '', maxDiscount: '', usageLimit: '' }

describe('couponForm', () => {
  it('converte o formulário no corpo da API, com campos vazios omitidos', () => {
    const parsed = couponSchema.parse({ ...base, minOrderValue: '40,50' })

    expect(toCouponInput(parsed)).toEqual({
      code: 'BEMVINDO',
      description: undefined,
      type: 'FIXED',
      value: 15,
      minOrderValue: 40.5,
      maxDiscount: undefined,
      usageLimit: undefined,
      validUntil: undefined,
    })
  })

  it('validade vai até o fim do dia escolhido', () => {
    const input = toCouponInput(couponSchema.parse({ ...base, validUntil: '2027-03-10' }))

    const end = new Date(input.validUntil!)
    expect([end.getFullYear(), end.getMonth(), end.getDate(), end.getHours(), end.getMinutes()])
      .toEqual([2027, 2, 10, 23, 59])
  })

  it('percentual acima de 100 é recusado', () => {
    const result = couponSchema.safeParse({ ...base, type: 'PERCENTAGE', value: '120' })

    expect(result.success).toBe(false)
    expect(result.error?.issues[0].path).toEqual(['value'])
  })

  it('exige valor, código válido e limite inteiro', () => {
    const result = couponSchema.safeParse({ ...base, code: 'x', value: '', usageLimit: '2.5' })

    expect(result.success).toBe(false)
    expect(result.error?.issues.map((i) => i.path[0]).sort()).toEqual(['code', 'usageLimit', 'value'])
  })
})
