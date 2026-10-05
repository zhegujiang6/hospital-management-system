<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getPatientMealMenu } from '@/api/mealMenu'
import { createMealOrder, getMealPatientDeliveryInfo } from '@/api/mealOrder'

const props = defineProps({
  user: {
    type: Object,
    required: true,
  },
})

const emit = defineEmits(['navigate-orders'])

const mealPeriods = [
  { label: '早餐', value: 'BREAKFAST', time: '06:30—09:00' },
  { label: '午餐', value: 'LUNCH', time: '10:30—13:30' },
  { label: '晚餐', value: 'DINNER', time: '16:30—19:00' },
]

const selectedDate = ref(todayText())
const selectedPeriod = ref(currentMealPeriod())
const activeCategory = ref('ALL')
const menuItems = ref([])
const cartQuantities = ref({})
const loading = ref(false)
const checkoutVisible = ref(false)
const checkoutSubmitting = ref(false)
const checkoutFormRef = ref()
const deliveryInfo = ref(null)
const deliveryInfoLoading = ref(false)
const deliveryInfoError = ref('')
const orderResultVisible = ref(false)
const orderResult = ref(null)

const checkoutForm = reactive({
  deliveryType: 'WARD',
  recipientName: '',
  recipientPhone: '',
  deliveryLocation: '',
  remark: '',
})

const checkoutRules = {
  deliveryType: [{ required: true, message: '请选择配送位置类型', trigger: 'change' }],
  recipientName: [{ required: true, message: '请输入收餐人姓名', trigger: 'blur' }],
  deliveryLocation: [{ required: true, message: '请输入完整院内配送位置', trigger: 'blur' }],
}

const isWardDelivery = computed(() => checkoutForm.deliveryType === 'WARD')

const wardDeliveryLocation = computed(() => {
  const info = deliveryInfo.value
  if (!info?.currentlyAdmitted) return ''

  return [
    info.building,
    info.floorNo ? `${info.floorNo}层` : '',
    info.wardName,
    info.roomNo ? `${info.roomNo}病房` : '',
    info.bedNo ? `${info.bedNo}床` : '',
  ]
    .filter(Boolean)
    .join(' ')
})

const categories = computed(() => {
  const uniqueCategories = new Map()

  menuItems.value.forEach((item) => {
    uniqueCategories.set(item.categoryId, item.categoryName)
  })

  return Array.from(uniqueCategories, ([id, name]) => ({ id, name }))
})

const visibleMenuItems = computed(() => {
  if (activeCategory.value === 'ALL') return menuItems.value
  return menuItems.value.filter((item) => item.categoryId === activeCategory.value)
})

const cartItems = computed(() =>
  menuItems.value
    .filter((item) => quantityOf(item.stockId) > 0)
    .map((item) => ({ ...item, quantity: quantityOf(item.stockId) })),
)

const cartCount = computed(() =>
  cartItems.value.reduce((sum, item) => sum + item.quantity, 0),
)

const cartAmount = computed(() =>
  cartItems.value.reduce((sum, item) => sum + Number(item.price) * item.quantity, 0),
)

function todayText() {
  const date = new Date()
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function currentMealPeriod() {
  const hour = new Date().getHours()
  if (hour < 10) return 'BREAKFAST'
  if (hour < 15) return 'LUNCH'
  return 'DINNER'
}

function periodInfo(value) {
  return mealPeriods.find((period) => period.value === value) || mealPeriods[1]
}

function splitTags(value) {
  if (!value) return []
  return value
    .split(/[,，、]/)
    .map((tag) => tag.trim())
    .filter(Boolean)
    .slice(0, 3)
}

function quantityOf(stockId) {
  return Number(cartQuantities.value[stockId] || 0)
}

function increase(item) {
  const currentQuantity = quantityOf(item.stockId)

  if (currentQuantity >= item.availableStock) {
    ElMessage.warning('不能超过当前可售库存')
    return
  }

  cartQuantities.value = {
    ...cartQuantities.value,
    [item.stockId]: currentQuantity + 1,
  }
}

function decrease(item) {
  const nextQuantity = Math.max(quantityOf(item.stockId) - 1, 0)

  cartQuantities.value = {
    ...cartQuantities.value,
    [item.stockId]: nextQuantity,
  }
}

async function openCheckoutDialog() {
  Object.assign(checkoutForm, {
    deliveryType: 'WARD',
    recipientName: props.user.realName || '',
    recipientPhone: '',
    deliveryLocation: '',
    remark: '',
  })

  deliveryInfo.value = null
  deliveryInfoError.value = ''

  checkoutVisible.value = true
  await nextTick()
  checkoutFormRef.value?.clearValidate()
  await loadWardDeliveryInfo()
}

async function loadWardDeliveryInfo() {
  deliveryInfoLoading.value = true
  deliveryInfoError.value = ''

  try {
    deliveryInfo.value = await getMealPatientDeliveryInfo()
  } catch (error) {
    deliveryInfo.value = null
    deliveryInfoError.value = error.message
  } finally {
    deliveryInfoLoading.value = false
  }
}

async function handleDeliveryTypeChange(value) {
  checkoutFormRef.value?.clearValidate(['recipientName', 'deliveryLocation'])

  if (value === 'WARD' && !deliveryInfo.value) {
    await loadWardDeliveryInfo()
  }
}

async function submitOrder() {
  if (isWardDelivery.value && !deliveryInfo.value?.currentlyAdmitted) {
    ElMessage.warning(deliveryInfoError.value || '当前患者没有有效住院信息，无法配送到病房')
    return
  }

  const valid = await checkoutFormRef.value?.validate().catch(() => false)
  if (!valid) return

  checkoutSubmitting.value = true

  try {
    orderResult.value = await createMealOrder({
      deliveryType: checkoutForm.deliveryType,
      // 病房配送的姓名和地址由后端读取住院档案，前端不提交可信字段。
      recipientName: isWardDelivery.value ? null : checkoutForm.recipientName.trim(),
      recipientPhone: checkoutForm.recipientPhone.trim() || null,
      deliveryLocation: isWardDelivery.value ? null : checkoutForm.deliveryLocation.trim(),
      remark: checkoutForm.remark.trim() || null,
      items: cartItems.value.map((item) => ({
        stockId: item.stockId,
        quantity: item.quantity,
      })),
    })

    checkoutVisible.value = false
    orderResultVisible.value = true
    await loadMenu()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    checkoutSubmitting.value = false
  }
}

function formatDateTime(value) {
  return value ? value.replace('T', ' ').slice(0, 19) : '--'
}

async function loadMenu() {
  loading.value = true
  activeCategory.value = 'ALL'
  cartQuantities.value = {}

  try {
    menuItems.value =
      (await getPatientMealMenu({
        serviceDate: selectedDate.value,
        mealPeriod: selectedPeriod.value,
      })) || []
  } catch (error) {
    menuItems.value = []
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

onMounted(loadMenu)
</script>

<template>
  <section class="content ordering-page">
    <div class="welcome-strip ordering-welcome">
      <div>
        <span class="eyebrow">ORDER HOSPITAL MEALS</span>
        <h2>{{ user.realName }}，今天想吃点什么？</h2>
        <p>选择供应日期和餐次，院内餐饮会在订单确认后配送到你的住院位置。</p>
      </div>
      <div class="patient-badge">患者点餐</div>
    </div>

    <div class="menu-toolbar panel">
      <div class="date-select">
        <span>供应日期</span>
        <el-date-picker
          v-model="selectedDate"
          type="date"
          value-format="YYYY-MM-DD"
          :disabled-date="(date) => date.getTime() < new Date(todayText()).getTime()"
          @change="loadMenu"
        />
      </div>

      <div class="period-select">
        <button
          v-for="period in mealPeriods"
          :key="period.value"
          type="button"
          :class="['period-option', { active: selectedPeriod === period.value }]"
          @click="selectedPeriod = period.value; loadMenu()"
        >
          <strong>{{ period.label }}</strong>
          <small>{{ period.time }}</small>
        </button>
      </div>
    </div>

    <div class="menu-section panel">
      <div class="menu-heading">
        <div>
          <span class="section-kicker">今日菜单</span>
          <h3>{{ periodInfo(selectedPeriod).label }}可售菜品</h3>
          <p>{{ selectedDate }} · {{ periodInfo(selectedPeriod).time }}</p>
        </div>
        <el-button :loading="loading" plain @click="loadMenu">刷新菜单</el-button>
      </div>

      <div v-if="categories.length" class="category-tabs">
        <button
          type="button"
          :class="{ active: activeCategory === 'ALL' }"
          @click="activeCategory = 'ALL'"
        >
          全部菜品
        </button>
        <button
          v-for="category in categories"
          :key="category.id"
          type="button"
          :class="{ active: activeCategory === category.id }"
          @click="activeCategory = category.id"
        >
          {{ category.name }}
        </button>
      </div>

      <div v-loading="loading" class="menu-content">
        <div v-if="!loading && visibleMenuItems.length" class="meal-grid">
          <article v-for="item in visibleMenuItems" :key="item.stockId" class="meal-card">
            <div class="meal-visual">
              <img v-if="item.imageUrl" :src="item.imageUrl" :alt="item.productName" />
              <span v-else>{{ item.productName?.slice(0, 1) || '餐' }}</span>
              <em>{{ item.categoryName }}</em>
            </div>

            <div class="meal-card-body">
              <div class="meal-title-row">
                <div>
                  <h4>{{ item.productName }}</h4>
                  <small>{{ item.storeName }}</small>
                </div>
                <strong>¥{{ Number(item.price).toFixed(2) }}</strong>
              </div>

              <p class="meal-description">{{ item.description || '院内餐厅当日新鲜制作' }}</p>

              <div v-if="splitTags(item.dietaryTags).length" class="dietary-tags">
                <span v-for="tag in splitTags(item.dietaryTags)" :key="tag">{{ tag }}</span>
              </div>

              <p v-if="item.allergenInfo" class="allergen-info">过敏提示：{{ item.allergenInfo }}</p>

              <div class="meal-card-footer">
                <span>剩余 {{ item.availableStock }} 份</span>
                <el-button v-if="quantityOf(item.stockId) === 0" type="primary" round @click="increase(item)">
                  加入点餐单
                </el-button>
                <div v-else class="quantity-control">
                  <button type="button" @click="decrease(item)">−</button>
                  <strong>{{ quantityOf(item.stockId) }}</strong>
                  <button type="button" @click="increase(item)">＋</button>
                </div>
              </div>
            </div>
          </article>
        </div>

        <el-empty
          v-else-if="!loading"
          description="这个日期和餐次暂时没有可售菜品"
          :image-size="110"
        />
      </div>
    </div>

    <div v-if="cartCount" class="cart-summary">
      <div class="cart-count">{{ cartCount }}</div>
      <div>
        <span>已选 {{ cartCount }} 份</span>
        <strong>合计 ¥{{ cartAmount.toFixed(2) }}</strong>
      </div>
      <el-button type="primary" @click="openCheckoutDialog">填写配送信息</el-button>
    </div>

    <el-dialog v-model="checkoutVisible" title="确认点餐信息" width="580px" align-center>
      <div class="checkout-summary">
        <div>
          <span>供应安排</span>
          <strong>{{ selectedDate }} · {{ periodInfo(selectedPeriod).label }}</strong>
        </div>
        <div>
          <span>菜品数量</span>
          <strong>{{ cartCount }} 份</strong>
        </div>
        <div>
          <span>订单金额</span>
          <strong>¥{{ cartAmount.toFixed(2) }}</strong>
        </div>
      </div>

      <el-alert
        title="订单价格将由后端根据数据库中的实时价格重新计算。"
        type="info"
        :closable="false"
        show-icon
        class="checkout-alert"
      />

      <el-form
        ref="checkoutFormRef"
        :model="checkoutForm"
        :rules="checkoutRules"
        label-position="top"
      >
        <el-form-item label="配送位置类型" prop="deliveryType">
          <el-select
            v-model="checkoutForm.deliveryType"
            class="full-width"
            @change="handleDeliveryTypeChange"
          >
            <el-option label="住院病房或床位" value="WARD" />
            <el-option label="医院科室" value="DEPARTMENT" />
            <el-option label="医院内其他位置" value="OTHER" />
          </el-select>
        </el-form-item>

        <el-alert
          v-if="isWardDelivery && deliveryInfoError"
          :title="deliveryInfoError"
          type="error"
          :closable="false"
          show-icon
          class="checkout-alert"
        />

        <el-alert
          v-else-if="isWardDelivery && deliveryInfo && !deliveryInfo.currentlyAdmitted"
          title="当前患者未办理住院，不能选择病房配送。"
          type="warning"
          :closable="false"
          show-icon
          class="checkout-alert"
        />

        <div class="checkout-form-grid" v-loading="isWardDelivery && deliveryInfoLoading">
          <el-form-item :label="isWardDelivery ? '住院患者' : '收餐人姓名'" :prop="isWardDelivery ? undefined : 'recipientName'">
            <el-input
              v-if="isWardDelivery"
              :model-value="deliveryInfo?.patientName || ''"
              placeholder="正在读取患者档案"
              disabled
            />
            <el-input v-else v-model="checkoutForm.recipientName" maxlength="100" />
          </el-form-item>
          <el-form-item label="联系电话（选填）" prop="recipientPhone">
            <el-input
              v-model="checkoutForm.recipientPhone"
              maxlength="30"
              :placeholder="isWardDelivery && deliveryInfo?.patientPhone ? `默认使用 ${deliveryInfo.patientPhone}` : ''"
            />
          </el-form-item>
        </div>

        <el-form-item
          :label="isWardDelivery ? '当前住院床位' : '完整院内配送位置'"
          :prop="isWardDelivery ? undefined : 'deliveryLocation'"
        >
          <el-input
            v-if="isWardDelivery"
            :model-value="wardDeliveryLocation"
            placeholder="正在读取住院床位"
            disabled
          />
          <el-input
            v-else
            v-model="checkoutForm.deliveryLocation"
            maxlength="255"
            placeholder="例如：内科住院部3楼301病房2床"
          />
        </el-form-item>

        <el-alert
          v-if="isWardDelivery && deliveryInfo?.dietaryNotes"
          :title="`住院饮食备注：${deliveryInfo.dietaryNotes}`"
          type="warning"
          :closable="false"
          show-icon
          class="checkout-alert"
        />

        <el-form-item label="点餐备注（选填）" prop="remark">
          <el-input
            v-model="checkoutForm.remark"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="例如：少盐、餐具放在护士站"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="checkoutVisible = false">返回修改</el-button>
        <el-button type="primary" :loading="checkoutSubmitting" @click="submitOrder">
          提交订单
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="orderResultVisible"
      width="470px"
      align-center
      :show-close="false"
    >
      <div v-if="orderResult" class="order-success">
        <div class="success-mark">✓</div>
        <h3>订单创建成功</h3>
        <p>订单号：{{ orderResult.orderNo }}</p>
        <strong>¥{{ Number(orderResult.totalAmount).toFixed(2) }}</strong>
        <span>请在 {{ formatDateTime(orderResult.paymentDeadline) }} 前完成支付</span>
        <div class="success-actions">
          <el-button @click="orderResultVisible = false">继续点餐</el-button>
          <el-button type="primary" @click="orderResultVisible = false; emit('navigate-orders')">
            查看我的订单
          </el-button>
        </div>
      </div>
    </el-dialog>
  </section>
</template>

<style scoped>
.ordering-page {
  padding-bottom: 95px;
}

.ordering-welcome {
  background:
    radial-gradient(circle at 84% 38%, rgba(255, 255, 255, 0.14) 0 11%, transparent 11.5%),
    linear-gradient(125deg, #b05a20, #db7d2e 58%, #e99947);
}

.patient-badge {
  padding: 11px 18px;
  border: 1px solid rgba(255, 255, 255, 0.35);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 13px;
  font-weight: 700;
}

.menu-toolbar {
  display: flex;
  align-items: end;
  gap: 28px;
  margin: 20px 0;
  padding: 18px 22px;
}

.date-select {
  display: grid;
  gap: 8px;
}

.date-select > span {
  color: #7d889b;
  font-size: 11px;
  font-weight: 700;
}

.period-select {
  display: grid;
  flex: 1;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.period-option,
.category-tabs button {
  border: 0;
  cursor: pointer;
}

.period-option {
  display: grid;
  gap: 3px;
  padding: 11px 16px;
  border: 1px solid #e8ecf3;
  border-radius: 12px;
  background: #f8fafc;
  color: #667187;
  text-align: left;
  transition: 0.2s ease;
}

.period-option small {
  color: #9aa3b3;
  font-size: 9px;
}

.period-option.active {
  border-color: #e58a3e;
  background: #fff3e8;
  color: #b65e1f;
  box-shadow: 0 7px 18px rgba(196, 102, 34, 0.12);
}

.menu-section {
  min-height: 420px;
  padding: 24px;
}

.menu-heading,
.meal-title-row,
.meal-card-footer,
.cart-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-kicker {
  color: #d5782e;
  font-size: 9px;
  font-weight: 800;
  letter-spacing: 1.5px;
}

.menu-heading h3 {
  margin: 5px 0 4px;
  color: #273149;
  font-size: 19px;
}

.menu-heading p {
  margin: 0;
  color: #929cad;
  font-size: 10px;
}

.category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 9px;
  margin: 22px 0 18px;
}

.category-tabs button {
  padding: 7px 14px;
  border-radius: 999px;
  background: #f3f5f8;
  color: #7b869a;
  font-size: 11px;
}

.category-tabs button.active {
  background: #2f405e;
  color: #fff;
}

.menu-content {
  min-height: 260px;
}

.meal-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.meal-card {
  display: grid;
  grid-template-columns: 135px minmax(0, 1fr);
  overflow: hidden;
  border: 1px solid #e9edf3;
  border-radius: 16px;
  background: #fff;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.meal-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(35, 51, 79, 0.08);
}

.meal-visual {
  position: relative;
  display: grid;
  min-height: 190px;
  place-items: center;
  overflow: hidden;
  background: linear-gradient(145deg, #fff4e8, #f5d7b6);
  color: #bd6827;
}

.meal-visual img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.meal-visual > span {
  font-size: 34px;
  font-weight: 800;
}

.meal-visual em {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.86);
  color: #ab5b23;
  font-size: 9px;
  font-style: normal;
  font-weight: 700;
}

.meal-card-body {
  display: flex;
  min-width: 0;
  flex-direction: column;
  padding: 17px;
}

.meal-title-row {
  align-items: flex-start;
  gap: 12px;
}

.meal-title-row h4 {
  margin: 0;
  color: #263149;
  font-size: 14px;
}

.meal-title-row small {
  display: block;
  margin-top: 4px;
  color: #929daf;
  font-size: 9px;
}

.meal-title-row > strong {
  flex: 0 0 auto;
  color: #d36e27;
  font-size: 15px;
}

.meal-description {
  min-height: 34px;
  margin: 10px 0;
  color: #7f899b;
  font-size: 10px;
  line-height: 1.65;
}

.dietary-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.dietary-tags span {
  padding: 3px 7px;
  border-radius: 5px;
  background: #edf7ef;
  color: #5a8a62;
  font-size: 8px;
}

.allergen-info {
  margin: 7px 0 0;
  color: #b27456;
  font-size: 8px;
}

.meal-card-footer {
  margin-top: auto;
  padding-top: 12px;
}

.meal-card-footer > span {
  color: #8994a6;
  font-size: 9px;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 11px;
}

.quantity-control button {
  display: grid;
  width: 27px;
  height: 27px;
  place-items: center;
  border: 1px solid #e2a46f;
  border-radius: 50%;
  background: #fff8f1;
  color: #c86c29;
  cursor: pointer;
}

.quantity-control strong {
  color: #344159;
  font-size: 12px;
}

.cart-summary {
  position: sticky;
  z-index: 5;
  bottom: 18px;
  width: min(620px, calc(100% - 36px));
  gap: 14px;
  margin: 18px auto 0;
  padding: 13px 15px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 16px;
  background: rgba(38, 49, 72, 0.96);
  color: #fff;
  box-shadow: 0 15px 35px rgba(25, 34, 52, 0.22);
  backdrop-filter: blur(12px);
}

.cart-count {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  border-radius: 12px;
  background: #e98536;
  font-weight: 800;
}

.cart-summary > div:nth-child(2) {
  display: grid;
  flex: 1;
  gap: 3px;
}

.cart-summary span {
  color: #bfc7d4;
  font-size: 9px;
}

.cart-summary strong {
  font-size: 13px;
}

.checkout-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 16px;
}

.checkout-summary > div {
  display: grid;
  gap: 5px;
  padding: 12px;
  border-radius: 11px;
  background: #f6f8fb;
}

.checkout-summary span {
  color: #929cad;
  font-size: 9px;
}

.checkout-summary strong {
  color: #354158;
  font-size: 11px;
}

.checkout-alert {
  margin-bottom: 18px;
}

.checkout-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.full-width {
  width: 100%;
}

.order-success {
  display: grid;
  justify-items: center;
  padding: 15px 10px 8px;
  text-align: center;
}

.success-mark {
  display: grid;
  width: 56px;
  height: 56px;
  place-items: center;
  border-radius: 50%;
  background: #eaf8ee;
  color: #43a864;
  font-size: 25px;
  font-weight: 800;
}

.order-success h3 {
  margin: 16px 0 6px;
  color: #29344b;
}

.order-success p {
  margin: 0;
  color: #8b96a8;
  font-size: 10px;
}

.order-success > strong {
  margin: 18px 0 7px;
  color: #d66f29;
  font-size: 26px;
}

.order-success > span {
  margin-bottom: 20px;
  color: #8c96a7;
  font-size: 10px;
}

.success-actions {
  display: flex;
  gap: 10px;
}

@media (max-width: 980px) {
  .meal-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .menu-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .period-select {
    grid-template-columns: 1fr;
  }

  .meal-card {
    grid-template-columns: 105px minmax(0, 1fr);
  }

  .checkout-form-grid,
  .checkout-summary {
    grid-template-columns: 1fr;
  }
}
</style>
