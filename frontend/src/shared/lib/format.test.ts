import { cn, formatCurrency } from './format'

describe('formatCurrency', () => {
  it('formata em reais', () => {
    expect(formatCurrency(1234.5).replace(/\s/g, ' ')).toBe('R$ 1.234,50')
  })
})

describe('cn', () => {
  it('ignora valores falsos', () => {
    expect(cn('a', false, null, 'b', undefined)).toBe('a b')
  })
})
