<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getDepartmentOptions } from '@/api/department'
import { createWard, getWardDetail, getWardList } from '@/api/ward'

const wards = ref([])
const departments = ref([])
const loading = ref(false)
const loadingDepartments = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const detail = ref(null)
const formRef = ref()

const filters = reactive({
  keyword: '',
  status: '',
})

const form = reactive({
  wardNo: '',
  departmentId: null,
  name: '',
  building: '',
  floorNo: '',
})

const rules = {
  wardNo: [
    { required: true, message: '请输入病区编号', trigger: 'blur' },
    { max: 30, message: '病区编号不能超过30个字符', trigger: 'blur' },
  ],
  departmentId: [{ required: true, message: '请选择所属科室', trigger: 'change' }],
  name: [
    { required: true, message: '请输入病区名称', trigger: 'blur' },
    { max: 100, message: '病区名称不能超过100个字符', trigger: 'blur' },
  ],
  building: [
    { required: true, message: '请输入所在楼栋', trigger: 'blur' },
    { max: 100, message: '楼栋名称不能超过100个字符', trigger: 'blur' },
  ],
  floorNo: [
    { required: true, message: '请输入所在楼层', trigger: 'blur' },
    { max: 20, message: '楼层不能超过20个字符', trigger: 'blur' },
  ],
}

const filteredWards = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()

  return wards.value.filter((ward) => {
    const matchesKeyword =
      !keyword ||
      [ward.wardNo, ward.name, ward.departmentName, ward.building, ward.floorNo]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(keyword))

    const matchesStatus = filters.status === '' || ward.status === filters.status

    return matchesKeyword && matchesStatus
  })
})

const statistics = computed(() => ({
  total: wards.value.length,
  enabled: wards.value.filter((ward) => ward.status === 1).length,
  buildings: new Set(wards.value.map((ward) => ward.building).filter(Boolean)).size,
}))

async function loadWards() {
  loading.value = true

  try {
    wards.value = (await getWardList()) || []
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

async function loadDepartments() {
  loadingDepartments.value = true

  try {
    departments.value = await getDepartmentOptions()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loadingDepartments.value = false
  }
}

async function openCreateDialog() {
  Object.assign(form, {
    wardNo: '',
    departmentId: null,
    name: '',
    building: '',
    floorNo: '',
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
    await createWard({
      wardNo: form.wardNo.trim(),
      departmentId: form.departmentId,
      name: form.name.trim(),
      building: form.building.trim(),
      floorNo: form.floorNo.trim(),
    })

    dialogVisible.value = false
    ElMessage.success('病区创建成功')
    await loadWards()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    submitting.value = false
  }
}

async function showDetail(row) {
  try {
    detail.value = await getWardDetail(row.id)
    detailVisible.value = true
  } catch (error) {
    ElMessage.error(error.message)
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
  loadWards()
  loadDepartments()
})
</script>

<template>
  <section class="content">
    <div class="welcome-strip ward-welcome">
      <div>
        <span class="eyebrow">INPATIENT LOCATION</span>
        <h2>维护住院病区与院内配送位置</h2>
        <p>病区关联科室、楼栋与楼层，后续床位、住院记录和餐饮订单都以这里的数据为基础。</p>
      </div>
      <button class="primary-action" type="button" @click="openCreateDialog">
        <span>＋</span>
        新增病区
      </button>
    </div>

    <div class="stats-grid">
      <article class="stat-card">
        <div class="stat-icon stat-icon-blue">区</div>
        <div>
          <span>病区总数</span>
          <strong>{{ statistics.total }}</strong>
        </div>
      </article>
      <article class="stat-card">
        <div class="stat-icon stat-icon-green">启</div>
        <div>
          <span>启用病区</span>
          <strong>{{ statistics.enabled }}</strong>
        </div>
      </article>
      <article class="stat-card">
        <div class="stat-icon stat-icon-gray">楼</div>
        <div>
          <span>覆盖楼栋</span>
          <strong>{{ statistics.buildings }}</strong>
        </div>
      </article>
    </div>

    <div class="panel">
      <div class="panel-heading">
        <div>
          <h3>病区列表</h3>
          <p>当前显示 {{ filteredWards.length }} 条，共 {{ wards.length }} 条数据</p>
        </div>
        <el-button plain @click="loadWards">刷新数据</el-button>
      </div>

      <div class="filters">
        <el-input
          v-model="filters.keyword"
          clearable
          placeholder="搜索编号、名称、科室或位置"
          class="keyword-input"
        />
        <el-select v-model="filters.status" class="status-select" placeholder="全部状态">
          <el-option label="全部状态" value="" />
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="filteredWards"
        row-key="id"
        class="ward-table"
        empty-text="暂无病区数据"
      >
        <el-table-column prop="wardNo" label="病区编号" width="135" />
        <el-table-column label="病区名称" min-width="180">
          <template #default="{ row }">
            <div class="ward-name">
              <span>{{ row.name?.slice(0, 1) || '区' }}</span>
              <div>
                <strong>{{ row.name }}</strong>
                <small>{{ row.departmentName }}</small>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="院内位置" min-width="180">
          <template #default="{ row }">
            <strong class="location-text">{{ row.building }}</strong>
            <span class="floor-text">{{ row.floorNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light" round>
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="175">
          <template #default="{ row }">
            <span class="date-text">{{ formatDate(row.updateTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" title="新增病区" width="600px" destroy-on-close align-center>
      <p class="dialog-description">填写病区的组织归属和院内位置，创建后即可继续配置床位。</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid">
          <el-form-item label="病区编号" prop="wardNo">
            <el-input v-model="form.wardNo" maxlength="30" placeholder="例如：CARD-01" />
          </el-form-item>
          <el-form-item label="所属科室" prop="departmentId">
            <el-select
              v-model="form.departmentId"
              :loading="loadingDepartments"
              filterable
              placeholder="请选择启用中的科室"
              class="full-width"
            >
              <el-option
                v-for="department in departments"
                :key="department.id"
                :label="department.name"
                :value="department.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="病区名称" prop="name" class="wide-field">
            <el-input v-model="form.name" maxlength="100" placeholder="例如：心内科一病区" />
          </el-form-item>
          <el-form-item label="所在楼栋" prop="building">
            <el-input v-model="form.building" maxlength="100" placeholder="例如：住院部A栋" />
          </el-form-item>
          <el-form-item label="所在楼层" prop="floorNo">
            <el-input v-model="form.floorNo" maxlength="20" placeholder="例如：3F" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确认创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="病区详情" width="560px" align-center>
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="病区编号">{{ detail.wardNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.status === 1 ? '启用' : '停用' }}</el-descriptions-item>
        <el-descriptions-item label="病区名称" :span="2">{{ detail.name }}</el-descriptions-item>
        <el-descriptions-item label="所属科室" :span="2">{{ detail.departmentName }}</el-descriptions-item>
        <el-descriptions-item label="所在楼栋">{{ detail.building }}</el-descriptions-item>
        <el-descriptions-item label="所在楼层">{{ detail.floorNo }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ formatDate(detail.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </section>
</template>

<style scoped>
.ward-welcome {
  background:
    radial-gradient(circle at 86% 38%, rgba(255, 255, 255, 0.14) 0 12%, transparent 12.5%),
    linear-gradient(125deg, #155f78, #217f9b 55%, #2a91a7);
}

.ward-table {
  width: 100%;
}

.ward-table :deep(.el-table__header th) {
  height: 48px;
  background: #f8fafd !important;
  color: #667189;
}

.ward-table :deep(.el-table__row td) {
  height: 68px;
}

.ward-name {
  display: flex;
  align-items: center;
  gap: 11px;
}

.ward-name > span {
  display: grid;
  width: 35px;
  height: 35px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 11px;
  background: #e7f7f5;
  color: #168b80;
  font-size: 13px;
  font-weight: 700;
}

.ward-name strong,
.ward-name small,
.location-text,
.floor-text {
  display: block;
}

.ward-name strong,
.location-text {
  color: #273149;
  font-size: 13px;
}

.ward-name small,
.floor-text {
  margin-top: 4px;
  color: #8c97a9;
  font-size: 11px;
}

.form-grid {
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
