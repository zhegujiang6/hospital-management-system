<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  acceptAdminAiConversation,
  getAdminAiConversationMessages,
  getAdminAiConversations,
  sendAdminHumanMessage,
} from '@/api/aiConversation'

const statusOptions = [
  { label: '等待接入', value: 'WAITING_HUMAN' },
  { label: '处理中', value: 'HUMAN_ACTIVE' },
  { label: '已结束', value: 'CLOSED' },
]

const activeStatus = ref('WAITING_HUMAN')
const conversations = ref([])
const selectedConversation = ref(null)
const messages = ref([])
const replyText = ref('')
const listLoading = ref(false)
const messageLoading = ref(false)
const accepting = ref(false)
const sending = ref(false)
const messageListRef = ref(null)

let pollingTimer = null

const currentStatusInfo = computed(() => (
  statusOptions.find((item) => item.value === activeStatus.value)
))

function statusInfo(status) {
  const mapping = {
    WAITING_HUMAN: { label: '等待接入', type: 'warning' },
    HUMAN_ACTIVE: { label: '处理中', type: 'primary' },
    CLOSED: { label: '已结束', type: 'info' },
  }

  return mapping[status] || { label: status || '未知状态', type: 'info' }
}

function senderName(senderType) {
  const mapping = {
    USER: '患者',
    ASSISTANT: 'AI 客服',
    HUMAN: '人工客服',
    SYSTEM: '系统',
  }

  return mapping[senderType] || senderType
}

function formatDateTime(value) {
  return value ? value.replace('T', ' ').slice(0, 19) : '—'
}

function conversationPreview(conversation) {
  return conversation.last_message_content || '暂无消息摘要'
}

function normalizeConversationResult(result) {
  if (Array.isArray(result)) return result
  return result?.records || []
}

function scrollToBottom() {
  nextTick(() => {
    const messageList = messageListRef.value
    if (messageList) {
      messageList.scrollTop = messageList.scrollHeight
    }
  })
}

async function loadMessages(conversation, showError = true) {
  if (!conversation) return

  messageLoading.value = true

  try {
    messages.value = (
      await getAdminAiConversationMessages(conversation.session_id)
    ) || []
    scrollToBottom()
  } catch (error) {
    if (showError) ElMessage.error(error.message)
  } finally {
    messageLoading.value = false
  }
}

async function selectConversation(conversation) {
  selectedConversation.value = conversation
  replyText.value = ''
  await loadMessages(conversation)
}

async function loadConversations(showError = true) {
  listLoading.value = true

  try {
    const result = await getAdminAiConversations(activeStatus.value)
    conversations.value = normalizeConversationResult(result)

    if (selectedConversation.value) {
      const refreshedConversation = conversations.value.find(
        (item) => item.session_id === selectedConversation.value.session_id,
      )

      selectedConversation.value = refreshedConversation || null

      if (!refreshedConversation) {
        messages.value = []
      }
    }
  } catch (error) {
    conversations.value = []
    if (showError) ElMessage.error(error.message)
  } finally {
    listLoading.value = false
  }
}

async function changeStatus(status) {
  if (activeStatus.value === status) return

  activeStatus.value = status
  selectedConversation.value = null
  messages.value = []
  await loadConversations()
}

async function acceptConversation() {
  const conversation = selectedConversation.value
  if (!conversation) return

  try {
    await ElMessageBox.confirm(
      '接入后，该会话将进入你的人工处理队列。',
      '确认接入会话',
      {
        confirmButtonText: '确认接入',
        cancelButtonText: '取消',
        type: 'info',
      },
    )
  } catch {
    return
  }

  accepting.value = true

  try {
    await acceptAdminAiConversation(conversation.session_id)
    ElMessage.success('已接入该会话')

    activeStatus.value = 'HUMAN_ACTIVE'
    selectedConversation.value = null
    messages.value = []
    await loadConversations()

    const acceptedConversation = conversations.value.find(
      (item) => item.session_id === conversation.session_id,
    )

    if (acceptedConversation) {
      await selectConversation(acceptedConversation)
    }
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    accepting.value = false
  }
}

async function sendReply() {
  const content = replyText.value.trim()
  const conversation = selectedConversation.value

  if (!content || !conversation || sending.value) return

  sending.value = true

  try {
    const message = await sendAdminHumanMessage(
      conversation.session_id,
      content,
    )

    messages.value.push(message)
    replyText.value = ''
    scrollToBottom()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    sending.value = false
  }
}

function handleReplyKeydown(event) {
  if (event.isComposing) return

  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendReply()
  }
}

async function pollWorkspace() {
  await loadConversations(false)

  if (selectedConversation.value) {
    await loadMessages(selectedConversation.value, false)
  }
}

onMounted(async () => {
  await loadConversations()
  pollingTimer = window.setInterval(pollWorkspace, 5000)
})

onBeforeUnmount(() => {
  if (pollingTimer) window.clearInterval(pollingTimer)
})
</script>

<template>
  <section class="content customer-service-admin">
    <div class="service-admin-intro">
      <div>
        <span class="eyebrow">HUMAN SERVICE DESK</span>
        <h2>人工客服工作台</h2>
        <p>处理 AI 无法解决的餐饮问题，人工回复会同步展示在患者会话中。</p>
      </div>
      <div class="queue-summary">
        <strong>{{ conversations.length }}</strong>
        <span>{{ currentStatusInfo?.label }}会话</span>
      </div>
    </div>

    <div class="workbench panel">
      <aside class="conversation-queue">
        <header>
          <div>
            <strong>会话队列</strong>
            <span>页面每 5 秒自动刷新</span>
          </div>
          <el-button text :loading="listLoading" @click="loadConversations()">
            刷新
          </el-button>
        </header>

        <div class="status-tabs">
          <button
            v-for="option in statusOptions"
            :key="option.value"
            type="button"
            :class="{ active: activeStatus === option.value }"
            @click="changeStatus(option.value)"
          >
            {{ option.label }}
          </button>
        </div>

        <div v-loading="listLoading" class="conversation-list">
          <button
            v-for="conversation in conversations"
            :key="conversation.session_id"
            type="button"
            class="conversation-card"
            :class="{
              active: selectedConversation?.session_id === conversation.session_id,
            }"
            @click="selectConversation(conversation)"
          >
            <div class="conversation-card-heading">
              <strong>患者用户 #{{ conversation.user_id }}</strong>
              <el-tag
                :type="statusInfo(conversation.status).type"
                size="small"
                effect="light"
              >
                {{ statusInfo(conversation.status).label }}
              </el-tag>
            </div>
            <p>{{ conversationPreview(conversation) }}</p>
            <div class="conversation-meta">
              <span>{{ conversation.session_id.slice(0, 8) }}</span>
              <time>{{ formatDateTime(conversation.updated_at || conversation.created_at) }}</time>
            </div>
          </button>

          <el-empty
            v-if="!listLoading && !conversations.length"
            description="当前没有会话"
            :image-size="70"
          />
        </div>
      </aside>

      <main v-if="selectedConversation" class="service-chat">
        <header class="service-chat-header">
          <div>
            <strong>患者用户 #{{ selectedConversation.user_id }}</strong>
            <span>会话号 {{ selectedConversation.session_id }}</span>
          </div>
          <el-button
            v-if="selectedConversation.status === 'WAITING_HUMAN'"
            type="primary"
            :loading="accepting"
            @click="acceptConversation"
          >
            接入会话
          </el-button>
          <el-tag
            v-else
            :type="statusInfo(selectedConversation.status).type"
            effect="light"
          >
            {{ statusInfo(selectedConversation.status).label }}
          </el-tag>
        </header>

        <div ref="messageListRef" v-loading="messageLoading" class="admin-message-list">
          <template v-for="message in messages" :key="message.id">
            <div v-if="message.sender_type === 'SYSTEM'" class="admin-system-message">
              {{ message.content }}
            </div>

            <div
              v-else
              class="admin-message-row"
              :class="{
                'human-message': message.sender_type === 'HUMAN',
                'patient-message': message.sender_type === 'USER',
              }"
            >
              <div class="admin-message-avatar">
                {{ senderName(message.sender_type).slice(0, 2) }}
              </div>
              <div>
                <span class="sender-name">{{ senderName(message.sender_type) }}</span>
                <div class="admin-message-bubble">{{ message.content }}</div>
                <time>{{ formatDateTime(message.created_at) }}</time>
              </div>
            </div>
          </template>

          <el-empty
            v-if="!messageLoading && !messages.length"
            description="暂无聊天记录"
            :image-size="72"
          />
        </div>

        <footer v-if="selectedConversation.status === 'HUMAN_ACTIVE'" class="admin-composer">
          <el-input
            v-model="replyText"
            type="textarea"
            :rows="3"
            maxlength="4000"
            resize="none"
            placeholder="输入人工回复，Enter 发送，Shift + Enter 换行"
            @keydown="handleReplyKeydown"
          />
          <el-button
            type="primary"
            :loading="sending"
            :disabled="!replyText.trim()"
            @click="sendReply"
          >
            发送回复
          </el-button>
        </footer>

        <div v-else-if="selectedConversation.status === 'WAITING_HUMAN'" class="accept-tip">
          接入会话后才能向患者发送人工回复
        </div>

        <div v-else class="accept-tip closed-tip">
          会话已经结束，只能查看历史记录
        </div>
      </main>

      <div v-else class="conversation-placeholder">
        <div>客</div>
        <strong>请选择一个会话</strong>
        <span>从左侧队列选择会话后，可以查看完整聊天记录并进行处理。</span>
      </div>
    </div>
  </section>
</template>

<style scoped>
.service-admin-intro {
  display: flex;
  min-height: 126px;
  align-items: center;
  justify-content: space-between;
  padding: 25px 30px;
  border-radius: 18px;
  background:
    radial-gradient(circle at 85% 40%, rgba(255, 255, 255, 0.12) 0 12%, transparent 12.5%),
    linear-gradient(125deg, #263e68, #315b8c 56%, #347d93);
  color: #fff;
  box-shadow: 0 15px 38px rgba(35, 73, 120, 0.2);
}

.service-admin-intro .eyebrow {
  color: #bed9ee;
}

.service-admin-intro h2 {
  margin: 9px 0 7px;
  font-size: 24px;
}

.service-admin-intro p {
  margin: 0;
  color: #d4e3ef;
  font-size: 12px;
}

.queue-summary {
  display: grid;
  min-width: 110px;
  justify-items: center;
  gap: 3px;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.1);
}

.queue-summary strong {
  font-size: 25px;
}

.queue-summary span {
  color: #d3e2ef;
  font-size: 9px;
}

.workbench {
  display: grid;
  grid-template-columns: 335px minmax(0, 1fr);
  min-height: 680px;
  margin-top: 20px;
}

.conversation-queue {
  min-width: 0;
  border-right: 1px solid #e7ecf3;
}

.conversation-queue > header,
.service-chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 70px;
  padding: 15px 18px;
  border-bottom: 1px solid #e8edf3;
}

.conversation-queue header strong,
.conversation-queue header span,
.service-chat-header strong,
.service-chat-header span {
  display: block;
}

.conversation-queue header strong,
.service-chat-header strong {
  color: #273249;
  font-size: 13px;
}

.conversation-queue header span,
.service-chat-header span {
  margin-top: 4px;
  color: #98a2b2;
  font-size: 9px;
}

.status-tabs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 5px;
  padding: 11px 12px;
  border-bottom: 1px solid #edf0f4;
  background: #fafbfd;
}

.status-tabs button {
  padding: 7px 5px;
  border: 0;
  border-radius: 7px;
  background: transparent;
  color: #7f8a9e;
  cursor: pointer;
  font-size: 10px;
}

.status-tabs button.active {
  background: #eaf1fd;
  color: #3471ce;
  font-weight: 700;
}

.conversation-list {
  height: 555px;
  overflow-y: auto;
  padding: 9px;
}

.conversation-card {
  display: block;
  width: 100%;
  margin-bottom: 7px;
  padding: 13px;
  border: 1px solid transparent;
  border-radius: 11px;
  background: #f8fafc;
  color: inherit;
  cursor: pointer;
  text-align: left;
}

.conversation-card:hover,
.conversation-card.active {
  border-color: #cbdcf4;
  background: #f0f5fd;
}

.conversation-card-heading,
.conversation-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.conversation-card-heading strong {
  color: #344057;
  font-size: 11px;
}

.conversation-card p {
  overflow: hidden;
  margin: 10px 0;
  color: #7d899b;
  font-size: 10px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-meta {
  color: #a0a9b7;
  font-size: 8px;
}

.service-chat {
  display: flex;
  min-width: 0;
  flex-direction: column;
}

.admin-message-list {
  height: 492px;
  overflow-y: auto;
  padding: 20px 23px;
  background: #fbfcfe;
}

.admin-message-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 18px;
}

.admin-message-row.human-message {
  flex-direction: row-reverse;
}

.admin-message-avatar {
  display: grid;
  width: 34px;
  height: 34px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 10px;
  background: #eaf1fd;
  color: #3973c9;
  font-size: 9px;
  font-weight: 800;
}

.human-message .admin-message-avatar {
  background: #e5f5f0;
  color: #27866d;
}

.sender-name {
  display: block;
  margin-bottom: 5px;
  color: #8792a5;
  font-size: 9px;
}

.human-message .sender-name,
.human-message time {
  text-align: right;
}

.admin-message-bubble {
  max-width: 560px;
  padding: 11px 14px;
  border: 1px solid #e3e9f0;
  border-radius: 4px 13px 13px;
  background: #fff;
  color: #3f4b61;
  font-size: 11px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.human-message .admin-message-bubble {
  border-color: #2e967e;
  border-radius: 13px 4px 13px 13px;
  background: #2e967e;
  color: #fff;
}

.admin-message-row time {
  display: block;
  margin-top: 4px;
  color: #a3acba;
  font-size: 8px;
}

.admin-system-message {
  width: fit-content;
  margin: 5px auto 16px;
  padding: 5px 11px;
  border-radius: 999px;
  background: #eef2f6;
  color: #7c8799;
  font-size: 9px;
}

.admin-composer {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 88px;
  align-items: end;
  gap: 10px;
  padding: 14px 18px 17px;
  border-top: 1px solid #e8edf3;
}

.admin-composer .el-button {
  height: 72px;
}

.accept-tip {
  display: grid;
  min-height: 105px;
  place-items: center;
  border-top: 1px solid #f0dfb8;
  background: #fffaf0;
  color: #967536;
  font-size: 10px;
}

.closed-tip {
  border-color: #e4e8ee;
  background: #f7f9fb;
  color: #8994a5;
}

.conversation-placeholder {
  display: grid;
  align-content: center;
  justify-items: center;
  padding: 40px;
  color: #8d98a9;
  text-align: center;
}

.conversation-placeholder > div {
  display: grid;
  width: 58px;
  height: 58px;
  place-items: center;
  border-radius: 18px;
  background: #ebf2fc;
  color: #4178c5;
  font-size: 20px;
  font-weight: 800;
}

.conversation-placeholder strong {
  margin: 17px 0 8px;
  color: #3e495f;
  font-size: 14px;
}

.conversation-placeholder span {
  max-width: 310px;
  font-size: 10px;
  line-height: 1.7;
}
</style>
