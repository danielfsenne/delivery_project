import { AxiosError, AxiosHeaders, type AxiosResponse } from 'axios'
import { toApiError } from './client'

function httpError(status: number, data: unknown) {
  const response = { status, data, statusText: '', headers: {}, config: { headers: new AxiosHeaders() } } as AxiosResponse
  return new AxiosError('erro', String(status), undefined, undefined, response)
}

describe('toApiError', () => {
  it('usa a mensagem enviada pelo serviço', () => {
    expect(toApiError(httpError(409, { message: 'Transição inválida' }))).toMatchObject({
      status: 409,
      message: 'Transição inválida',
    })
  })

  it('traduz respostas sem corpo do gateway', () => {
    expect(toApiError(httpError(429, '')).message).toMatch(/Muitas requisições/)
    expect(toApiError(httpError(503, '')).message).toMatch(/indisponível/)
  })

  it('sem resposta, indica falha de conexão', () => {
    expect(toApiError(new AxiosError('Network Error')).status).toBe(0)
  })
})
