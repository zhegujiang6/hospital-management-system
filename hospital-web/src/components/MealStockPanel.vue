<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getMealProductList } from '@/api/mealProduct'
import {
  adjustMealStock,
  createMealStock,
  getMealStockList,
  updateMealStock,
} from '@/api/mealStock'
import { getMealStoreList } from '@/api/mealStore'

const mealPeriods = [
  { label: '早餐', value: 'BREAKFAST' },
  { label: '午餐', value: 'LUNCH' },
  { label: '晚餐', value: 'DINNER' },
]

const stores = ref([])
const products = ref([])
const stocks = ref([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const editDialogVisible = ref(false)
const editSubmitting = ref(false)
const editFormRef = ref()
const adjustDialogVisible = ref(false)
const adjustSubmitting = ref(false)
const adjustFormRef = ref()

const filters = reactive({
  storeId: '',
  productId: '',
  serviceDate: '',
  mealPeriod: '',
})

const form = reactive({
  productId: null,
  serviceDate: todayText(),
  mealPeriod: 'LUNCH',
  totalStock: 0,
})

const editForm = reactive({
  id: null,
  productName: '',
  serviceDate: '',
  mealPeriod: '',
  totalStock: 0,
  soldStock: 0,
  version: 0,
})

const adjustForm = reactive({
  id: null,
  productName: '',
  serviceDate: '',
  mealPeriod: '',
  totalStock: 0,
  availableStock: 0,
  operation: 'ADD',
  quantity: 1,
})

const rules = {
  productId: [{ required: true, message: '请选择菜品', trigger: 'change' }],
  serviceDate: [{ required: true, message: '请选择供应日期', trigger: 'change' }],
  mealPeriod: [{ required: true, message: '请选择供应餐次', trigger: 'change' }],
  totalStock: [{ required: true, message: '请输入总库存', trigger: 'change' }],
}

const editRules = {
  totalStock: [
    { required: true, message: '请输入总库存', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value < editForm.soldStock) {
          callback(new Error(`总库存不能小于已售数量 ${editForm.soldStock}`))
          return
        }
        callback()
      },
      trigger: 'change',
    },
  ],
}

const adjustRules = {
  operation: [{ required: true, message: '请选择调整方式', trigger: 'change' }],
  quantity: [
    { required: true, message: '请输入调整数量', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (!Number.isInteger(value) || value <= 0) {
          callback(new Error('调整数量必须是大于0的整数'))
          return
        }

        if (adjustForm.operation === 'REDUCE' && value > adjustForm.availableStock) {
          callback(new Error(`最多只能减少当前可售库存 ${adjustForm.availableStock} 份`))
          return
        }

        callback()
      },
      trigger: 'change',
    },
  ],
}

const filterProducts = computed(() => {
  if (!filters.storeId) return products.value
  return products.value.filter((product) => product.storeId === filters.storeId)
})

const statistics = computed(() => ({
  records: stocks.value.length,
  total: stocks.value.reduce((sum, stock) => sum + Number(stock.totalStock || 0), 0),
  available: stocks.value.reduce((sum, stock) => sum + Number(stock.availableStock || 0), 0),
  sold: stocks.value.reduce((sum, stock) => sum + Number(stock.soldStock || 0), 0),
}))

function todayText() {
  const date = new Date()
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function periodLabel(value) {
  return mealPeriods.find((period) => period.value === value)?.label || value
}

async function loadReferenceData() {
  try {
    const [storeList, productList] = await Promise.all([
      getMealStoreList(),
      getMealProductList(),
    ])

    stores.value = storeList || []
    products.value = productList || []
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function loadStocks() {
  loading.value = true

  try {
    stocks.value = (await getMealStockList(filters)) || []
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

function handleStoreChange() {
  if (
    filters.productId &&
    !filterProducts.value.some((product) => product.id === filters.productId)
  ) {
    filters.productId = ''
  }
  loadStocks()
}

function resetFilters() {
  Object.assign(filters, {
    storeId: '',
    productId: '',
    serviceDate: '',
    mealPeriod: '',
  })
  loadStocks()
}

async function openCreateDialog() {
  Object.assign(form, {
    productId: products.value[0]?.id || null,
    serviceDate: todayText(),
    mealPeriod: 'LUNCH',
    totalStock: 0,
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
    await createMealStock({
      productId: form.productId,
      serviceDate: form.serviceDate,
      mealPeriod: form.mealPeriod,
      totalStock: form.totalStock,
    })

    dialogVisible.value = false
    ElMessage.success('库存设置成功')
    await loadStocks()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    submitting.value = false
  }
}

async function openEditDialog(row) {
  Object.assign(editForm, {
    id: row.id,
    productName: row.productName,
    serviceDate: row.serviceDate,
    mealPeriod: row.mealPeriod,
    totalStock: row.totalStock,
    soldStock: row.soldStock,
    version: row.version,
  })

  editDialogVisible.value = true
  await nextTick()
  editFormRef.value?.clearValidate()
}

async function openAdjustDialog(row) {
  Object.assign(adjustForm, {
    id: row.id,
    productName: row.productName,
    serviceDate: row.serviceDate,
    mealPeriod: row.mealPeriod,
    totalStock: row.totalStock,
    availableStock: row.availableStock,
    operation: 'ADD',
    quantity: 1,
  })

  adjustDialogVisible.value = true
  await nextTick()
  adjustFormRef.value?.clearValidate()
}

async function submitAdjustForm() {
  const valid = await adjustFormRef.value?.validate().catch(() => false)
  if (!valid) return

  adjustSubmitting.value = true

  try {
    const quantityDelta =
      adjustForm.operation === 'ADD' ? adjustForm.quantity : -adjustForm.quantity

    await adjustMealStock(adjustForm.id, { quantityDelta })

    adjustDialogVisible.value = false
    ElMessage.success(adjustForm.operation === 'ADD' ? '库存增加成功' : '库存减少成功')
    await loadStocks()
  } catch (error) {
    ElMessage.error(error.message)
    await loadStocks()
  } finally {
    adjustSubmitting.value = false
  }
}

async function submitEditForm() {
  const valid = await editFormRef.value?.validate().catch(() => false)
  if (!valid) return

  editSubmitting.value = true

  try {
    await updateMealStock(editForm.id, {
      totalStock: editForm.totalStock,
      version: editForm.version,
    })

    editDialogVisible.value = false
    ElMessage.success('库存修改成功')
    await loadStocks()
  } catch (error) {
    ElMessage.error(error.message)
    await loadStocks()
  } finally {
    editSubmitting.value = false
  }
}

onMounted(async () => {
  await loadReferenceData()
  await loadStocks()
})
</script>

<template>
  <div>
    <div class="stock-stats">
      <article><span>库存记录</span><strong>{{ statistics.records }}</strong></article>
      <article><span>计划供应</span><strong>{{ statistics.total }}</strong></article>
      <article><span>当前可售</span><strong>{{ statistics.available }}</strong></article>
      <article><span>已经售出</span><strong>{{ statistics.sold }}</strong></article>
    </div>

    <div class="panel">
      <div class="panel-heading">
        <div>
          <h3>每日分时库存</h3>
          <p>同一菜品在不同日期和餐次分别管理可售数量</p>
        </div>
        <div class="heading-actions">
          <el-button plain @click="loadStocks">刷新数据</el-button>
          <el-button type="primary" @click="openCreateDialog">设置库存</el-button>
        </div>
      </div>

      <div class="filters stock-filters">
        <el-select v-model="filters.storeId" clearable placeholder="全部餐厅" @change="handleStoreChange">
          <el-option v-for="store in stores" :key="store.id" :label="store.name" :value="store.id" />
        </el-select>
        <el-select v-model="filters.productId" clearable filterable placeholder="全部菜品" @change="loadStocks">
          <el-option
            v-for="product in filterProducts"
            :key="product.id"
            :label="product.name"
            :value="product.id"
          />
        </el-select>
        <el-date-picker
          v-model="filters.serviceDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="供应日期"
          @change="loadStocks"
        />
        <el-select v-model="filters.mealPeriod" clearable placeholder="全部餐次" @change="loadStocks">
          <el-option
            v-for="period in mealPeriods"
            :key="period.value"
            :label="period.label"
            :value="period.value"
          />
        </el-select>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="stocks"
        row-key="id"
        class="stock-table"
        empty-text="暂无库存数据"
      >
        <el-table-column prop="serviceDate" label="供应日期" width="125" />
        <el-table-column label="餐次" width="90">
          <template #default="{ row }">
            <el-tag type="warning" effect="light" round>{{ periodLabel(row.mealPeriod) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="菜品" min-width="210">
          <template #default="{ row }">
            <strong class="stock-product">{{ row.productName }}</strong>
            <small class="stock-secondary">{{ row.productNo }} · {{ row.categoryName }}</small>
          </template>
        </el-table-column>
        <el-table-column prop="storeName" label="所属餐厅" min-width="190" />
        <el-table-column prop="totalStock" label="总库存" width="90" align="center" />
        <el-table-column prop="availableStock" label="可售" width="90" align="center" />
        <el-table-column prop="soldStock" label="已售" width="90" align="center" />
        <el-table-column label="版本" width="80" align="center">
          <template #default="{ row }">v{{ row.version }}</template>
        </el-table-column>
        <el-table-column label="操作" width="145" align="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openAdjustDialog(row)">增减</el-button>
            <el-button link type="warning" @click="openEditDialog(row)">校正</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="dialogVisible" title="设置分时库存" width="560px" destroy-on-close align-center>
        <p class="dialog-description">选择菜品、供应日期和餐次，创建时剩余库存等于总库存。</p>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="菜品" prop="productId">
            <el-select v-model="form.productId" filterable class="full-width" placeholder="请选择菜品">
              <el-option
                v-for="product in products"
                :key="product.id"
                :label="`${product.storeName} / ${product.name}`"
                :value="product.id"
              />
            </el-select>
          </el-form-item>
          <div class="stock-form-grid">
            <el-form-item label="供应日期" prop="serviceDate">
              <el-date-picker
                v-model="form.serviceDate"
                type="date"
                value-format="YYYY-MM-DD"
                :disabled-date="(date) => date.getTime() < new Date(todayText()).getTime()"
                class="full-width"
              />
            </el-form-item>
            <el-form-item label="供应餐次" prop="mealPeriod">
              <el-select v-model="form.mealPeriod" class="full-width">
                <el-option
                  v-for="period in mealPeriods"
                  :key="period.value"
                  :label="period.label"
                  :value="period.value"
                />
              </el-select>
            </el-form-item>
          </div>
          <el-form-item label="总库存" prop="totalStock">
            <el-input-number v-model="form.totalStock" :min="0" :max="999999" />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitForm">确认设置</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="adjustDialogVisible" title="增减库存" width="520px" align-center>
        <el-descriptions :column="2" border class="edit-summary">
          <el-descriptions-item label="菜品" :span="2">{{ adjustForm.productName }}</el-descriptions-item>
          <el-descriptions-item label="供应日期">{{ adjustForm.serviceDate }}</el-descriptions-item>
          <el-descriptions-item label="餐次">{{ periodLabel(adjustForm.mealPeriod) }}</el-descriptions-item>
          <el-descriptions-item label="当前总库存">{{ adjustForm.totalStock }}</el-descriptions-item>
          <el-descriptions-item label="当前可售">{{ adjustForm.availableStock }}</el-descriptions-item>
        </el-descriptions>

        <el-alert
          title="日常补货或临时下架请使用增减库存，并发下单时无需依赖页面中的版本号。"
          type="info"
          :closable="false"
          show-icon
          class="stock-dialog-alert"
        />

        <el-form ref="adjustFormRef" :model="adjustForm" :rules="adjustRules" label-position="top">
          <el-form-item label="调整方式" prop="operation">
            <el-radio-group v-model="adjustForm.operation">
              <el-radio-button value="ADD">增加库存</el-radio-button>
              <el-radio-button value="REDUCE">减少库存</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="调整数量" prop="quantity">
            <el-input-number
              v-model="adjustForm.quantity"
              :min="1"
              :max="adjustForm.operation === 'REDUCE' ? Math.max(adjustForm.availableStock, 1) : 999999"
              :step="1"
              step-strictly
            />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="adjustDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="adjustSubmitting" @click="submitAdjustForm">
            确认调整
          </el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="editDialogVisible" title="校正库存总量" width="520px" align-center>
        <el-descriptions :column="2" border class="edit-summary">
          <el-descriptions-item label="菜品" :span="2">{{ editForm.productName }}</el-descriptions-item>
          <el-descriptions-item label="供应日期">{{ editForm.serviceDate }}</el-descriptions-item>
          <el-descriptions-item label="餐次">{{ periodLabel(editForm.mealPeriod) }}</el-descriptions-item>
          <el-descriptions-item label="已售数量">{{ editForm.soldStock }}</el-descriptions-item>
          <el-descriptions-item label="当前版本">v{{ editForm.version }}</el-descriptions-item>
        </el-descriptions>

        <el-alert
          title="校正会按页面版本覆盖总库存；如果期间库存已变化，系统会提示刷新后重试。"
          type="warning"
          :closable="false"
          show-icon
          class="stock-dialog-alert"
        />

        <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-position="top">
          <el-form-item label="新的总库存" prop="totalStock">
            <el-input-number
              v-model="editForm.totalStock"
              :min="editForm.soldStock"
              :max="999999"
            />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="editSubmitting" @click="submitEditForm">
            确认校正
          </el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<style scoped>
.stock-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 18px;
}

.stock-stats article {
  padding: 18px 20px;
  border: 1px solid #e8edf5;
  border-radius: 14px;
  background: #fff;
}

.stock-stats span,
.stock-stats strong,
.stock-product,
.stock-secondary {
  display: block;
}

.stock-stats span {
  margin-bottom: 5px;
  color: #8a95a7;
  font-size: 11px;
}

.stock-stats strong {
  color: #263248;
  font-size: 23px;
}

.heading-actions {
  display: flex;
  gap: 10px;
}

.stock-filters :deep(.el-select) {
  width: 160px;
}

.stock-table {
  width: 100%;
}

.stock-table :deep(.el-table__header th) {
  height: 48px;
  background: #f8fafd !important;
  color: #667189;
}

.stock-table :deep(.el-table__row td) {
  height: 66px;
}

.stock-product {
  color: #273149;
  font-size: 13px;
}

.stock-secondary {
  margin-top: 4px;
  color: #8c97a9;
  font-size: 11px;
}

.stock-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.full-width {
  width: 100%;
}

.edit-summary {
  margin-bottom: 22px;
}

.stock-dialog-alert {
  margin-bottom: 20px;
}
</style>
