import { z } from 'zod'
import type { DayOfWeek, OpeningHour, RestaurantDetail } from '@/core/api/types'

const dayValues = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'] as const

export const days: Array<{ value: DayOfWeek; label: string }> = [
  { value: 'MONDAY', label: 'Segunda' },
  { value: 'TUESDAY', label: 'Terça' },
  { value: 'WEDNESDAY', label: 'Quarta' },
  { value: 'THURSDAY', label: 'Quinta' },
  { value: 'FRIDAY', label: 'Sexta' },
  { value: 'SATURDAY', label: 'Sábado' },
  { value: 'SUNDAY', label: 'Domingo' },
]

/** Texto de valor em reais: aceita vírgula; vazio não é permitido. */
const money = z
  .string()
  .trim()
  .min(1, 'Informe um valor')
  .refine((v) => {
    const n = toNumber(v)
    return Number.isFinite(n) && n >= 0
  }, 'Informe um valor válido')

const minutes = z
  .string()
  .trim()
  .refine((v) => /^\d+$/.test(v) && Number(v) >= 1 && Number(v) <= 240, 'Entre 1 e 240 minutos')

const time = z.string().regex(/^\d{2}:\d{2}$/, 'Horário inválido')

export const restaurantSchema = z
  .object({
    name: z.string().trim().min(2, 'Informe o nome').max(120),
    cuisine: z.string().trim().min(2, 'Informe o tipo de cozinha').max(60),
    description: z.string().trim().max(500).optional(),
    phone: z.string().trim().max(20).optional(),
    imageUrl: z.union([z.literal(''), z.string().trim().url('Informe uma URL válida').max(500)]).optional(),
    deliveryFee: money,
    minOrderValue: money,
    deliveryTimeMin: minutes,
    deliveryTimeMax: minutes,
    address: z.object({
      zipCode: z.string().trim().regex(/^\d{5}-?\d{3}$/, 'CEP inválido'),
      street: z.string().trim().min(1, 'Informe a rua').max(160),
      number: z.string().trim().min(1, 'Informe o número').max(20),
      complement: z.string().trim().max(80).optional(),
      district: z.string().trim().min(1, 'Informe o bairro').max(80),
      city: z.string().trim().min(1, 'Informe a cidade').max(80),
      state: z
        .string()
        .trim()
        .transform((v) => v.toUpperCase())
        .pipe(z.string().regex(/^[A-Z]{2}$/, 'Use a sigla do estado, ex.: SP')),
    }),
    // A posição alimenta a distância das corridas; vem do mapa, não do endereço digitado.
    location: z
      .object({ latitude: z.number(), longitude: z.number() })
      .nullable()
      .refine((v) => v !== null, 'Marque no mapa onde fica o restaurante'),
    openingHours: z
      .array(z.object({ dayOfWeek: z.enum(dayValues), opensAt: time, closesAt: time }))
      .min(1, 'Informe pelo menos um horário de funcionamento')
      .refine((hours) => hours.every((h) => h.opensAt !== h.closesAt), 'Abertura e fechamento não podem ser iguais'),
  })
  .refine((d) => Number(d.deliveryTimeMin) <= Number(d.deliveryTimeMax), {
    path: ['deliveryTimeMax'],
    message: 'Deve ser maior ou igual ao tempo mínimo',
  })

export type RestaurantForm = z.input<typeof restaurantSchema>

function toNumber(value: string): number {
  return Number(value.replace(',', '.'))
}

function formatMoney(value: number): string {
  return value.toFixed(2).replace('.', ',')
}

/** O Java manda HH:mm:ss; o input de horário trabalha com HH:mm. */
function hhmm(value: string): string {
  return value.slice(0, 5)
}

export const emptyRestaurantForm: RestaurantForm = {
  name: '',
  cuisine: '',
  description: '',
  phone: '',
  imageUrl: '',
  deliveryFee: '',
  minOrderValue: '0,00',
  deliveryTimeMin: '30',
  deliveryTimeMax: '45',
  address: { zipCode: '', street: '', number: '', complement: '', district: '', city: '', state: '' },
  location: null,
  openingHours: days.map((d) => ({ dayOfWeek: d.value, opensAt: '11:00', closesAt: '22:00' })),
}

export function fromDetail(r: RestaurantDetail): RestaurantForm {
  const { latitude, longitude } = r.address
  return {
    name: r.name,
    cuisine: r.cuisine,
    description: r.description ?? '',
    phone: r.phone ?? '',
    imageUrl: r.imageUrl ?? '',
    deliveryFee: formatMoney(r.deliveryFee),
    minOrderValue: formatMoney(r.minOrderValue),
    deliveryTimeMin: String(r.deliveryTimeMin),
    deliveryTimeMax: String(r.deliveryTimeMax),
    address: {
      zipCode: r.address.zipCode,
      street: r.address.street,
      number: r.address.number,
      complement: r.address.complement ?? '',
      district: r.address.district,
      city: r.address.city,
      state: r.address.state,
    },
    location: latitude != null && longitude != null ? { latitude, longitude } : null,
    openingHours: r.openingHours.map((h) => ({ ...h, opensAt: hhmm(h.opensAt), closesAt: hhmm(h.closesAt) })),
  }
}

export interface RestaurantRequest {
  name: string
  cuisine: string
  description?: string
  phone?: string
  imageUrl?: string
  deliveryFee: number
  minOrderValue: number
  deliveryTimeMin: number
  deliveryTimeMax: number
  address: {
    street: string
    number: string
    complement?: string
    district: string
    city: string
    state: string
    zipCode: string
    latitude: number
    longitude: number
  }
  openingHours: OpeningHour[]
}

/** Converte o formulário já validado no corpo de POST/PUT /restaurants/mine. */
export function toRestaurantRequest(f: RestaurantForm): RestaurantRequest {
  // Validado pelo schema: a localização já foi marcada.
  const location = f.location!
  const blankToUndefined = (v: string | undefined) => (v?.trim() ? v.trim() : undefined)
  return {
    name: f.name.trim(),
    cuisine: f.cuisine.trim(),
    description: blankToUndefined(f.description),
    phone: blankToUndefined(f.phone),
    imageUrl: blankToUndefined(f.imageUrl),
    deliveryFee: toNumber(f.deliveryFee),
    minOrderValue: toNumber(f.minOrderValue),
    deliveryTimeMin: Number(f.deliveryTimeMin),
    deliveryTimeMax: Number(f.deliveryTimeMax),
    address: {
      street: f.address.street.trim(),
      number: f.address.number.trim(),
      complement: blankToUndefined(f.address.complement),
      district: f.address.district.trim(),
      city: f.address.city.trim(),
      state: f.address.state.trim().toUpperCase(),
      zipCode: f.address.zipCode.trim(),
      latitude: location.latitude,
      longitude: location.longitude,
    },
    openingHours: f.openingHours,
  }
}
