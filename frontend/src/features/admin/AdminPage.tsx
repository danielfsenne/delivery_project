import { useState } from 'react'
import { Overview } from './Overview'
import { AdminOrders } from './AdminOrders'
import { CouponsAdmin } from './CouponsAdmin'
import { UsersAdmin } from './UsersAdmin'
import { cn } from '@/shared/lib/format'

const tabs = [
  ['overview', 'Visão geral'],
  ['orders', 'Pedidos'],
  ['coupons', 'Cupons'],
  ['users', 'Usuários'],
] as const

type Tab = (typeof tabs)[number][0]

export function AdminPage() {
  const [tab, setTab] = useState<Tab>('overview')

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">Administração</h1>

      <div role="tablist" className="flex gap-1 overflow-x-auto border-b border-gray-200">
        {tabs.map(([value, label]) => (
          <button
            key={value}
            role="tab"
            aria-selected={tab === value}
            onClick={() => setTab(value)}
            className={cn(
              '-mb-px whitespace-nowrap border-b-2 px-4 py-2 text-sm font-semibold',
              tab === value ? 'border-brand-500 text-brand-600' : 'border-transparent text-gray-500 hover:text-gray-800',
            )}
          >
            {label}
          </button>
        ))}
      </div>

      {tab === 'overview' && <Overview />}
      {tab === 'orders' && <AdminOrders />}
      {tab === 'coupons' && <CouponsAdmin />}
      {tab === 'users' && <UsersAdmin />}
    </div>
  )
}
