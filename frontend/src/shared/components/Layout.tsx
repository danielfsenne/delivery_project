import { Link, NavLink, Outlet, useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { LogOut, Receipt, Route, Search, Shield, ShoppingBag, Store } from 'lucide-react'
import { useAuthStore } from '@/core/auth/auth-store'
import { useCart } from '@/features/cart/api'
import { logout as logoutRequest } from '@/features/auth/api'
import { cn, formatCurrency } from '@/shared/lib/format'
import { Toaster } from './Toaster'

export function Logo({ className }: { className?: string }) {
  return (
    <Link to="/" className={cn('flex items-center gap-2', className)} aria-label="Rota, página inicial">
      <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-brand-500 text-xl font-black text-white">
        r
      </span>
      <span className="text-2xl font-black tracking-tight text-brand-500">rota</span>
    </Link>
  )
}

/** Busca do cabeçalho: escreve o termo na URL da home, que é quem filtra a lista. */
function HeaderSearch({ className }: { className?: string }) {
  const navigate = useNavigate()
  const { pathname } = useLocation()
  const [params] = useSearchParams()
  const value = pathname === '/' ? params.get('q') ?? '' : ''

  const change = (term: string) => {
    const next = new URLSearchParams(pathname === '/' ? params : undefined)
    if (term) next.set('q', term)
    else next.delete('q')
    navigate({ pathname: '/', search: next.toString() }, { replace: pathname === '/' })
  }

  return (
    <label
      className={cn(
        'flex items-center gap-2 rounded-lg bg-gray-100 px-4 py-2.5 focus-within:bg-white focus-within:ring-2 focus-within:ring-brand-500/40',
        className,
      )}
    >
      <Search size={18} className="text-brand-500" aria-hidden />
      <span className="sr-only">Buscar restaurantes</span>
      <input
        value={value}
        onChange={(e) => change(e.target.value)}
        placeholder="Busque por restaurante ou culinária"
        className="w-full bg-transparent text-sm outline-none placeholder:text-gray-500"
      />
    </label>
  )
}

const navLink = ({ isActive }: { isActive: boolean }) =>
  cn('flex items-center gap-1.5 font-semibold transition-colors hover:text-brand-500', isActive ? 'text-brand-500' : 'text-gray-600')

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
  const showSearch = !user || isCustomer

  return (
    <div className="min-h-screen flex flex-col">
      <header className="sticky top-0 z-20 border-b border-gray-100 bg-white/95 backdrop-blur">
        <div className="mx-auto flex h-[72px] max-w-6xl items-center gap-6 px-4">
          <Logo />
          {showSearch && <HeaderSearch className="hidden flex-1 md:flex max-w-xl" />}
          <nav className="ml-auto flex items-center gap-5 text-sm">
            {isCustomer && (
              <NavLink to="/orders" className={navLink}>
                <Receipt size={18} /> <span className="hidden sm:inline">Pedidos</span>
              </NavLink>
            )}
            {user?.role === 'RESTAURANT' && (
              <NavLink to="/partner" className={navLink}>
                <Store size={18} /> <span className="hidden sm:inline">Painel</span>
              </NavLink>
            )}
            {user?.role === 'DRIVER' && (
              <NavLink to="/driver" className={navLink}>
                <Route size={18} /> <span className="hidden sm:inline">Entregas</span>
              </NavLink>
            )}
            {user?.role === 'ADMIN' && (
              <NavLink to="/admin" className={navLink}>
                <Shield size={18} /> <span className="hidden sm:inline">Administração</span>
              </NavLink>
            )}
            {user ? (
              <div className="flex items-center gap-3">
                <span
                  className="flex h-9 w-9 items-center justify-center rounded-full bg-brand-50 font-bold text-brand-600"
                  title={user.name}
                >
                  {user.name.trim().charAt(0).toUpperCase()}
                </span>
                <button onClick={logout} className="flex items-center gap-1 font-semibold text-gray-600 hover:text-brand-500">
                  <LogOut size={18} /> <span className="hidden sm:inline">Sair</span>
                </button>
              </div>
            ) : (
              <Link to="/login" className="rounded-lg bg-brand-500 px-4 py-2 font-bold text-white hover:bg-brand-600">
                Entrar
              </Link>
            )}
            {isCustomer && (
              <NavLink
                to="/cart"
                aria-label={`Sacola com ${cart?.itemCount ?? 0} itens`}
                className="flex items-center gap-2 rounded-lg px-2 py-1.5 text-brand-500 hover:bg-brand-50"
              >
                <ShoppingBag size={22} />
                <span className="hidden flex-col leading-tight sm:flex">
                  <span className="text-xs font-bold text-gray-900">{formatCurrency(cart?.total ?? 0)}</span>
                  <span className="text-[11px] text-gray-500">{cart?.itemCount ?? 0} itens</span>
                </span>
                {!!cart?.itemCount && (
                  <span className="flex h-5 min-w-5 items-center justify-center rounded-full bg-brand-500 px-1 text-xs font-bold text-white sm:hidden">
                    {cart.itemCount}
                  </span>
                )}
              </NavLink>
            )}
          </nav>
        </div>
        {showSearch && (
          <div className="px-4 pb-3 md:hidden">
            <HeaderSearch />
          </div>
        )}
      </header>
      <main className="flex-1 max-w-6xl w-full mx-auto px-4 py-6">
        <Outlet />
      </main>
      <footer className="border-t border-gray-100 py-6 text-center text-xs text-gray-400">
        Rota · projeto de portfólio · dados de demonstração
      </footer>
      <Toaster />
    </div>
  )
}
