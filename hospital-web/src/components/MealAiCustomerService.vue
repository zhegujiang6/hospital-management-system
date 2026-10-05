<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  closeAiConversation,
  createAiConversation,
  getAiConversationMessages,
  sendAiMessage,
  sendPatientHumanMessage,
  transferAiConversation,
} from '@/api/aiConversation'

const conversationStorageKey = 'hospital-meal-ai-conversation'

const quickQuestions = [
  '今天有哪些餐食可以选择？',
  '餐食配送到病房需要多久？',
  '我有忌口，应该怎样备注？',
  '订单出现问题怎么办？',
]

const welcomeMessage = {
  localId: 'welcome',
  senderType: 'ASSISTANT',
  content: '您好，我是院内餐饮 AI 客服。您可以咨询菜单、下单、配送和餐饮订单相关问题。',
  createdAt: new Date(),
}

const sessionId = ref('')
const conversationState = ref('READY')
const messages = ref([{ ...welcomeMessage }])
const inputText = ref('')
const sending = ref(false)
const transferring = ref(false)
const closing = ref(false)
const ratingDialogVisible = ref(false)
const rating = ref(5)
const feedback = ref('')
const messageListRef = ref(null)

let localMessageSequence = 0
let humanPollingTimer = null

const statusText = computed(() => {
  if (conversationState.value === 'TRANSFERRED') return '等待人工客服'
  if (conversationState.value === 'CLOSED') return '会话已结束'
  return 'AI 客服在线'
})

const canSend = computed(() => (
  conversationState.value !== 'CLOSED' &&
  !sending.value
))

function nextLocalId() {
  localMessageSequence += 1
  return `local-${localMessageSequence}`
}

function appendMessage(senderType, content, createdAt = new Date()) {
  messages.value.push({
    localId: nextLocalId(),
    senderType,
    content,
    createdAt,
  })

  scrollToBottom()
}

function scrollToBottom() {
  nextTick(() => {
    const messageList = messageListRef.value
    if (messageList) {
      messageList.scrollTop = messageList.scrollHeight
    }
  })
}

function formatMessageTime(value) {
  return new Date(value).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
  })
}

function senderLabel(senderType) {
  if (senderType === 'USER') return '我'
  if (senderType === 'HUMAN') return '人工'
  return 'AI'
}

function applyHistoryMessages(historyMessages) {
  if (!historyMessages.length) return

  messages.value = historyMessages.map((message) => ({
    localId: `server-${message.id}`,
    senderType: message.sender_type,
    content: message.content,
    createdAt: message.created_at,
  }))

  scrollToBottom()
}

async function pollHumanMessages() {
  if (!sessionId.value || conversationState.value !== 'TRANSFERRED') return

  try {
    const historyMessages = await getAiConversationMessages(sessionId.value)
    applyHistoryMessages(historyMessages)
  } catch {
    // 轮询失败时保持当前页面，下一轮继续尝试。
  }
}

function stopHumanPolling() {
  if (humanPollingTimer) {
    window.clearInterval(humanPollingTimer)
    humanPollingTimer = null
  }
}

function startHumanPolling() {
  stopHumanPolling()
  pollHumanMessages()
  humanPollingTimer = window.setInterval(pollHumanMessages, 5000)
}

function persistConversation() {
  if (!sessionId.value) {
    sessionStorage.removeItem(conversationStorageKey)
    return
  }

  sessionStorage.setItem(
    conversationStorageKey,
    JSON.stringify({
      sessionId: sessionId.value,
      state: conversationState.value,
    }),
  )
}

async function restoreConversation() {
  const storedValue = sessionStorage.getItem(conversationStorageKey)
  if (!storedValue) return

  try {
    const storedConversation = JSON.parse(storedValue)
    if (!storedConversation.sessionId) return

    sessionId.value = storedConversation.sessionId
    conversationState.value = storedConversation.state || 'ACTIVE'

    const historyMessages = await getAiConversationMessages(sessionId.value)
    applyHistoryMessages(historyMessages)

    if (conversationState.value === 'TRANSFERRED') {
      startHumanPolling()
    }
  } catch {
    sessionId.value = ''
    conversationState.value = 'READY'
    messages.value = [{ ...welcomeMessage, createdAt: new Date() }]
    sessionStorage.removeItem(conversationStorageKey)
  }
}

async function ensureConversation() {
  if (sessionId.value) return sessionId.value

  const conversation = await createAiConversation()
  sessionId.value = conversation.session_id
  conversationState.value = 'ACTIVE'
  persistConversation()
  return sessionId.value
}

async function sendMessage(content = inputText.value) {
  const normalizedContent = content.trim()

  if (!normalizedContent || !canSend.value) return

  inputText.value = ''
  appendMessage('USER', normalizedContent)
  sending.value = true

  try {
    const currentSessionId = await ensureConversation()

    if (conversationState.value === 'TRANSFERRED') {
      await sendPatientHumanMessage(currentSessionId, normalizedContent)
      return
    }

    const result = await sendAiMessage(currentSessionId, normalizedContent)

    appendMessage(
      'ASSISTANT',
      result.assistant_message.content,
      result.assistant_message.created_at,
    )
  } catch (error) {
    appendMessage(
      'SYSTEM',
      `本次回复失败：${error.message}`,
    )
  } finally {
    sending.value = false
    scrollToBottom()
  }
}

function handleInputKeydown(event) {
  if (event.isComposing) return

  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

async function requestHumanService() {
  if (conversationState.value === 'CLOSED') return

  try {
    await ElMessageBox.confirm(
      '转接后将由人工客服继续处理，确定转接吗？',
      '转接人工客服',
      {
        confirmButtonText: '确定转接',
        cancelButtonText: '继续咨询 AI',
        type: 'warning',
      },
    )
  } catch {
    return
  }

  transferring.value = true

  try {
    const currentSessionId = await ensureConversation()
    await transferAiConversation(currentSessionId)
    conversationState.value = 'TRANSFERRED'
    persistConversation()
    appendMessage('SYSTEM', '已提交人工客服请求，请耐心等待工作人员接入。')
    startHumanPolling()
    ElMessage.success('已申请转接人工客服')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    transferring.value = false
  }
}

function openRatingDialog() {
  if (!sessionId.value) {
    ElMessage.info('当前还没有开始会话')
    return
  }

  rating.value = 5
  feedback.value = ''
  ratingDialogVisible.value = true
}

async function submitRating() {
  closing.value = true

  try {
    await closeAiConversation(
      sessionId.value,
      rating.value,
      feedback.value.trim() || null,
    )

    conversationState.value = 'CLOSED'
    persistConversation()
    stopHumanPolling()
    ratingDialogVisible.value = false
    appendMessage('SYSTEM', '本次会话已结束，感谢您的评价。')
    ElMessage.success('评价提交成功')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    closing.value = false
  }
}

function startNewConversation() {
  stopHumanPolling()
  sessionId.value = ''
  conversationState.value = 'READY'
  messages.value = [{
    ...welcomeMessage,
    createdAt: new Date(),
  }]
  inputText.value = ''
  rating.value = 5
  feedback.value = ''
  persistConversation()
}

onMounted(restoreConversation)
onBeforeUnmount(stopHumanPolling)
</script>

<template>
  <section class="content ai-service-page">
    <div class="service-intro">
      <div>
        <span class="eyebrow">MEAL SERVICE ASSISTANT</span>
        <h2>院内餐饮智能客服</h2>
        <p>咨询菜单、配送、下单和订单问题，复杂情况可以随时转接人工客服。</p>
      </div>
      <div class="service-status" :class="conversationState.toLowerCase()">
        <span></span>
        {{ statusText }}
      </div>
    </div>

    <div class="service-layout">
      <aside class="assistant-sidebar panel">
        <div class="assistant-avatar">AI</div>
        <h3>餐饮服务助手</h3>
        <p>工作时间内 AI 客服会立即响应，涉及特殊情况时可以申请人工处理。</p>

        <div class="service-scope">
          <strong>可以咨询</strong>
          <span>今日菜单与餐别</span>
          <span>下单和配送说明</span>
          <span>订单异常处理</span>
          <span>饮食备注填写</span>
        </div>

        <el-alert
          title="涉及疾病、用药或营养治疗的问题，请咨询医生或营养师。"
          type="warning"
          :closable="false"
          show-icon
        />
      </aside>

      <section class="chat-panel panel">
        <header class="chat-header">
          <div>
            <strong>当前会话</strong>
            <span>{{ sessionId ? `会话号 ${sessionId.slice(0, 8)}` : '发送消息后自动创建' }}</span>
          </div>
          <div class="chat-actions">
            <el-button
              :loading="transferring"
              :disabled="conversationState === 'TRANSFERRED' || conversationState === 'CLOSED'"
              @click="requestHumanService"
            >
              转人工
            </el-button>
            <el-button
              type="primary"
              plain
              :disabled="!sessionId || conversationState === 'CLOSED'"
              @click="openRatingDialog"
            >
              结束并评价
            </el-button>
          </div>
        </header>

        <div ref="messageListRef" class="message-list">
          <template v-for="message in messages" :key="message.localId">
            <div
              v-if="message.senderType === 'SYSTEM'"
              class="system-message"
            >
              {{ message.content }}
            </div>

            <div
              v-else
              class="message-row"
              :class="message.senderType === 'USER' ? 'user-message' : 'assistant-message'"
            >
              <div class="message-avatar">
                {{ senderLabel(message.senderType) }}
              </div>
              <div>
                <div class="message-bubble">{{ message.content }}</div>
                <time>{{ formatMessageTime(message.createdAt) }}</time>
              </div>
            </div>
          </template>

          <div v-if="sending" class="message-row assistant-message">
            <div class="message-avatar">AI</div>
            <div class="message-bubble typing-bubble">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>

        <div v-if="conversationState === 'CLOSED'" class="conversation-finished">
          <span>本次会话已结束</span>
          <el-button type="primary" @click="startNewConversation">
            开始新会话
          </el-button>
        </div>

        <template v-else>
          <div v-if="conversationState === 'TRANSFERRED'" class="human-service-banner">
            <span>正在等待或已接入人工客服，消息会自动刷新</span>
            <el-button plain size="small" @click="openRatingDialog">结束会话</el-button>
          </div>

          <footer class="composer">
          <div v-if="conversationState !== 'TRANSFERRED'" class="quick-questions">
            <button
              v-for="question in quickQuestions"
              :key="question"
              type="button"
              :disabled="sending"
              @click="sendMessage(question)"
            >
              {{ question }}
            </button>
          </div>

          <div class="composer-row">
            <el-input
              v-model="inputText"
              type="textarea"
              :rows="3"
              maxlength="4000"
              resize="none"
              :placeholder="conversationState === 'TRANSFERRED'
                ? '请输入需要告诉人工客服的内容'
                : '请输入您的餐饮问题，Enter 发送，Shift + Enter 换行'"
              @keydown="handleInputKeydown"
            />
            <el-button
              type="primary"
              :loading="sending"
              :disabled="!inputText.trim() || !canSend"
              @click="sendMessage()"
            >
              {{ conversationState === 'TRANSFERRED' ? '发送给人工' : '发送' }}
            </el-button>
          </div>
          </footer>
        </template>
      </section>
    </div>

    <el-dialog
      v-model="ratingDialogVisible"
      title="结束会话并评价"
      width="460px"
      align-center
    >
      <div class="rating-form">
        <span>您对本次服务满意吗？</span>
        <el-rate
          v-model="rating"
          size="large"
          show-text
          :texts="['很差', '较差', '一般', '满意', '非常满意']"
        />
        <el-input
          v-model="feedback"
          type="textarea"
          :rows="4"
          maxlength="500"
          show-word-limit
          placeholder="可以补充本次服务中做得好或需要改进的地方"
        />
        <p>高质量问答会进入人工审核流程，审核通过后用于完善知识库。</p>
      </div>

      <template #footer>
        <el-button @click="ratingDialogVisible = false">继续咨询</el-button>
        <el-button type="primary" :loading="closing" @click="submitRating">
          提交评价并结束
        </el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.ai-service-page {
  padding-bottom: 36px;
}

.service-intro {
  display: flex;
  min-height: 130px;
  align-items: center;
  justify-content: space-between;
  padding: 26px 30px;
  border-radius: 18px;
  background:
    radial-gradient(circle at 86% 42%, rgba(255, 255, 255, 0.12) 0 12%, transparent 12.5%),
    linear-gradient(125deg, #145f75, #177e87 55%, #2c9b91);
  color: #fff;
  box-shadow: 0 15px 38px rgba(24, 116, 126, 0.18);
}

.service-intro .eyebrow {
  color: #bfe9e7;
}

.service-intro h2 {
  margin: 9px 0 7px;
  font-size: 24px;
}

.service-intro p {
  margin: 0;
  color: #d2efec;
  font-size: 12px;
}

.service-status {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 10px 15px;
  border: 1px solid rgba(255, 255, 255, 0.28);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  font-size: 12px;
  font-weight: 700;
}

.service-status > span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #6bf0c4;
  box-shadow: 0 0 0 4px rgba(107, 240, 196, 0.14);
}

.service-status.transferred > span {
  background: #ffd36b;
}

.service-status.closed > span {
  background: #b9c4ce;
}

.service-layout {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 20px;
  margin-top: 20px;
}

.assistant-sidebar {
  align-self: start;
  padding: 25px 21px;
}

.assistant-avatar {
  display: grid;
  width: 58px;
  height: 58px;
  place-items: center;
  border-radius: 18px;
  background: linear-gradient(145deg, #16788a, #2eae9e);
  color: #fff;
  box-shadow: 0 10px 24px rgba(29, 137, 143, 0.22);
  font-size: 16px;
  font-weight: 800;
}

.assistant-sidebar h3 {
  margin: 18px 0 8px;
  color: #263249;
  font-size: 17px;
}

.assistant-sidebar > p {
  margin: 0;
  color: #8994a6;
  font-size: 11px;
  line-height: 1.75;
}

.service-scope {
  display: grid;
  gap: 9px;
  margin: 23px 0;
  padding: 17px;
  border-radius: 12px;
  background: #f5f9fa;
}

.service-scope strong {
  margin-bottom: 2px;
  color: #405064;
  font-size: 11px;
}

.service-scope span {
  color: #778496;
  font-size: 10px;
}

.service-scope span::before {
  margin-right: 7px;
  color: #29a193;
  content: "✓";
}

.chat-panel {
  display: flex;
  min-height: 650px;
  flex-direction: column;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 17px 20px;
  border-bottom: 1px solid #e8edf3;
}

.chat-header strong,
.chat-header span {
  display: block;
}

.chat-header strong {
  color: #273249;
  font-size: 14px;
}

.chat-header span {
  margin-top: 4px;
  color: #98a2b2;
  font-size: 9px;
}

.chat-actions {
  display: flex;
  gap: 8px;
}

.message-list {
  flex: 1;
  min-height: 380px;
  max-height: 470px;
  overflow-y: auto;
  padding: 22px;
  background: #fbfcfe;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 20px;
}

.message-row.user-message {
  flex-direction: row-reverse;
}

.message-avatar {
  display: grid;
  width: 34px;
  height: 34px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 11px;
  background: #e1f3f1;
  color: #16867e;
  font-size: 10px;
  font-weight: 800;
}

.user-message .message-avatar {
  background: #e9f1ff;
  color: #3474de;
}

.message-bubble {
  max-width: 570px;
  padding: 12px 15px;
  border: 1px solid #e5ebf1;
  border-radius: 5px 15px 15px;
  background: #fff;
  color: #374359;
  font-size: 12px;
  line-height: 1.75;
  white-space: pre-wrap;
  word-break: break-word;
}

.user-message .message-bubble {
  border-color: #397de2;
  border-radius: 15px 5px 15px 15px;
  background: #397de2;
  color: #fff;
}

.message-row time {
  display: block;
  margin-top: 5px;
  color: #a1aab8;
  font-size: 8px;
}

.user-message time {
  text-align: right;
}

.system-message {
  width: fit-content;
  max-width: 80%;
  margin: 8px auto 18px;
  padding: 6px 12px;
  border-radius: 999px;
  background: #eef2f6;
  color: #7c8799;
  font-size: 9px;
  text-align: center;
}

.typing-bubble {
  display: flex;
  gap: 5px;
  padding: 16px 18px;
}

.typing-bubble span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #6c7a8f;
  animation: typing 1s infinite ease-in-out;
}

.typing-bubble span:nth-child(2) {
  animation-delay: 0.15s;
}

.typing-bubble span:nth-child(3) {
  animation-delay: 0.3s;
}

.composer {
  padding: 15px 18px 18px;
  border-top: 1px solid #e8edf3;
  background: #fff;
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-bottom: 11px;
}

.quick-questions button {
  padding: 6px 10px;
  border: 1px solid #dce6e7;
  border-radius: 999px;
  background: #f5faf9;
  color: #52706f;
  cursor: pointer;
  font-size: 9px;
}

.quick-questions button:hover {
  border-color: #65aaa5;
  color: #19877e;
}

.quick-questions button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.composer-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 76px;
  align-items: end;
  gap: 10px;
}

.composer-row .el-button {
  height: 72px;
}

.conversation-finished {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  min-height: 112px;
  border-top: 1px solid #e8edf3;
  background: #f8fafc;
  color: #697589;
  font-size: 11px;
}

.human-service-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 18px;
  border-top: 1px solid #f0dfb8;
  background: #fffaf0;
  color: #8c6a28;
  font-size: 10px;
}

.rating-form {
  display: grid;
  gap: 17px;
}

.rating-form > span {
  color: #364158;
  font-size: 13px;
  font-weight: 700;
}

.rating-form p {
  margin: 0;
  color: #929cac;
  font-size: 10px;
  line-height: 1.6;
}

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.45; }
  30% { transform: translateY(-4px); opacity: 1; }
}

@media (max-width: 1120px) {
  .service-layout {
    grid-template-columns: 220px minmax(0, 1fr);
  }
}
</style>
