import { parsePrice } from './price'

describe('parsePrice', () => {
  it('aceita vírgula ou ponto como separador decimal', () => {
    expect(parsePrice('29,90')).toBe(29.9)
    expect(parsePrice(' 12.5 ')).toBe(12.5)
    expect(parsePrice('0')).toBe(0)
  })

  it('arredonda para centavos', () => {
    expect(parsePrice('10,999')).toBe(11)
  })

  it('rejeita vazio, negativo e texto', () => {
    expect(parsePrice('')).toBeNull()
    expect(parsePrice('-1')).toBeNull()
    expect(parsePrice('dez')).toBeNull()
  })
})
