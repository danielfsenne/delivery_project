import { createBrowserRouter } from 'react-router-dom'
import { Layout } from '@/shared/components/Layout'
import { HomePage } from '@/features/restaurants/HomePage'
import { LoginPage } from '@/features/auth/LoginPage'
import { RegisterPage } from '@/features/auth/RegisterPage'

export const router = createBrowserRouter([
  {
    element: <Layout />,
    children: [
      { path: '/', element: <HomePage /> },
      { path: '/login', element: <LoginPage /> },
      { path: '/register', element: <RegisterPage /> },
    ],
  },
])
