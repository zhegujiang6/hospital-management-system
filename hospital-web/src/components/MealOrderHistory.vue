<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  cancelMealOrder,
  getPatientMealOrderDetail,
  getPatientMealOrders,
  simulateMealOrderPayment,
} from '@/api/mealOrder'

const statusOptions = [
  { label: '全部订单', value: '' },
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
  status: '',
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
const actionSubmitting = ref(false)

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

function formatDateTime(value) {
  return value ? value.replace('T', ' ').slice(0, 19) : '--'
}

async function loadOrders() {
  loading.value = true

  try {
    const result =
      (await getPatientMealOrders({
        pageNum: filters.pageNum,
        pageSize: filters.pageSize,
        status: filters.status || undefined,
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

function changeStatus(status) {
  filters.status = status
  filters.pageNum = 1
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
    detail.value = await getPatientMealOrderDetail(order.orderId)
  } catch (error) {
    detailVisible.value = false
    ElMessage.error(error.message)
  } finally {
    detailLoading.value = false
  }
}

async function payCurrentOrder() {
  if (!detail.value) return

  try {
    await ElMessageBox.confirm(
      `确认模拟支付订单 ${detail.value.orderNo} 吗？`,
      '模拟支付',
      {
        confirmButtonText: '确认支付',
        cancelButtonText: '暂不支付',
        type: 'warning',
      },
    )
  } catch {
    return
  }

  actionSubmitting.value = true

  try {
    await simulateMealOrderPayment(detail.value.orderId)
    detailVisible.value = false
    ElMessage.success('订单支付成功')
    await loadOrders()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    actionSubmitting.value = false
  }
}

async function cancelCurrentOrder() {
  if (!detail.value) return

  try {
    await ElMessageBox.confirm(
      '取消后已占用的菜品库存会立即归还，确定继续吗？',
      '取消订单',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '保留订单',
        type: 'warning',
      },
    )
  } catch {
    return
  }

  actionSubmitting.value = true

  try {
    await cancelMealOrder(detail.value.orderId)
    detailVisible.value = false
    ElMessage.success('订单已取消，库存已经归还')
    await loadOrders()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    actionSubmitting.value = false
  }
}

onMounted(loadOrders)
</script>

<template>
  <section class="content order-history-page">
    <div class="welcome-strip orders-welcome">
      <div>
        <span class="eyebrow">MY MEAL ORDERS</span>
        <h2>我的餐饮订单</h2>
        <p>查看支付状态、制作进度和院内配送信息。</p>
      </div>
      <div class="order-count">
        <strong>{{ pageData.total }}</strong>
        <span>全部订单</span>
      </div>
    </div>

    <div class="panel order-panel">
      <div class="order-toolbar">
        <div class="status-tabs">
          <button
            v-for="option in statusOptions"
            :key="option.value"
            type="button"
            :class="{ active: filters.status === option.value }"
            @click="changeStatus(option.value)"
          >
            {{ option.label }}
          </button>
        </div>
        <el-button plain :loading="loading" @click="loadOrders">刷新</el-button>
      </div>

      <div v-loading="loading" class="order-list-wrap">
        <div v-if="!loading && pageData.records.length" class="order-list">
          <article
            v-for="order in pageData.records"
            :key="order.orderId"
            class="order-card"
            @click="openDetail(order)"
          >
            <div class="order-card-top">
              <div>
                <span>订单号 {{ order.orderNo }}</span>
                <strong>{{ order.serviceDate }} · {{ periodLabels[order.mealPeriod] }}</strong>
              </div>
              <el-tag :type="statusInfo(order.status).type" effect="light" round>
                {{ statusInfo(order.status).label }}
              </el-tag>
            </div>

            <div class="order-card-main">
              <div class="order-metric">
                <span>菜品</span>
                <strong>{{ order.itemKindCount }} 种 / {{ order.totalQuantity }} 份</strong>
              </div>
              <div class="order-location">
                <span>{{ deliveryLabels[order.deliveryType] || order.deliveryType }}</span>
                <strong>{{ order.deliveryLocation }}</strong>
              </div>
              <div class="order-price">
                <span>订单金额</span>
                <strong>¥{{ Number(order.totalAmount).toFixed(2) }}</strong>
              </div>
            </div>

            <div class="order-card-bottom">
              <span>创建于 {{ formatDateTime(order.createTime) }}</span>
              <button type="button">查看详情 →</button>
            </div>
          </article>
        </div>

        <el-empty v-else-if="!loading" description="暂时没有符合条件的订单" />
      </div>

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
    </div>

    <el-dialog v-model="detailVisible" title="订单详情" width="680px" align-center>
      <div v-loading="detailLoading" class="detail-wrap">
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
            <el-descriptions-item label="收餐人">{{ detail.recipientName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ detail.recipientPhone || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="配送类型">
              {{ deliveryLabels[detail.deliveryType] || detail.deliveryType }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDateTime(detail.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="配送位置" :span="2">
              {{ detail.deliveryLocation }}
            </el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">
              {{ detail.remark || '无' }}
            </el-descriptions-item>
          </el-descriptions>

          <div class="detail-items">
            <h4>菜品明细</h4>
            <div v-for="item in detail.items" :key="item.itemId" class="detail-item">
              <div>
                <strong>{{ item.productName }}</strong>
                <span>{{ item.productNo }} · ¥{{ Number(item.unitPrice).toFixed(2) }} × {{ item.quantity }}</span>
              </div>
              <strong>¥{{ Number(item.subtotalAmount).toFixed(2) }}</strong>
            </div>
          </div>

          <div class="detail-total">
            <span>订单合计</span>
            <strong>¥{{ Number(detail.totalAmount).toFixed(2) }}</strong>
          </div>

          <el-alert
            v-if="detail.status === 'PENDING_PAYMENT'"
            :title="`请在 ${formatDateTime(detail.paymentDeadline)} 前完成支付`"
            type="warning"
            :closable="false"
            show-icon
          />
        </template>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <template v-if="detail?.status === 'PENDING_PAYMENT'">
          <el-button :disabled="actionSubmitting" @click="cancelCurrentOrder">取消订单</el-button>
          <el-button type="primary" :loading="actionSubmitting" @click="payCurrentOrder">
            模拟支付
          </el-button>
        </template>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.orders-welcome {
  background:
    radial-gradient(circle at 82% 30%, rgba(255, 255, 255, 0.12) 0 12%, transparent 12.5%),
    linear-gradient(125deg, #405373, #526d91 58%, #6985a8);
}

.order-count {
  display: grid;
  min-width: 95px;
  justify-items: center;
  gap: 3px;
  padding: 12px 18px;
  border: 1px solid rgba(255, 255, 255, 0.28);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.1);
}

.order-count strong {
  font-size: 22px;
}

.order-count span {
  font-size: 9px;
  opacity: 0.8;
}

.order-panel {
  min-height: 450px;
  margin-top: 20px;
  padding: 22px;
}

.order-toolbar,
.order-card-top,
.order-card-main,
.order-card-bottom,
.detail-heading,
.detail-item,
.detail-total {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.order-toolbar {
  gap: 15px;
  margin-bottom: 18px;
}

.status-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
}

.status-tabs button {
  padding: 7px 12px;
  border: 0;
  border-radius: 999px;
  background: #f3f5f8;
  color: #788398;
  cursor: pointer;
  font-size: 10px;
}

.status-tabs button.active {
  background: #344966;
  color: #fff;
}

.order-list-wrap {
  min-height: 315px;
}

.order-list {
  display: grid;
  gap: 13px;
}

.order-card {
  padding: 17px 19px;
  border: 1px solid #e7ebf2;
  border-radius: 14px;
  background: #fff;
  cursor: pointer;
  transition: 0.2s ease;
}

.order-card:hover {
  border-color: #bdcbe0;
  box-shadow: 0 8px 24px rgba(38, 55, 83, 0.07);
  transform: translateY(-1px);
}

.order-card-top > div,
.order-metric,
.order-location,
.order-price {
  display: grid;
  gap: 4px;
}

.order-card-top span,
.order-card-main span,
.order-card-bottom,
.detail-heading span,
.detail-item span {
  color: #909bad;
  font-size: 9px;
}

.order-card-top strong {
  color: #2f3b53;
  font-size: 13px;
}

.order-card-main {
  display: grid;
  grid-template-columns: 150px minmax(0, 1fr) 120px;
  gap: 25px;
  margin: 15px 0;
  padding: 14px 0;
  border-top: 1px dashed #e7ebf1;
  border-bottom: 1px dashed #e7ebf1;
}

.order-card-main strong {
  overflow: hidden;
  color: #4a566d;
  font-size: 11px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-price {
  justify-items: end;
}

.order-price strong {
  color: #d4712e;
  font-size: 16px;
}

.order-card-bottom button {
  border: 0;
  background: transparent;
  color: #49698f;
  cursor: pointer;
  font-size: 10px;
}

.order-pagination {
  justify-content: center;
  margin-top: 20px;
}

.detail-wrap {
  min-height: 170px;
}

.detail-heading {
  margin-bottom: 17px;
}

.detail-heading h3 {
  margin: 5px 0 0;
  color: #2f3a51;
}

.detail-items {
  margin-top: 20px;
}

.detail-items h4 {
  margin: 0 0 9px;
  color: #364157;
  font-size: 12px;
}

.detail-item {
  padding: 11px 2px;
  border-bottom: 1px solid #edf0f4;
}

.detail-item > div {
  display: grid;
  gap: 4px;
}

.detail-item strong {
  color: #465166;
  font-size: 11px;
}

.detail-total {
  margin: 17px 0;
  color: #647086;
  font-size: 11px;
}

.detail-total strong {
  color: #d16c29;
  font-size: 19px;
}

@media (max-width: 720px) {
  .order-card-main {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .order-price {
    justify-items: start;
  }
}
</style>
