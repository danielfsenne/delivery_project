import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { LogOut, Receipt, ShoppingBag, Store, User } from 'lucide-react'
import { useAuthStore } from '@/core/auth/auth-store'
import { useCart } from '@/features/cart/api'
import { logout as logoutRequest } from '@/features/auth/api'

export function Layout() {
  const user = useAuthStore((s) => s.user)
  const refreshToken = useAuthStore((s) => s.refreshToken)
  const clearSession = useAuthStore((s) => s.logout)
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const { data: cart } = useCart()

  const logout = () => {
    if (refreshToken) logoutRequest(refreshToken).catch(() => undefined)
    clearSession()
    queryClient.clear()
    navigate('/')
  }

  const isCustomer = user?.role === 'CUSTOMER'

  return (
    <div className="min-h-screen flex flex-col">
      <header className="bg-white border-b border-gray-200 sticky top-0 z-10">
        <div className="max-w-6xl mx-auto px-4 h-16 flex items-center justify-between">
          <Link to="/" className="text-2xl font-extrabold text-brand-500 tracking-tight">
            rota<span className="text-gray-900">.</span>
          </Link>
          <nav className="flex items-center gap-4 text-sm">
            {isCustomer && (
              <>
                <NavLink to="/orders" className="flex items-center gap-1 hover:text-brand-600">
                  <Receipt size={18} /> <span className="hidden sm:inline">Pedidos</span>
                </NavLink>
                <NavLink to="/cart" className="relative flex items-center gap-1 hover:text-brand-600" aria-label="Carrinho">
                  <ShoppingBag size={20} />
                  {!!cart?.itemCount && (
                    <span className="absolute -top-2 -right-2 min-w-5 h-5 px-1 rounded-full bg-brand-500 text-white text-xs font-bold flex items-center justify-center">
                      {cart.itemCount}
                    </span>
                  )}
                </NavLink>
              </>
            )}
            {user?.role === 'RESTAURANT' && (
              <NavLink to="/partner" className="flex items-center gap-1 hover:text-brand-600">
                <Store size={18} /> <span className="hidden sm:inline">Painel</span>
              </NavLink>
            )}
            {user ? (
              <>
                <span className="hidden sm:flex items-center gap-1 text-gray-600">
                  <User size={18} /> {user.name}
                </span>
                <button onClick={logout} className="flex items-center gap-1 hover:text-brand-600">
                  <LogOut size={18} /> Sair
                </button>
              </>
            ) : (
              <Link to="/login" className="font-medium text-brand-600 hover:text-brand-700">
                Entrar
              </Link>
            )}
          </nav>
        </div>
      </header>
      <main className="flex-1 max-w-6xl w-full mx-auto px-4 py-6">
        <Outlet />
      </main>
    </div>
  )
}
