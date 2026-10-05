<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  completeAdminMealOrder,
  deliverAdminMealOrder,
  getAdminMealOrderDetail,
  getAdminMealOrders,
  prepareAdminMealOrder,
} from '@/api/mealOrder'

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '待支付', value: 'PENDING_PAYMENT' },
  { label: '已支付', value: 'PAID' },
  { label: '制作中', value: 'PREPARING' },
  { label: '配送中', value: 'DELIVERING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' },
]

const periodLabels = {
  BREAKFAST: '早餐',
  LUNCH: '午餐',
  DINNER: '晚餐',
}

const deliveryLabels = {
  WARD: '病房或床位',
  DEPARTMENT: '医院科室',
  OTHER: '院内其他位置',
}

const filters = reactive({
  keyword: '',
  status: '',
  serviceDate: '',
  pageNum: 1,
  pageSize: 10,
})

const pageData = reactive({
  records: [],
  total: 0,
})

const loading = ref(false)
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)
const actionOrderId = ref(null)

function statusInfo(status) {
  const mapping = {
    PENDING_PAYMENT: { label: '待支付', type: 'warning' },
    PAID: { label: '已支付', type: 'success' },
    PREPARING: { label: '制作中', type: 'primary' },
    DELIVERING: { label: '配送中', type: 'primary' },
    COMPLETED: { label: '已完成', type: 'success' },
    CANCELLED: { label: '已取消', type: 'info' },
  }

  return mapping[status] || { label: status || '未知状态', type: 'info' }
}

function nextAction(status) {
  const mapping = {
    PAID: {
      label: '开始制作',
      title: '确认开始制作',
      message: '操作后订单将进入制作中状态，是否继续？',
      successMessage: '订单已进入制作中',
      request: prepareAdminMealOrder,
    },
    PREPARING: {
      label: '开始配送',
      title: '确认开始配送',
      message: '请确认餐品已经制作完成并交给配送人员。',
      successMessage: '订单已进入配送中',
      request: deliverAdminMealOrder,
    },
    DELIVERING: {
      label: '确认送达',
      title: '确认订单送达',
      message: '请确认餐品已经送达患者所在位置。',
      successMessage: '订单已经完成',
      request: completeAdminMealOrder,
    },
  }

  return mapping[status] || null
}

function formatDateTime(value) {
  return value ? value.replace('T', ' ').slice(0, 19) : '—'
}

function formatMoney(value) {
  return Number(value || 0).toFixed(2)
}

async function loadOrders() {
  loading.value = true

  try {
    const result =
      (await getAdminMealOrders({
        pageNum: filters.pageNum,
        pageSize: filters.pageSize,
        status: filters.status || undefined,
        serviceDate: filters.serviceDate || undefined,
        keyword: filters.keyword.trim() || undefined,
      })) || {}

    pageData.records = result.records || []
    pageData.total = Number(result.total || 0)
  } catch (error) {
    pageData.records = []
    pageData.total = 0
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

function searchOrders() {
  filters.pageNum = 1
  loadOrders()
}

function resetFilters() {
  Object.assign(filters, {
    keyword: '',
    status: '',
    serviceDate: '',
    pageNum: 1,
  })
  loadOrders()
}

function changePage(pageNum) {
  filters.pageNum = pageNum
  loadOrders()
}

async function openDetail(order) {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null

  try {
    detail.value = await getAdminMealOrderDetail(order.orderId)
  } catch (error) {
    detailVisible.value = false
    ElMessage.error(error.message)
  } finally {
    detailLoading.value = false
  }
}

async function executeAction(order) {
  const action = nextAction(order.status)
  if (!action) return

  try {
    await ElMessageBox.confirm(action.message, action.title, {
      confirmButtonText: action.label,
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }

  actionOrderId.value = order.orderId

  try {
    await action.request(order.orderId)
    ElMessage.success(action.successMessage)
    await loadOrders()

    if (detailVisible.value && detail.value?.orderId === order.orderId) {
      detail.value = await getAdminMealOrderDetail(order.orderId)
    }
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    actionOrderId.value = null
  }
}

onMounted(loadOrders)
</script>

<template>
  <div class="panel order-admin-panel">
    <div class="panel-heading">
      <div>
        <h3>餐饮订单处理</h3>
        <p>当前条件下共 {{ pageData.total }} 张订单，按创建时间倒序显示</p>
      </div>
      <el-button plain :loading="loading" @click="loadOrders">刷新数据</el-button>
    </div>

    <div class="order-filters">
      <el-input
        v-model="filters.keyword"
        clearable
        class="order-keyword"
        placeholder="订单号、收餐人或配送地点"
        @keyup.enter="searchOrders"
      />
      <el-select v-model="filters.status" class="order-status" placeholder="全部状态">
        <el-option
          v-for="option in statusOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
      <el-date-picker
        v-model="filters.serviceDate"
        type="date"
        value-format="YYYY-MM-DD"
        placeholder="选择供餐日期"
        class="order-date"
      />
      <el-button type="primary" @click="searchOrders">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="pageData.records"
      row-key="orderId"
      class="order-table"
      empty-text="暂无符合条件的餐饮订单"
      @row-click="openDetail"
    >
      <el-table-column label="订单" min-width="205">
        <template #default="{ row }">
          <div class="order-no-cell">
            <strong>{{ row.orderNo }}</strong>
            <span>患者ID：{{ row.patientId }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="收餐信息" min-width="180">
        <template #default="{ row }">
          <div class="recipient-cell">
            <strong>{{ row.recipientName }}</strong>
            <span>{{ row.recipientPhone || '未填写电话' }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="供餐" width="135">
        <template #default="{ row }">
          <div class="supply-cell">
            <strong>{{ row.serviceDate }}</strong>
            <span>{{ periodLabels[row.mealPeriod] || row.mealPeriod }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="配送位置" min-width="195">
        <template #default="{ row }">
          <div class="location-cell">
            <span>{{ deliveryLabels[row.deliveryType] || row.deliveryType }}</span>
            <strong>{{ row.deliveryLocation }}</strong>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="菜品" width="105" align="center">
        <template #default="{ row }">
          {{ row.itemKindCount }}种 / {{ row.totalQuantity }}份
        </template>
      </el-table-column>

      <el-table-column label="金额" width="100" align="right">
        <template #default="{ row }">
          <strong class="order-amount">¥{{ formatMoney(row.totalAmount) }}</strong>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusInfo(row.status).type" effect="light" round>
            {{ statusInfo(row.status).label }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="118" fixed="right" align="center">
        <template #default="{ row }">
          <el-button
            v-if="nextAction(row.status)"
            type="primary"
            link
            :loading="actionOrderId === row.orderId"
            @click.stop="executeAction(row)"
          >
            {{ nextAction(row.status).label }}
          </el-button>
          <span v-else class="no-action">查看详情</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="pageData.total > filters.pageSize"
      background
      layout="prev, pager, next"
      :current-page="filters.pageNum"
      :page-size="filters.pageSize"
      :total="pageData.total"
      class="order-pagination"
      @current-change="changePage"
    />

    <el-dialog v-model="detailVisible" title="餐饮订单详情" width="720px" align-center>
      <div v-loading="detailLoading" class="order-detail">
        <template v-if="detail">
          <div class="detail-heading">
            <div>
              <span>{{ detail.orderNo }}</span>
              <h3>{{ detail.serviceDate }} · {{ periodLabels[detail.mealPeriod] }}</h3>
            </div>
            <el-tag :type="statusInfo(detail.status).type" size="large" effect="light" round>
              {{ statusInfo(detail.status).label }}
            </el-tag>
          </div>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="患者ID">{{ detail.patientId }}</el-descriptions-item>
            <el-descriptions-item label="收餐人">{{ detail.recipientName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">
              {{ detail.recipientPhone || '未填写' }}
            </el-descriptions-item>
            <el-descriptions-item label="配送类型">
              {{ deliveryLabels[detail.deliveryType] || detail.deliveryType }}
            </el-descriptions-item>
            <el-descriptions-item label="配送位置" :span="2">
              {{ detail.deliveryLocation }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDateTime(detail.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="支付时间">
              {{ formatDateTime(detail.paidTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="订单备注" :span="2">
              {{ detail.remark || '无' }}
            </el-descriptions-item>
          </el-descriptions>

          <div class="detail-items">
            <h4>菜品明细</h4>
            <div v-for="item in detail.items" :key="item.itemId" class="detail-item">
              <div>
                <strong>{{ item.productName }}</strong>
                <span>
                  {{ item.productNo }} · ¥{{ formatMoney(item.unitPrice) }} × {{ item.quantity }}
                </span>
              </div>
              <strong>¥{{ formatMoney(item.subtotalAmount) }}</strong>
            </div>
          </div>

          <div class="detail-total">
            <span>订单合计</span>
            <strong>¥{{ formatMoney(detail.totalAmount) }}</strong>
          </div>
        </template>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="detail && nextAction(detail.status)"
          type="primary"
          :loading="actionOrderId === detail.orderId"
          @click="executeAction(detail)"
        >
          {{ nextAction(detail.status).label }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.order-admin-panel {
  min-height: 430px;
}

.order-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 18px;
}

.order-keyword {
  width: 260px;
}

.order-status {
  width: 135px;
}

.order-date {
  width: 165px !important;
}

.order-table {
  width: 100%;
}

.order-table :deep(.el-table__header th) {
  height: 48px;
  background: #f8fafd !important;
  color: #667189;
}

.order-table :deep(.el-table__row) {
  cursor: pointer;
}

.order-table :deep(.el-table__row td) {
  height: 70px;
}

.order-no-cell,
.recipient-cell,
.supply-cell,
.location-cell {
  display: grid;
  gap: 5px;
}

.order-no-cell strong,
.recipient-cell strong,
.supply-cell strong,
.location-cell strong {
  color: #303b52;
  font-size: 12px;
}

.order-no-cell span,
.recipient-cell span,
.supply-cell span,
.location-cell span,
.no-action,
.detail-heading span,
.detail-item span {
  color: #929caf;
  font-size: 10px;
}

.location-cell strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-amount {
  color: #cb6b29;
  font-size: 13px;
}

.order-pagination {
  justify-content: center;
  margin-top: 20px;
}

.order-detail {
  min-height: 170px;
}

.detail-heading,
.detail-item,
.detail-total {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.detail-heading {
  margin-bottom: 18px;
}

.detail-heading h3 {
  margin: 5px 0 0;
  color: #303b52;
}

.detail-items {
  margin-top: 20px;
}

.detail-items h4 {
  margin: 0 0 9px;
  color: #364157;
  font-size: 13px;
}

.detail-item {
  padding: 12px 2px;
  border-bottom: 1px solid #edf0f4;
}

.detail-item > div {
  display: grid;
  gap: 4px;
}

.detail-item strong {
  color: #465166;
  font-size: 12px;
}

.detail-total {
  margin-top: 18px;
  color: #647086;
  font-size: 12px;
}

.detail-total strong {
  color: #d16c29;
  font-size: 20px;
}

@media (max-width: 900px) {
  .order-keyword,
  .order-status,
  .order-date {
    width: 100% !important;
  }
}
</style>
