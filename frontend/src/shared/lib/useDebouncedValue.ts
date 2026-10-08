import { useEffect, useState } from 'react'

/** Devolve o valor só depois de {@code delay} ms sem mudanças (ex.: busca enquanto se digita). */
export function useDebouncedValue<T>(value: T, delay = 300): T {
  const [debounced, setDebounced] = useState(value)
  useEffect(() => {
    const timer = setTimeout(() => setDebounced(value), delay)
    return () => clearTimeout(timer)
  }, [value, delay])
  return debounced
}
