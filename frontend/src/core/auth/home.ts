import type { Role } from './auth-store'

/** Tela inicial de cada perfil após o login. */
export function homeFor(role: Role): string {
  switch (role) {
    case 'RESTAURANT':
      return '/partner'
    case 'DRIVER':
      return '/driver'
    case 'ADMIN':
      return '/admin'
    default:
      return '/'
  }
}
