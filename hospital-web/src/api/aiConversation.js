import request from '@/utils/request'

export function createAiConversation() {
  return request.post('/ai/conversations')
}

export function sendAiMessage(sessionId, content) {
  return request.post(
    `/ai/conversations/${sessionId}/messages`,
    { content },
    { timeout: 60000 },
  )
}

export function getAiConversationMessages(sessionId) {
  return request.get(`/ai/conversations/${sessionId}/messages`)
}

export function transferAiConversation(sessionId) {
  return request.post(`/ai/conversations/${sessionId}/transfer`)
}

export function sendPatientHumanMessage(sessionId, content) {
  return request.post(`/ai/conversations/${sessionId}/human-messages`, {
    content,
  })
}

export function closeAiConversation(sessionId, rating, feedback) {
  return request.post(`/ai/conversations/${sessionId}/close`, {
    rating,
    feedback,
  })
}

export function getAdminAiConversations(status) {
  return request.get('/ai/admin/conversations', {
    params: {
      status: status || undefined,
    },
  })
}

export function getAdminAiConversationMessages(sessionId) {
  return request.get(`/ai/admin/conversations/${sessionId}/messages`)
}

export function acceptAdminAiConversation(sessionId) {
  return request.post(`/ai/admin/conversations/${sessionId}/accept`)
}

export function sendAdminHumanMessage(sessionId, content) {
  return request.post(`/ai/admin/conversations/${sessionId}/messages`, {
    content,
  })
}
