<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createMealCategory, getMealCategoryList } from '@/api/mealCategory'
import { createMealStore, getMealStoreList } from '@/api/mealStore'
import MealProductPanel from '@/components/MealProductPanel.vue'
import MealStockPanel from '@/components/MealStockPanel.vue'
import MealOrderAdminPanel from '@/components/MealOrderAdminPanel.vue'

const activeSection = ref('stores')
const stores = ref([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const categories = ref([])
const categoryLoading = ref(false)
const categorySubmitting = ref(false)
const categoryDialogVisible = ref(false)
const categoryFormRef = ref()

const sections = [
  { key: 'stores', label: '餐厅管理', description: '维护院内餐厅、档口和配送位置' },
  { key: 'categories', label: '分类管理', description: '维护早餐、主食、套餐等分类' },
  { key: 'products', label: '菜品管理', description: '维护菜品、价格和饮食标签' },
  { key: 'inventory', label: '分时库存', description: '管理日期和餐次对应的可售数量' },
  { key: 'orders', label: '餐饮订单', description: '处理制作、配送和完成状态' },
]

const filters = reactive({
  keyword: '',
  status: '',
})

const form = reactive({
  storeNo: '',
  name: '',
  location: '',
  phone: '',
})

const categoryFilters = reactive({
  storeId: '',
})

const categoryForm = reactive({
  storeId: null,
  name: '',
  sortOrder: 0,
})

const rules = {
  storeNo: [
    { required: true, message: '请输入餐厅编号', trigger: 'blur' },
    { max: 30, message: '餐厅编号不能超过30个字符', trigger: 'blur' },
  ],
  name: [
    { required: true, message: '请输入餐厅名称', trigger: 'blur' },
    { max: 100, message: '餐厅名称不能超过100个字符', trigger: 'blur' },
  ],
  location: [
    { required: true, message: '请输入餐厅位置', trigger: 'blur' },
    { max: 200, message: '餐厅位置不能超过200个字符', trigger: 'blur' },
  ],
  phone: [{ max: 30, message: '联系电话不能超过30个字符', trigger: 'blur' }],
}

const categoryRules = {
  storeId: [{ required: true, message: '请选择所属餐厅', trigger: 'change' }],
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { max: 100, message: '分类名称不能超过100个字符', trigger: 'blur' },
  ],
  sortOrder: [{ required: true, message: '请输入显示顺序', trigger: 'change' }],
}

const currentSection = computed(() =>
  sections.find((section) => section.key === activeSection.value),
)

const filteredStores = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()

  return stores.value.filter((store) => {
    const matchesKeyword =
      !keyword ||
      [store.storeNo, store.name, store.location, store.phone]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(keyword))

    const matchesStatus = !filters.status || store.status === filters.status

    return matchesKeyword && matchesStatus
  })
})

const statistics = computed(() => ({
  total: stores.value.length,
  enabled: stores.value.filter((store) => store.status === 'ENABLED').length,
  locations: new Set(stores.value.map((store) => store.location).filter(Boolean)).size,
}))

const enabledStores = computed(() =>
  stores.value.filter((store) => store.status === 'ENABLED'),
)

async function loadStores() {
  loading.value = true

  try {
    stores.value = (await getMealStoreList()) || []
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  categoryLoading.value = true

  try {
    categories.value = (await getMealCategoryList(categoryFilters.storeId)) || []
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    categoryLoading.value = false
  }
}

async function openCreateDialog() {
  Object.assign(form, {
    storeNo: '',
    name: '',
    location: '',
    phone: '',
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
    await createMealStore({
      storeNo: form.storeNo.trim(),
      name: form.name.trim(),
      location: form.location.trim(),
      phone: form.phone.trim() || null,
    })

    dialogVisible.value = false
    ElMessage.success('餐厅创建成功')
    await loadStores()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    submitting.value = false
  }
}

async function openCategoryDialog() {
  Object.assign(categoryForm, {
    storeId: categoryFilters.storeId || enabledStores.value[0]?.id || null,
    name: '',
    sortOrder: 0,
  })

  categoryDialogVisible.value = true
  await nextTick()
  categoryFormRef.value?.clearValidate()
}

async function submitCategoryForm() {
  const valid = await categoryFormRef.value?.validate().catch(() => false)
  if (!valid) return

  categorySubmitting.value = true

  try {
    await createMealCategory({
      storeId: categoryForm.storeId,
      name: categoryForm.name.trim(),
      sortOrder: categoryForm.sortOrder,
    })

    categoryDialogVisible.value = false
    ElMessage.success('分类创建成功')
    await loadCategories()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    categorySubmitting.value = false
  }
}

function resetFilters() {
  filters.keyword = ''
  filters.status = ''
}

function formatDate(value) {
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

onMounted(() => {
  loadStores()
  loadCategories()
})
</script>

<template>
  <section class="content">
    <div class="welcome-strip meal-admin-welcome">
      <div>
        <span class="eyebrow">HOSPITAL MEAL OPERATIONS</span>
        <h2>院内餐饮运营中心</h2>
        <p>一个入口统一管理餐厅、菜品、库存、订单和病区配送。</p>
      </div>
      <div class="service-badge">餐饮微服务 · 8081</div>
    </div>

    <div class="section-tabs">
      <button
        v-for="section in sections"
        :key="section.key"
        type="button"
        :class="{ active: activeSection === section.key }"
        @click="activeSection = section.key"
      >
        <strong>{{ section.label }}</strong>
        <span>{{ section.description }}</span>
      </button>
    </div>

    <template v-if="activeSection === 'stores'">
      <div class="stats-grid">
        <article class="stat-card">
          <div class="stat-icon stat-icon-orange">餐</div>
          <div><span>餐厅总数</span><strong>{{ statistics.total }}</strong></div>
        </article>
        <article class="stat-card">
          <div class="stat-icon stat-icon-green">启</div>
          <div><span>正常营业</span><strong>{{ statistics.enabled }}</strong></div>
        </article>
        <article class="stat-card">
          <div class="stat-icon stat-icon-gray">位</div>
          <div><span>院内位置</span><strong>{{ statistics.locations }}</strong></div>
        </article>
      </div>

      <div class="panel">
        <div class="panel-heading">
          <div>
            <h3>餐厅与档口</h3>
            <p>当前显示 {{ filteredStores.length }} 条，共 {{ stores.length }} 条数据</p>
          </div>
          <div class="heading-actions">
            <el-button plain @click="loadStores">刷新数据</el-button>
            <el-button type="primary" @click="openCreateDialog">新增餐厅</el-button>
          </div>
        </div>

        <div class="filters">
          <el-input
            v-model="filters.keyword"
            clearable
            placeholder="搜索编号、名称、位置或电话"
            class="keyword-input"
          />
          <el-select v-model="filters.status" class="status-select" placeholder="全部状态">
            <el-option label="全部状态" value="" />
            <el-option label="营业中" value="ENABLED" />
            <el-option label="已停用" value="DISABLED" />
          </el-select>
          <el-button @click="resetFilters">重置</el-button>
        </div>

        <el-table
          v-loading="loading"
          :data="filteredStores"
          row-key="id"
          class="store-table"
          empty-text="暂无餐厅数据"
        >
          <el-table-column prop="storeNo" label="餐厅编号" width="145" />
          <el-table-column label="餐厅名称" min-width="200">
            <template #default="{ row }">
              <div class="store-name">
                <span>{{ row.name?.slice(0, 1) || '餐' }}</span>
                <strong>{{ row.name }}</strong>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="location" label="院内位置" min-width="190" />
          <el-table-column prop="phone" label="联系电话" width="150">
            <template #default="{ row }">{{ row.phone || '—' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="105">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'" round>
                {{ row.status === 'ENABLED' ? '营业中' : '已停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="175">
            <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </template>

    <template v-else-if="activeSection === 'categories'">
      <div class="panel">
        <div class="panel-heading">
          <div>
            <h3>菜品分类</h3>
            <p>当前显示 {{ categories.length }} 条分类</p>
          </div>
          <div class="heading-actions">
            <el-button plain @click="loadCategories">刷新数据</el-button>
            <el-button type="primary" @click="openCategoryDialog">新增分类</el-button>
          </div>
        </div>

        <div class="filters">
          <el-select
            v-model="categoryFilters.storeId"
            class="category-store-filter"
            placeholder="全部餐厅"
            @change="loadCategories"
          >
            <el-option label="全部餐厅" value="" />
            <el-option
              v-for="store in stores"
              :key="store.id"
              :label="store.name"
              :value="store.id"
            />
          </el-select>
        </div>

        <el-table
          v-loading="categoryLoading"
          :data="categories"
          row-key="id"
          class="store-table"
          empty-text="暂无分类数据"
        >
          <el-table-column prop="name" label="分类名称" min-width="180" />
          <el-table-column prop="storeName" label="所属餐厅" min-width="220" />
          <el-table-column prop="sortOrder" label="显示顺序" width="110" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'" round>
                {{ row.status === 'ENABLED' ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="175">
            <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </template>

    <MealProductPanel v-else-if="activeSection === 'products'" />

    <MealStockPanel v-else-if="activeSection === 'inventory'" />

    <MealOrderAdminPanel v-else-if="activeSection === 'orders'" />

    <div v-else class="panel pending-panel">
      <div class="pending-icon">餐</div>
      <h3>{{ currentSection?.label }}</h3>
      <p>{{ currentSection?.description }}，将在对应后端接口完成后接入真实数据。</p>
    </div>

    <el-dialog v-model="dialogVisible" title="新增餐厅" width="590px" destroy-on-close align-center>
      <p class="dialog-description">创建院内可供餐和配送的餐厅或档口。</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid">
          <el-form-item label="餐厅编号" prop="storeNo">
            <el-input v-model="form.storeNo" maxlength="30" placeholder="例如：CANTEEN-03" />
          </el-form-item>
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="form.phone" maxlength="30" placeholder="选填" />
          </el-form-item>
          <el-form-item label="餐厅名称" prop="name" class="wide-field">
            <el-input v-model="form.name" maxlength="100" placeholder="例如：门诊部便民餐厅" />
          </el-form-item>
          <el-form-item label="院内位置" prop="location" class="wide-field">
            <el-input v-model="form.location" maxlength="200" placeholder="例如：门诊部负一层西侧" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确认创建</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="categoryDialogVisible"
      title="新增菜品分类"
      width="540px"
      destroy-on-close
      align-center
    >
      <p class="dialog-description">分类必须属于一个正在营业的餐厅。</p>

      <el-form
        ref="categoryFormRef"
        :model="categoryForm"
        :rules="categoryRules"
        label-position="top"
      >
        <el-form-item label="所属餐厅" prop="storeId">
          <el-select v-model="categoryForm.storeId" class="full-width" placeholder="请选择餐厅">
            <el-option
              v-for="store in enabledStores"
              :key="store.id"
              :label="store.name"
              :value="store.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" maxlength="100" placeholder="例如：低盐套餐" />
        </el-form-item>
        <el-form-item label="显示顺序" prop="sortOrder">
          <el-input-number v-model="categoryForm.sortOrder" :min="0" :max="9999" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="categorySubmitting" @click="submitCategoryForm">
          确认创建
        </el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.meal-admin-welcome {
  background:
    radial-gradient(circle at 85% 38%, rgba(255, 255, 255, 0.14) 0 11%, transparent 11.5%),
    linear-gradient(125deg, #8b4c1d, #b96928 56%, #cf873d);
}

.service-badge {
  padding: 11px 18px;
  border: 1px solid rgba(255, 255, 255, 0.34);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 13px;
  font-weight: 700;
}

.section-tabs {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
  margin: 22px 0;
}

.section-tabs button {
  padding: 16px;
  border: 1px solid #e5eaf2;
  border-radius: 14px;
  background: #fff;
  color: #263248;
  cursor: pointer;
  text-align: left;
  transition: all 0.18s ease;
}

.section-tabs button:hover,
.section-tabs button.active {
  border-color: #e0a064;
  box-shadow: 0 10px 26px rgba(150, 83, 30, 0.1);
  transform: translateY(-2px);
}

.section-tabs strong,
.section-tabs span {
  display: block;
}

.section-tabs strong {
  margin-bottom: 7px;
  font-size: 14px;
}

.section-tabs span {
  color: #8a95a7;
  font-size: 11px;
  line-height: 1.55;
}

.heading-actions {
  display: flex;
  gap: 10px;
}

.store-table {
  width: 100%;
}

.store-table :deep(.el-table__header th) {
  height: 48px;
  background: #f8fafd !important;
  color: #667189;
}

.store-table :deep(.el-table__row td) {
  height: 66px;
}

.store-name {
  display: flex;
  align-items: center;
  gap: 11px;
}

.store-name span {
  display: grid;
  width: 35px;
  height: 35px;
  place-items: center;
  border-radius: 11px;
  background: #fff0e2;
  color: #b96826;
  font-size: 13px;
  font-weight: 700;
}

.store-name strong {
  color: #273149;
  font-size: 13px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 2px 18px;
}

.wide-field {
  grid-column: 1 / -1;
}

.full-width,
.category-store-filter {
  width: 260px;
}

.pending-panel {
  display: grid;
  min-height: 270px;
  place-items: center;
  align-content: center;
  text-align: center;
}

.pending-icon {
  display: grid;
  width: 58px;
  height: 58px;
  place-items: center;
  border-radius: 17px;
  background: #fff0e2;
  color: #b96826;
  font-size: 22px;
  font-weight: 700;
}

.pending-panel h3 {
  margin: 16px 0 8px;
  color: #243048;
}

.pending-panel p {
  margin: 0;
  color: #8b96a8;
  font-size: 12px;
}

@media (max-width: 1100px) {
  .section-tabs {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
