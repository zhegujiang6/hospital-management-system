<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPatientList } from '@/api/patient'
import { createPayment, mockPaymentSuccess } from '@/api/payment'
import { getScheduleList } from '@/api/schedule'
import {
  cancelRegistration,
  createRegistration,
  getRegistrationList,
} from '@/api/registration'

const orders = ref([])
const patients = ref([])
const schedules = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const payingOrderId = ref(null)
const formRef = ref()
let refreshTimer = null

const filters = reactive({
  keyword: '',
  status: '',
  scheduleDate: '',
})

const form = reactive({
  requestId: '',
  patientId: null,
  scheduleId: null,
})

const rules = {
  patientId: [{ required: true, message: '请选择患者', trigger: 'change' }],
  scheduleId: [{ required: true, message: '请选择排班', trigger: 'change' }],
}

const availablePatients = computed(() => patients.value.filter((item) => item.status === 1))

const availableSchedules = computed(() => {
  const today = formatLocalDate(new Date())

  return schedules.value.filter(
    (item) => item.status === 1 && item.scheduleDate >= today && item.remainingSlots > 0,
  )
})

const selectedSchedule = computed(() =>
  availableSchedules.value.find((item) => String(item.id) === String(form.scheduleId)),
)

const filteredOrders = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()

  return orders.value.filter((item) => {
    const matchesKeyword =
      !keyword ||
      item.orderNo?.toLowerCase().includes(keyword) ||
      item.patientNo?.toLowerCase().includes(keyword) ||
      item.patientName?.toLowerCase().includes(keyword) ||
      item.doctorName?.toLowerCase().includes(keyword) ||
      item.departmentName?.toLowerCase().includes(keyword)
    const matchesStatus = filters.status === '' || item.status === filters.status
    const matchesDate = !filters.scheduleDate || item.scheduleDate === filters.scheduleDate

    return matchesKeyword && matchesStatus && matchesDate
  })
})

const statistics = computed(() => ({
  total: orders.value.length,
  pending: orders.value.filter((item) => item.status === 'PENDING_PAYMENT').length,
  cancelled: orders.value.filter((item) => item.status === 'CANCELLED').length,
  expired: orders.value.filter((item) => item.status === 'EXPIRED').length,
}))

async function refreshPageData(showLoading) {
  if (showLoading) loading.value = true

  try {
    const [orderData, patientData, scheduleData] = await Promise.all([
      getRegistrationList(),
      getPatientList(),
      getScheduleList(),
    ])
    orders.value = orderData || []
    patients.value = patientData || []
    schedules.value = scheduleData || []
  } catch (error) {
    if (showLoading) ElMessage.error(error.message)
  } finally {
    if (showLoading) loading.value = false
  }
}

function loadPageData() {
  return refreshPageData(true)
}

function resetFilters() {
  filters.keyword = ''
  filters.status = ''
  filters.scheduleDate = ''
}

function createRequestId() {
  if (globalThis.crypto?.randomUUID) {
    return globalThis.crypto.randomUUID()
  }

  return `req-${Date.now()}-${Math.random().toString(16).slice(2)}`
}

async function openCreateDialog() {
  Object.assign(form, {
    requestId: createRequestId(),
    patientId: availablePatients.value[0]?.id ?? null,
    scheduleId: availableSchedules.value[0]?.id ?? null,
  })
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true

  try {
    await createRegistration({
      requestId: form.requestId,
      patientId: form.patientId,
      scheduleId: form.scheduleId,
    })
    ElMessage.success('挂号订单创建成功，请在15分钟内完成支付')
    dialogVisible.value = false
    await loadPageData()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    submitting.value = false
  }
}

async function handleCancel(row) {
  let reason

  try {
    const result = await ElMessageBox.prompt(
      `确定取消患者“${row.patientName}”的这笔挂号订单吗？取消成功后号源会自动归还。`,
      '取消挂号',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '返回',
        inputPlaceholder: '请输入取消原因',
        inputValidator: (value) => {
          if (!value?.trim()) return '取消原因不能为空'
          if (value.trim().length > 255) return '取消原因不能超过255个字符'
          return true
        },
        type: 'warning',
      },
    )
    reason = result.value.trim()
  } catch {
    return
  }

  try {
    await cancelRegistration(row.id, { cancelReason: reason })
    ElMessage.success('挂号订单已取消，号源已归还')
    await loadPageData()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

function isPaymentExpired(row) {
  return Boolean(row.paymentDeadline) && new Date(row.paymentDeadline).getTime() <= Date.now()
}

async function handlePay(row) {
  if (isPaymentExpired(row)) {
    ElMessage.warning('该订单已超过支付截止时间，不能继续支付')
    return
  }

  try {
    await ElMessageBox.confirm(
      `将模拟支付 ${formatMoney(row.amount)}。成功后，挂号订单和支付单会在同一个事务中更新。`,
      '模拟支付',
      {
        confirmButtonText: '确认支付',
        cancelButtonText: '暂不支付',
        type: 'info',
      },
    )
  } catch {
    return
  }

  payingOrderId.value = row.id

  try {
    const paymentId = await createPayment({
      registrationOrderId: row.id,
    })
    await mockPaymentSuccess(paymentId)
    ElMessage.success('模拟支付成功，挂号订单已更新为已支付')
    await loadPageData()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    payingOrderId.value = null
  }
}

function formatLocalDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatPeriod(value) {
  return value === 'MORNING' ? '上午' : '下午'
}

function formatMoney(value) {
  return `¥${Number(value || 0).toFixed(2)}`
}

function formatDateTime(value) {
  if (!value) return '—'

  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  })
    .format(new Date(value))
    .replaceAll('/', '-')
}

function statusMeta(status) {
  const map = {
    PENDING_PAYMENT: { label: '待支付', type: 'warning' },
    PAID: { label: '已支付', type: 'success' },
    CANCELLED: { label: '已取消', type: 'info' },
    EXPIRED: { label: '已超时', type: 'danger' },
    COMPLETED: { label: '已完成', type: 'primary' },
  }

  return map[status] || { label: status || '未知', type: 'info' }
}

function deadlineText(value) {
  if (!value) return '—'
  return new Date(value).getTime() <= Date.now() ? '已到支付截止时间' : formatDateTime(value)
}

onMounted(() => {
  loadPageData()
  refreshTimer = window.setInterval(() => refreshPageData(false), 30_000)
})

onBeforeUnmount(() => {
  if (refreshTimer) window.clearInterval(refreshTimer)
})
</script>

<template>
  <section class="content">
    <div class="welcome-strip registration-welcome">
      <div>
        <span class="eyebrow">REGISTRATION CENTER</span>
        <h2>创建并管理患者挂号订单</h2>
        <p>订单创建时原子扣减号源；主动取消或支付超时后，系统会在事务中自动归还号源。</p>
      </div>
      <button class="primary-action" type="button" @click="openCreateDialog">
        <span>＋</span>
        新建挂号
      </button>
    </div>

    <div class="stats-grid registration-stats-grid">
      <article class="stat-card">
        <div class="stat-icon stat-icon-blue">单</div>
        <div>
          <span>挂号订单总数</span>
          <strong>{{ statistics.total }}</strong>
        </div>
      </article>
      <article class="stat-card">
        <div class="stat-icon registration-pending-icon">待</div>
        <div>
          <span>等待支付</span>
          <strong>{{ statistics.pending }}</strong>
        </div>
      </article>
      <article class="stat-card">
        <div class="stat-icon stat-icon-gray">消</div>
        <div>
          <span>已取消订单</span>
          <strong>{{ statistics.cancelled }}</strong>
        </div>
      </article>
      <article class="stat-card">
        <div class="stat-icon registration-expired-icon">超</div>
        <div>
          <span>支付超时订单</span>
          <strong>{{ statistics.expired }}</strong>
        </div>
      </article>
    </div>

    <div class="panel">
      <div class="panel-heading">
        <div>
          <h3>挂号订单列表</h3>
          <p>共 {{ filteredOrders.length }} 条符合条件的数据</p>
        </div>
        <el-button plain @click="loadPageData">刷新数据</el-button>
      </div>

      <div class="filters registration-filters">
        <el-input
          v-model="filters.keyword"
          clearable
          placeholder="搜索订单号、患者、医生或科室"
          class="keyword-input"
        />
        <el-date-picker
          v-model="filters.scheduleDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="全部就诊日期"
          class="registration-date-filter"
        />
        <el-select v-model="filters.status" placeholder="全部状态" class="status-select">
          <el-option label="全部状态" value="" />
          <el-option label="待支付" value="PENDING_PAYMENT" />
          <el-option label="已支付" value="PAID" />
          <el-option label="已取消" value="CANCELLED" />
          <el-option label="已超时" value="EXPIRED" />
          <el-option label="已完成" value="COMPLETED" />
        </el-select>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="filteredOrders"
        row-key="id"
        class="department-table"
        empty-text="暂无挂号订单"
      >
        <el-table-column prop="orderNo" label="订单号" width="210">
          <template #default="{ row }">
            <span class="order-number">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="患者信息" min-width="145">
          <template #default="{ row }">
            <div class="registration-person">
              <span>{{ row.patientName?.slice(0, 1) }}</span>
              <div>
                <strong>{{ row.patientName }}</strong>
                <small>{{ row.patientNo }}</small>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="就诊安排" min-width="195">
          <template #default="{ row }">
            <div class="visit-summary">
              <strong>{{ row.scheduleDate }} {{ formatPeriod(row.period) }}</strong>
              <small>{{ row.departmentName }} · {{ row.doctorName }}</small>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="订单金额" width="105">
          <template #default="{ row }">
            <strong class="amount-text">{{ formatMoney(row.amount) }}</strong>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="105">
          <template #default="{ row }">
            <el-tag :type="statusMeta(row.status).type" effect="light" round>
              {{ statusMeta(row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="支付截止时间" width="175">
          <template #default="{ row }">
            <span class="deadline-text">{{ deadlineText(row.paymentDeadline) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="190" align="right" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING_PAYMENT'">
              <el-button
                link
                type="primary"
                :loading="payingOrderId === row.id"
                :disabled="isPaymentExpired(row)"
                @click="handlePay(row)"
              >
                {{ isPaymentExpired(row) ? '已超时' : '模拟支付' }}
              </el-button>
              <el-button
                link
                type="danger"
                :disabled="payingOrderId === row.id"
                @click="handleCancel(row)"
              >
                取消挂号
              </el-button>
            </template>
            <span v-else class="no-action">—</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="dialogVisible"
      title="新建挂号订单"
      width="680px"
      destroy-on-close
      align-center
    >
      <p class="dialog-description">
        选择患者与有剩余号源的排班，提交后订单将进入15分钟待支付状态。
      </p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="registration-form-grid">
          <el-form-item label="就诊患者" prop="patientId">
            <el-select v-model="form.patientId" filterable placeholder="请选择患者" style="width: 100%">
              <el-option
                v-for="item in availablePatients"
                :key="item.id"
                :label="`${item.name} · ${item.patientNo}`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="出诊排班" prop="scheduleId">
            <el-select v-model="form.scheduleId" filterable placeholder="请选择排班" style="width: 100%">
              <el-option
                v-for="item in availableSchedules"
                :key="item.id"
                :label="`${item.scheduleDate} ${formatPeriod(item.period)} · ${item.doctorName}`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </div>

        <div v-if="selectedSchedule" class="schedule-preview">
          <div>
            <span>就诊科室</span>
            <strong>{{ selectedSchedule.departmentName }}</strong>
          </div>
          <div>
            <span>出诊医生</span>
            <strong>{{ selectedSchedule.doctorName }}</strong>
          </div>
          <div>
            <span>剩余号源</span>
            <strong>{{ selectedSchedule.remainingSlots }} / {{ selectedSchedule.totalSlots }}</strong>
          </div>
          <div>
            <span>应付金额</span>
            <strong class="preview-amount">{{ formatMoney(selectedSchedule.registrationFee) }}</strong>
          </div>
        </div>

        <p v-else class="empty-schedule-tip">当前没有可挂号的排班，请先在排班管理中增加号源。</p>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          :disabled="!selectedSchedule"
          @click="submitForm"
        >
          确认挂号
        </el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.registration-welcome {
  background:
    radial-gradient(circle at 82% 45%, rgba(255, 255, 255, 0.16) 0 4%, transparent 4.5%),
    radial-gradient(circle at 88% 42%, rgba(255, 255, 255, 0.1) 0 12%, transparent 12.5%),
    linear-gradient(125deg, #bf3f5d, #d65b6f 55%, #d78a67);
}

.registration-pending-icon {
  background: #fff3d9;
  color: #c4821d;
}

.registration-expired-icon {
  background: #fff0ee;
  color: #d85d53;
}

.registration-stats-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.registration-filters {
  flex-wrap: wrap;
}

.registration-date-filter {
  width: 175px !important;
}

.order-number {
  color: #4d5c75;
  font-family: Consolas, monospace;
  font-size: 11px;
}

.registration-person {
  display: flex;
  align-items: center;
  gap: 10px;
}

.registration-person > span {
  display: grid;
  width: 34px;
  height: 34px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 50%;
  background: #fcebf0;
  color: #c84a69;
  font-size: 13px;
  font-weight: 700;
}

.registration-person strong,
.registration-person small,
.visit-summary strong,
.visit-summary small {
  display: block;
}

.registration-person strong,
.visit-summary strong {
  color: #273149;
  font-size: 13px;
}

.registration-person small,
.visit-summary small {
  margin-top: 4px;
  color: #8c96a8;
  font-size: 11px;
}

.amount-text,
.preview-amount {
  color: #cf4b62;
}

.deadline-text,
.no-action {
  color: #8490a3;
  font-size: 12px;
}

.registration-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 18px;
}

.schedule-preview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin: 8px 0 18px;
  padding: 18px;
  border: 1px solid #e8edf5;
  border-radius: 13px;
  background: #f8fafd;
}

.schedule-preview span,
.schedule-preview strong {
  display: block;
}

.schedule-preview span {
  margin-bottom: 7px;
  color: #8994a7;
  font-size: 11px;
}

.schedule-preview strong {
  color: #263149;
  font-size: 13px;
}

.empty-schedule-tip {
  margin: 8px 0 18px;
  padding: 14px 16px;
  border-radius: 10px;
  background: #fff6e7;
  color: #a66d1e;
  font-size: 12px;
}
</style>
