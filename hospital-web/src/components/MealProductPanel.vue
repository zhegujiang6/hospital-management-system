<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getMealCategoryList } from '@/api/mealCategory'
import { createMealProduct, getMealProductList } from '@/api/mealProduct'
import { getMealStoreList } from '@/api/mealStore'

const stores = ref([])
const categories = ref([])
const products = ref([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref()

const filters = reactive({
  keyword: '',
  storeId: '',
  categoryId: '',
  status: '',
})

const form = reactive({
  categoryId: null,
  productNo: '',
  name: '',
  description: '',
  price: 0,
  imageUrl: '',
  dietaryTags: '',
  allergenInfo: '',
})

const rules = {
  categoryId: [{ required: true, message: '请选择所属分类', trigger: 'change' }],
  productNo: [
    { required: true, message: '请输入菜品编号', trigger: 'blur' },
    { max: 40, message: '菜品编号不能超过40个字符', trigger: 'blur' },
  ],
  name: [
    { required: true, message: '请输入菜品名称', trigger: 'blur' },
    { max: 100, message: '菜品名称不能超过100个字符', trigger: 'blur' },
  ],
  price: [{ required: true, message: '请输入菜品价格', trigger: 'change' }],
  description: [{ max: 1000, message: '菜品描述不能超过1000个字符', trigger: 'blur' }],
  imageUrl: [{ max: 500, message: '图片地址不能超过500个字符', trigger: 'blur' }],
  dietaryTags: [{ max: 255, message: '饮食标签不能超过255个字符', trigger: 'blur' }],
  allergenInfo: [{ max: 500, message: '过敏原信息不能超过500个字符', trigger: 'blur' }],
}

const filterCategories = computed(() => {
  if (!filters.storeId) return categories.value
  return categories.value.filter((category) => category.storeId === filters.storeId)
})

const enabledCategories = computed(() =>
  categories.value.filter((category) => category.status === 'ENABLED'),
)

const filteredProducts = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()
  if (!keyword) return products.value

  return products.value.filter((product) =>
    [product.productNo, product.name, product.categoryName, product.storeName, product.dietaryTags]
      .filter(Boolean)
      .some((value) => String(value).toLowerCase().includes(keyword)),
  )
})

async function loadReferenceData() {
  try {
    const [storeList, categoryList] = await Promise.all([
      getMealStoreList(),
      getMealCategoryList(),
    ])

    stores.value = storeList || []
    categories.value = categoryList || []
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function loadProducts() {
  loading.value = true

  try {
    products.value =
      (await getMealProductList({
        storeId: filters.storeId,
        categoryId: filters.categoryId,
        status: filters.status,
      })) || []
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

function handleStoreFilterChange() {
  if (
    filters.categoryId &&
    !filterCategories.value.some((category) => category.id === filters.categoryId)
  ) {
    filters.categoryId = ''
  }

  loadProducts()
}

function resetFilters() {
  Object.assign(filters, {
    keyword: '',
    storeId: '',
    categoryId: '',
    status: '',
  })
  loadProducts()
}

async function openCreateDialog() {
  Object.assign(form, {
    categoryId: enabledCategories.value[0]?.id || null,
    productNo: '',
    name: '',
    description: '',
    price: 0,
    imageUrl: '',
    dietaryTags: '',
    allergenInfo: '',
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
    await createMealProduct({
      categoryId: form.categoryId,
      productNo: form.productNo.trim(),
      name: form.name.trim(),
      description: form.description.trim() || null,
      price: form.price,
      imageUrl: form.imageUrl.trim() || null,
      dietaryTags: form.dietaryTags.trim() || null,
      allergenInfo: form.allergenInfo.trim() || null,
    })

    dialogVisible.value = false
    ElMessage.success('菜品创建成功')
    await loadProducts()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    submitting.value = false
  }
}

function formatPrice(value) {
  return Number(value || 0).toFixed(2)
}

onMounted(async () => {
  await loadReferenceData()
  await loadProducts()
})
</script>

<template>
  <div class="panel">
    <div class="panel-heading">
      <div>
        <h3>菜品列表</h3>
        <p>当前显示 {{ filteredProducts.length }} 条，共 {{ products.length }} 条数据</p>
      </div>
      <div class="heading-actions">
        <el-button plain @click="loadProducts">刷新数据</el-button>
        <el-button type="primary" @click="openCreateDialog">新增菜品</el-button>
      </div>
    </div>

    <div class="filters product-filters">
      <el-input
        v-model="filters.keyword"
        clearable
        placeholder="搜索菜品编号、名称、餐厅或标签"
        class="product-keyword"
      />
      <el-select
        v-model="filters.storeId"
        clearable
        placeholder="全部餐厅"
        @change="handleStoreFilterChange"
      >
        <el-option v-for="store in stores" :key="store.id" :label="store.name" :value="store.id" />
      </el-select>
      <el-select
        v-model="filters.categoryId"
        clearable
        placeholder="全部分类"
        @change="loadProducts"
      >
        <el-option
          v-for="category in filterCategories"
          :key="category.id"
          :label="category.name"
          :value="category.id"
        />
      </el-select>
      <el-select v-model="filters.status" clearable placeholder="全部状态" @change="loadProducts">
        <el-option label="已上架" value="ON_SALE" />
        <el-option label="已下架" value="OFF_SALE" />
      </el-select>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="filteredProducts"
      row-key="id"
      class="product-table"
      empty-text="暂无菜品数据"
    >
      <el-table-column prop="productNo" label="菜品编号" width="145" />
      <el-table-column label="菜品" min-width="225">
        <template #default="{ row }">
          <div class="product-name">
            <span>{{ row.name?.slice(0, 1) || '菜' }}</span>
            <div>
              <strong>{{ row.name }}</strong>
              <small>{{ row.description || '暂无描述' }}</small>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="所属位置" min-width="190">
        <template #default="{ row }">
          <strong class="location-primary">{{ row.categoryName }}</strong>
          <small class="location-secondary">{{ row.storeName }}</small>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="105">
        <template #default="{ row }"><strong class="price">¥{{ formatPrice(row.price) }}</strong></template>
      </el-table-column>
      <el-table-column prop="dietaryTags" label="饮食标签" min-width="145">
        <template #default="{ row }">{{ row.dietaryTags || '—' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="105">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ON_SALE' ? 'success' : 'info'" round>
            {{ row.status === 'ON_SALE' ? '已上架' : '已下架' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="新增菜品" width="680px" destroy-on-close align-center>
      <p class="dialog-description">填写菜品基础资料。每天各餐次的可售数量将在库存模块设置。</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="product-form-grid">
          <el-form-item label="所属分类" prop="categoryId">
            <el-select v-model="form.categoryId" class="full-width" filterable placeholder="请选择分类">
              <el-option
                v-for="category in enabledCategories"
                :key="category.id"
                :label="`${category.storeName} / ${category.name}`"
                :value="category.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="菜品编号" prop="productNo">
            <el-input v-model="form.productNo" maxlength="40" placeholder="例如：MEAL-002" />
          </el-form-item>
          <el-form-item label="菜品名称" prop="name">
            <el-input v-model="form.name" maxlength="100" placeholder="例如：低糖杂粮套餐" />
          </el-form-item>
          <el-form-item label="价格" prop="price">
            <el-input-number v-model="form.price" :min="0" :max="99999999.99" :precision="2" :step="0.5" />
          </el-form-item>
          <el-form-item label="菜品描述" prop="description" class="wide-field">
            <el-input v-model="form.description" type="textarea" :rows="3" maxlength="1000" show-word-limit />
          </el-form-item>
          <el-form-item label="饮食标签" prop="dietaryTags">
            <el-input v-model="form.dietaryTags" maxlength="255" placeholder="例如：低盐,高蛋白" />
          </el-form-item>
          <el-form-item label="过敏原信息" prop="allergenInfo">
            <el-input v-model="form.allergenInfo" maxlength="500" placeholder="例如：含大豆制品" />
          </el-form-item>
          <el-form-item label="图片地址" prop="imageUrl" class="wide-field">
            <el-input v-model="form.imageUrl" maxlength="500" placeholder="选填，后续再接文件上传" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确认创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.heading-actions {
  display: flex;
  gap: 10px;
}

.product-filters :deep(.el-select) {
  width: 160px;
}

.product-keyword {
  width: 285px;
}

.product-table {
  width: 100%;
}

.product-table :deep(.el-table__header th) {
  height: 48px;
  background: #f8fafd !important;
  color: #667189;
}

.product-table :deep(.el-table__row td) {
  height: 72px;
}

.product-name {
  display: flex;
  align-items: center;
  gap: 11px;
}

.product-name > span {
  display: grid;
  width: 38px;
  height: 38px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 12px;
  background: #fff0e2;
  color: #ba6726;
  font-weight: 700;
}

.product-name strong,
.product-name small,
.location-primary,
.location-secondary {
  display: block;
}

.product-name strong,
.location-primary {
  color: #273149;
  font-size: 13px;
}

.product-name small,
.location-secondary {
  max-width: 250px;
  margin-top: 4px;
  overflow: hidden;
  color: #8c97a9;
  font-size: 11px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price {
  color: #c56525;
}

.product-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 2px 18px;
}

.wide-field {
  grid-column: 1 / -1;
}

.full-width {
  width: 100%;
}
</style>
