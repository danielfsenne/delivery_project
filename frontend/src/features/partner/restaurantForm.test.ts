import type { RestaurantDetail } from '@/core/api/types'
import { emptyRestaurantForm, fromDetail, restaurantSchema, toRestaurantRequest, type RestaurantForm } from './restaurantForm'

const valid: RestaurantForm = {
  ...emptyRestaurantForm,
  name: 'Cantina da Vó',
  cuisine: 'Italiana',
  deliveryFee: '7,90',
  address: {
    zipCode: '01310-100',
    street: 'Av. Paulista',
    number: '1000',
    complement: '',
    district: 'Bela Vista',
    city: 'São Paulo',
    state: 'sp',
  },
  location: { latitude: -23.561, longitude: -46.656 },
}

describe('restaurantForm', () => {
  it('converte o formulário no corpo da API', () => {
    const request = toRestaurantRequest(restaurantSchema.parse(valid))

    expect(request).toMatchObject({
      name: 'Cantina da Vó',
      deliveryFee: 7.9,
      minOrderValue: 0,
      deliveryTimeMin: 30,
      deliveryTimeMax: 45,
      description: undefined,
      imageUrl: undefined,
      address: { state: 'SP', complement: undefined, latitude: -23.561, longitude: -46.656 },
    })
    expect(request.openingHours).toHaveLength(7)
  })

  it('exige a posição no mapa e pelo menos um horário', () => {
    const result = restaurantSchema.safeParse({ ...valid, location: null, openingHours: [] })

    expect(result.success).toBe(false)
    expect(result.error?.issues.map((i) => i.path[0]).sort()).toEqual(['location', 'openingHours'])
  })

  it('tempo máximo não pode ser menor que o mínimo', () => {
    const result = restaurantSchema.safeParse({ ...valid, deliveryTimeMin: '50', deliveryTimeMax: '40' })

    expect(result.error?.issues[0].path).toEqual(['deliveryTimeMax'])
  })

  it('aceita expediente que vira a noite, mas não abertura igual ao fechamento', () => {
    const overnight = { dayOfWeek: 'FRIDAY' as const, opensAt: '18:00', closesAt: '02:00' }
    expect(restaurantSchema.safeParse({ ...valid, openingHours: [overnight] }).success).toBe(true)

    const empty = { dayOfWeek: 'FRIDAY' as const, opensAt: '18:00', closesAt: '18:00' }
    expect(restaurantSchema.safeParse({ ...valid, openingHours: [empty] }).success).toBe(false)
  })

  it('carrega um restaurante existente no formato do formulário', () => {
    const detail = {
      name: 'Forno da Nonna',
      cuisine: 'Pizza',
      description: null,
      phone: null,
      imageUrl: null,
      deliveryFee: 5,
      minOrderValue: 30,
      deliveryTimeMin: 40,
      deliveryTimeMax: 60,
      address: { ...valid.address, state: 'SP', complement: null, latitude: null, longitude: null },
      openingHours: [{ dayOfWeek: 'MONDAY', opensAt: '18:00:00', closesAt: '23:30:00' }],
    } as unknown as RestaurantDetail

    const form = fromDetail(detail)

    expect(form).toMatchObject({ deliveryFee: '5,00', minOrderValue: '30,00', deliveryTimeMin: '40', location: null })
    expect(form.openingHours).toEqual([{ dayOfWeek: 'MONDAY', opensAt: '18:00', closesAt: '23:30' }])
  })
})
