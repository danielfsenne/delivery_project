import { useState } from 'react'
import { Search } from 'lucide-react'
import { useAdminUsers, useSetUserActive } from './api'
import { Pagination } from './Pagination'
import type { Role } from '@/core/auth/auth-store'
import { useAuthStore } from '@/core/auth/auth-store'
import { toApiError } from '@/core/api/client'
import { cn, formatDateTime } from '@/shared/lib/format'
import { useDebouncedValue } from '@/shared/lib/useDebouncedValue'
import { Badge, Button, Card, ErrorMessage, Spinner } from '@/shared/components/ui'

const roleLabel: Record<Role, string> = {
  CUSTOMER: 'Cliente',
  RESTAURANT: 'Restaurante',
  DRIVER: 'Entregador',
  ADMIN: 'Admin',
}

export function UsersAdmin() {
  const me = useAuthStore((s) => s.user)
  const [role, setRole] = useState<Role | ''>('')
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  // A busca só dispara quando a digitação para, sem uma requisição por tecla.
  const debouncedSearch = useDebouncedValue(search)
  const { data, isLoading, isError, error } = useAdminUsers(role, debouncedSearch, page)
  const setActive = useSetUserActive()

  return (
    <Card>
      <div className="flex flex-wrap items-center gap-3 border-b border-gray-100 p-3">
        <h2 className="flex-1 font-semibold">Usuários</h2>
        <label className="relative">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            value={search}
            onChange={(e) => {
              setSearch(e.target.value)
              setPage(0)
            }}
            placeholder="Nome ou e-mail"
            aria-label="Buscar usuários"
            className="w-56 rounded-lg border border-gray-300 py-2 pl-9 pr-3 text-sm"
          />
        </label>
        <select
          value={role}
          onChange={(e) => {
            setRole(e.target.value as Role | '')
            setPage(0)
          }}
          aria-label="Filtrar por perfil"
          className="rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm"
        >
          <option value="">Todos os perfis</option>
          {(Object.keys(roleLabel) as Role[]).map((r) => (
            <option key={r} value={r}>{roleLabel[r]}</option>
          ))}
        </select>
      </div>

      {isLoading && <Spinner />}
      {isError && <div className="p-3"><ErrorMessage message={toApiError(error).message} /></div>}
      {setActive.isError && <div className="p-3"><ErrorMessage message={toApiError(setActive.error).message} /></div>}
      {data && data.content.length === 0 && <p className="p-6 text-sm text-gray-500">Nenhum usuário encontrado.</p>}

      <ul className="divide-y divide-gray-100">
        {data?.content.map((user) => (
          <li key={user.id} className={cn('flex flex-wrap items-center gap-3 p-4', !user.active && 'opacity-60')}>
            <div className="min-w-48 flex-1">
              <p className="font-semibold">{user.name}</p>
              <p className="text-sm text-gray-600">{user.email}</p>
              <p className="text-xs text-gray-500">desde {formatDateTime(user.createdAt)}</p>
            </div>
            <Badge className="bg-gray-100 text-gray-700">{roleLabel[user.role]}</Badge>
            {!user.active && <Badge className="bg-red-100 text-red-700">Bloqueado</Badge>}
            {user.id !== me?.id && (
              <Button
                variant={user.active ? 'danger' : 'secondary'}
                className="px-3 py-1.5"
                loading={setActive.isPending && setActive.variables?.id === user.id}
                onClick={() => setActive.mutate({ id: user.id, active: !user.active })}
              >
                {user.active ? 'Bloquear' : 'Desbloquear'}
              </Button>
            )}
          </li>
        ))}
      </ul>
      {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage} />}
    </Card>
  )
}
