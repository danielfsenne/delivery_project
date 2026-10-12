import { moveItem } from './menuOrder'

describe('moveItem', () => {
  it('troca o item com o vizinho de cima ou de baixo', () => {
    expect(moveItem([1, 2, 3], 1, -1)).toEqual([2, 1, 3])
    expect(moveItem([1, 2, 3], 1, 1)).toEqual([1, 3, 2])
  })

  it('não sai dos limites da lista', () => {
    expect(moveItem([1, 2, 3], 0, -1)).toEqual([1, 2, 3])
    expect(moveItem([1, 2, 3], 2, 1)).toEqual([1, 2, 3])
  })

  it('não altera a lista original', () => {
    const items = [1, 2, 3]
    moveItem(items, 0, 1)
    expect(items).toEqual([1, 2, 3])
  })
})
