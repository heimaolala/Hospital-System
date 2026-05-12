import request from './request'

export function sendMessage(messages) {
  return request.post('/ai/chat', { messages }, { timeout: 60000 })
}
