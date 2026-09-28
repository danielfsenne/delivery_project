import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation } from '@tanstack/react-query'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { login } from './api'
import { useAuthStore } from '@/core/auth/auth-store'
import { toApiError } from '@/core/api/client'
import { Button, Card, ErrorMessage, Input } from '@/shared/components/ui'
import { homeFor } from '@/core/auth/home'

const schema = z.object({
  email: z.string().email('Informe um e-mail válido'),
  password: z.string().min(1, 'Informe a senha'),
})

type FormData = z.infer<typeof schema>

export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const setSession = useAuthStore((s) => s.setSession)
  const from = (location.state as { from?: string } | null)?.from

  const { register, handleSubmit, formState } = useForm<FormData>({ resolver: zodResolver(schema) })

  const mutation = useMutation({
    mutationFn: (data: FormData) => login(data.email, data.password),
    onSuccess: (session) => {
      setSession(session)
      navigate(from ?? homeFor(session.user.role), { replace: true })
    },
  })

  return (
    <div className="max-w-md mx-auto py-10">
      <Card className="p-6">
        <h1 className="text-2xl font-bold">Entrar</h1>
        <p className="text-sm text-gray-500 mt-1">Acesse sua conta para fazer pedidos.</p>

        <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="mt-6 space-y-4" noValidate>
          {mutation.isError && <ErrorMessage message={toApiError(mutation.error).message} />}
          <Input label="E-mail" type="email" autoComplete="email" {...register('email')}
                 error={formState.errors.email?.message} />
          <Input label="Senha" type="password" autoComplete="current-password" {...register('password')}
                 error={formState.errors.password?.message} />
          <Button type="submit" className="w-full" loading={mutation.isPending}>
            Entrar
          </Button>
        </form>

        <p className="mt-6 text-sm text-center text-gray-600">
          Ainda não tem conta?{' '}
          <Link to="/register" className="font-semibold text-brand-600 hover:underline">
            Cadastre-se
          </Link>
        </p>
        <p className="mt-4 text-xs text-center text-gray-400">
          Demo: cliente@rota.dev / restaurante@rota.dev — senha rota12345
        </p>
      </Card>
    </div>
  )
}
