import type { AuthUser } from '@/core/auth/auth-store'

export interface Page<T> {
  content: T[]
  page: { size: number; number: number; totalElements: number; totalPages: number }
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  user: AuthUser
}

export interface Address {
  street: string
  number: string
  complement?: string | null
  district: string
  city: string
  state: string
  zipCode: string
  latitude?: number | null
  longitude?: number | null
}

export interface RestaurantSummary {
  id: number
  name: string
  description: string | null
  cuisine: string
  imageUrl: string | null
  deliveryFee: number
  minOrderValue: number
  deliveryTimeMin: number
  deliveryTimeMax: number
  city: string
  district: string
  active: boolean
  open: boolean
  ratingAverage: number | null
  ratingCount: number
}

export interface ProductOption {
  id: number
  name: string
  price: number
}

export interface Product {
  id: number
  name: string
  description: string | null
  price: number
  imageUrl: string | null
  available: boolean
  options: ProductOption[]
}

export interface Category {
  id: number
  name: string
  position: number
  products: Product[]
}

export interface RestaurantDetail extends Omit<RestaurantSummary, 'city' | 'district'> {
  ownerId: number
  phone: string | null
  address: Address
  categories: Category[]
}

export interface CartItem {
  id: string
  productId: number
  name: string
  unitPrice: number
  quantity: number
  options: ProductOption[]
  notes: string | null
}

export interface Cart {
  restaurantId: number | null
  restaurantName: string | null
  items: CartItem[]
  itemCount: number
  subtotal: number
  deliveryFee: number
  discount: number
  total: number
  minOrderValue: number
  reachesMinimumOrder: boolean
  couponCode: string | null
  couponError: string | null
}

export type OrderStatus =
  | 'CREATED'
  | 'PAYMENT_PENDING'
  | 'PAID'
  | 'RESTAURANT_ACCEPTED'
  | 'PREPARING'
  | 'READY_FOR_PICKUP'
  | 'OUT_FOR_DELIVERY'
  | 'DELIVERED'
  | 'CANCELLED'

export type PaymentMethod = 'PIX' | 'CREDIT_CARD' | 'CASH'

export interface OrderSummary {
  id: number
  status: OrderStatus
  restaurantId: number
  restaurantName: string
  itemCount: number
  total: number
  createdAt: string
}

export interface OrderItem {
  productId: number
  name: string
  options: string | null
  notes: string | null
  unitPrice: number
  quantity: number
  totalPrice: number
}

export interface OrderHistoryEntry {
  event: string
  oldStatus: OrderStatus | null
  newStatus: OrderStatus
  userId: number | null
  reason: string | null
  createdAt: string
}

export interface Order {
  id: number
  status: OrderStatus
  nextStatuses: OrderStatus[]
  customerId: number
  restaurantId: number
  restaurantName: string
  driverId: number | null
  paymentMethod: PaymentMethod
  items: OrderItem[]
  subtotal: number
  deliveryFee: number
  discount: number
  total: number
  couponCode: string | null
  notes: string | null
  deliveryAddress: Address
  history: OrderHistoryEntry[]
  createdAt: string
  updatedAt: string
}

export interface Review {
  orderId: number
  restaurantId: number
  foodRating: number
  deliveryRating: number | null
  comment: string | null
  createdAt: string
}

export interface RestaurantStats {
  ordersToday: number
  revenueToday: number
  averageTicket: number
  cancelledToday: number
  inProgress: number
}

export type DriverStatus = 'ONLINE' | 'OFFLINE' | 'BUSY'
export type DeliveryStatus = 'WAITING_DRIVER' | 'ASSIGNED' | 'PICKED_UP' | 'DELIVERED'

export interface Coordinates {
  latitude: number
  longitude: number
}

export interface Delivery {
  id: number
  orderId: number
  restaurantId: number
  restaurantName: string
  pickupAddress: string | null
  pickup: Coordinates | null
  dropoffAddress: string
  dropoff: Coordinates | null
  driverFee: number
  status: DeliveryStatus
  driverId: number | null
  distanceToPickupKm: number | null
  tripDistanceKm: number | null
  createdAt: string
  acceptedAt: string | null
  pickedUpAt: string | null
  deliveredAt: string | null
}

export interface DriverProfile {
  id: number
  status: DriverStatus
  location: Coordinates | null
  locationUpdatedAt: string | null
  currentDelivery: Delivery | null
  completedDeliveries: number
  totalEarnings: number
}

export interface DeliveryHistory {
  completedDeliveries: number
  totalEarnings: number
  deliveries: Delivery[]
}

export interface Tracking {
  orderId: number
  status: DeliveryStatus
  driverLocation: Coordinates | null
  driverLocationUpdatedAt: string | null
  pickup: Coordinates | null
  dropoff: Coordinates | null
}
