import { Link, Outlet } from 'react-router-dom'
import { LogOut, ShoppingBag, User } from 'lucide-react'
import { useAuthStore } from '@/core/auth/auth-store'

export function Layout() {
  const user = useAuthStore((s) => s.user)
  const logout = useAuthStore((s) => s.logout)

  return (
    <div className="min-h-screen flex flex-col">
      <header className="bg-white border-b border-gray-200 sticky top-0 z-10">
        <div className="max-w-6xl mx-auto px-4 h-16 flex items-center justify-between">
          <Link to="/" className="text-2xl font-extrabold text-brand-500 tracking-tight">
            rota<span className="text-gray-900">.</span>
          </Link>
          <nav className="flex items-center gap-4 text-sm">
            <Link to="/cart" className="flex items-center gap-1 hover:text-brand-600" aria-label="Carrinho">
              <ShoppingBag size={20} />
            </Link>
            {user ? (
              <>
                <span className="flex items-center gap-1 text-gray-600">
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
