import { createBrowserRouter } from 'react-router-dom'
import { Layout } from '@/shared/components/Layout'
import { HomePage } from '@/features/restaurants/HomePage'
import { LoginPage } from '@/features/auth/LoginPage'
import { RegisterPage } from '@/features/auth/RegisterPage'
import { RestaurantPage } from '@/features/restaurants/RestaurantPage'
import { CartPage } from '@/features/cart/CartPage'
import { RequireRole } from '@/core/auth/RequireRole'
import { CheckoutPage } from '@/features/checkout/CheckoutPage'
import { OrdersPage } from '@/features/orders/OrdersPage'
import { OrderDetailPage } from '@/features/orders/OrderDetailPage'
import { PartnerPage } from '@/features/partner/PartnerPage'
import { DriverPage } from '@/features/driver/DriverPage'

export const router = createBrowserRouter([
  {
    element: <Layout />,
    children: [
      { path: '/', element: <HomePage /> },
      { path: '/login', element: <LoginPage /> },
      { path: '/register', element: <RegisterPage /> },
      { path: '/restaurants/:id', element: <RestaurantPage /> },
      { path: '/cart', element: <RequireRole roles={['CUSTOMER']}><CartPage /></RequireRole> },
      { path: '/checkout', element: <RequireRole roles={['CUSTOMER']}><CheckoutPage /></RequireRole> },
      { path: '/orders', element: <RequireRole roles={['CUSTOMER']}><OrdersPage /></RequireRole> },
      {
        path: '/orders/:id',
        element: <RequireRole roles={['CUSTOMER', 'RESTAURANT', 'DRIVER', 'ADMIN']}><OrderDetailPage /></RequireRole>,
      },
      { path: '/partner', element: <RequireRole roles={['RESTAURANT', 'ADMIN']}><PartnerPage /></RequireRole> },
      { path: '/driver', element: <RequireRole roles={['DRIVER']}><DriverPage /></RequireRole> },
    ],
  },
])
