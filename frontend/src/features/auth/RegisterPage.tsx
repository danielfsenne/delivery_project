import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { register as registerUser } from './api'
import { useAuthStore } from '@/core/auth/auth-store'
import { toApiError } from '@/core/api/client'
import { Button, Card, ErrorMessage, Input } from '@/shared/components/ui'
import { homeFor } from '@/core/auth/home'
import { cn } from '@/shared/lib/format'

const roles = [
  { value: 'CUSTOMER', label: 'Cliente', hint: 'Quero pedir comida' },
  { value: 'RESTAURANT', label: 'Restaurante', hint: 'Quero vender' },
  { value: 'DRIVER', label: 'Entregador', hint: 'Quero entregar' },
] as const

const schema = z.object({
  name: z.string().trim().min(2, 'Informe seu nome'),
  email: z.string().email('Informe um e-mail válido'),
  password: z.string().min(8, 'A senha deve ter pelo menos 8 caracteres').max(72),
  phone: z.string().optional(),
  role: z.enum(['CUSTOMER', 'RESTAURANT', 'DRIVER']),
})

type FormData = z.infer<typeof schema>

export function RegisterPage() {
  const navigate = useNavigate()
  const setSession = useAuthStore((s) => s.setSession)

  const { register, handleSubmit, watch, setValue, formState, setError } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { role: 'CUSTOMER' },
  })
  const selectedRole = watch('role')

  const mutation = useMutation({
    mutationFn: registerUser,
    onSuccess: (session) => {
      setSession(session)
      navigate(homeFor(session.user.role), { replace: true })
    },
    onError: (error) => {
      const { fields } = toApiError(error)
      Object.entries(fields ?? {}).forEach(([field, message]) =>
        setError(field as keyof FormData, { message }),
      )
    },
  })

  return (
    <div className="max-w-md mx-auto py-10">
      <Card className="p-6">
        <h1 className="text-2xl font-bold">Criar conta</h1>

        <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="mt-6 space-y-4" noValidate>
          {mutation.isError && <ErrorMessage message={toApiError(mutation.error).message} />}

          <fieldset>
            <legend className="block text-sm font-medium text-gray-700 mb-2">Eu sou</legend>
            <div className="grid grid-cols-3 gap-2">
              {roles.map((r) => (
                <button
                  type="button"
                  key={r.value}
                  onClick={() => setValue('role', r.value)}
                  aria-pressed={selectedRole === r.value}
                  className={cn(
                    'rounded-lg border p-2 text-left transition',
                    selectedRole === r.value
                      ? 'border-brand-500 bg-brand-50 ring-2 ring-brand-500/20'
                      : 'border-gray-300 hover:border-gray-400',
                  )}
                >
                  <span className="block text-sm font-semibold">{r.label}</span>
                  <span className="block text-xs text-gray-500">{r.hint}</span>
                </button>
              ))}
            </div>
          </fieldset>

          <Input label="Nome" autoComplete="name" {...register('name')} error={formState.errors.name?.message} />
          <Input label="E-mail" type="email" autoComplete="email" {...register('email')}
                 error={formState.errors.email?.message} />
          <Input label="Telefone (opcional)" type="tel" autoComplete="tel" {...register('phone')}
                 error={formState.errors.phone?.message} />
          <Input label="Senha" type="password" autoComplete="new-password" {...register('password')}
                 error={formState.errors.password?.message} />

          <Button type="submit" className="w-full" loading={mutation.isPending}>
            Criar conta
          </Button>
        </form>

        <p className="mt-6 text-sm text-center text-gray-600">
          Já tem conta?{' '}
          <Link to="/login" className="font-semibold text-brand-600 hover:underline">
            Entrar
          </Link>
        </p>
      </Card>
    </div>
  )
}
