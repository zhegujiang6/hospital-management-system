<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createBed, getBedDetail, getBedList } from '@/api/bed'
import { getWardOptions } from '@/api/ward'

const beds = ref([])
const wards = ref([])
const loading = ref(false)
const loadingWards = ref(false)
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
  wardId: null,
  roomNo: '',
  bedNo: '',
})

const rules = {
  wardId: [{ required: true, message: '请选择所属病区', trigger: 'change' }],
  roomNo: [
    { required: true, message: '请输入房间号', trigger: 'blur' },
    { max: 30, message: '房间号不能超过30个字符', trigger: 'blur' },
  ],
  bedNo: [
    { required: true, message: '请输入床位号', trigger: 'blur' },
    { max: 30, message: '床位号不能超过30个字符', trigger: 'blur' },
  ],
}

const statusMeta = {
  AVAILABLE: { label: '空闲', type: 'success' },
  OCCUPIED: { label: '已占用', type: 'warning' },
  MAINTENANCE: { label: '维护中', type: 'info' },
}

const filteredBeds = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()

  return beds.value.filter((bed) => {
    const matchesKeyword =
      !keyword ||
      [bed.departmentName, bed.wardNo, bed.wardName, bed.roomNo, bed.bedNo]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(keyword))

    const matchesStatus = filters.status === '' || bed.status === filters.status

    return matchesKeyword && matchesStatus
  })
})

const statistics = computed(() => ({
  total: beds.value.length,
  available: beds.value.filter((bed) => bed.status === 'AVAILABLE').length,
  occupied: beds.value.filter((bed) => bed.status === 'OCCUPIED').length,
  maintenance: beds.value.filter((bed) => bed.status === 'MAINTENANCE').length,
}))

function getStatus(status) {
  return statusMeta[status] || { label: status || '未知', type: 'info' }
}

async function loadBeds() {
  loading.value = true

  try {
    beds.value = (await getBedList()) || []
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

async function loadWards() {
  loadingWards.value = true

  try {
    wards.value = await getWardOptions()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loadingWards.value = false
  }
}

async function openCreateDialog() {
  Object.assign(form, {
    wardId: null,
    roomNo: '',
    bedNo: '',
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
    await createBed({
      wardId: form.wardId,
      roomNo: form.roomNo.trim(),
      bedNo: form.bedNo.trim(),
    })

    dialogVisible.value = false
    ElMessage.success('床位创建成功')
    await loadBeds()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    submitting.value = false
  }
}

async function showDetail(row) {
  try {
    detail.value = await getBedDetail(row.id)
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
  loadBeds()
  loadWards()
})
</script>

<template>
  <section class="content">
    <div class="welcome-strip bed-welcome">
      <div>
        <span class="eyebrow">BED CAPACITY</span>
        <h2>掌握每一张床位的实时状态</h2>
        <p>床位状态将与患者入院、转床和出院事务同步，保证配送位置与住院信息一致。</p>
      </div>
      <button class="primary-action" type="button" @click="openCreateDialog">
        <span>＋</span>
        新增床位
      </button>
    </div>

    <div class="stats-grid bed-stats">
      <article class="stat-card">
        <div class="stat-icon stat-icon-blue">床</div>
        <div><span>床位总数</span><strong>{{ statistics.total }}</strong></div>
      </article>
      <article class="stat-card">
        <div class="stat-icon stat-icon-green">空</div>
        <div><span>当前空闲</span><strong>{{ statistics.available }}</strong></div>
      </article>
      <article class="stat-card">
        <div class="stat-icon stat-icon-orange">住</div>
        <div><span>已经占用</span><strong>{{ statistics.occupied }}</strong></div>
      </article>
      <article class="stat-card">
        <div class="stat-icon stat-icon-gray">修</div>
        <div><span>维护中</span><strong>{{ statistics.maintenance }}</strong></div>
      </article>
    </div>

    <div class="panel">
      <div class="panel-heading">
        <div>
          <h3>床位列表</h3>
          <p>当前显示 {{ filteredBeds.length }} 条，共 {{ beds.length }} 条数据</p>
        </div>
        <el-button plain @click="loadBeds">刷新数据</el-button>
      </div>

      <div class="filters">
        <el-input
          v-model="filters.keyword"
          clearable
          placeholder="搜索科室、病区、房间或床位"
          class="keyword-input"
        />
        <el-select v-model="filters.status" class="status-select" placeholder="全部状态">
          <el-option label="全部状态" value="" />
          <el-option label="空闲" value="AVAILABLE" />
          <el-option label="已占用" value="OCCUPIED" />
          <el-option label="维护中" value="MAINTENANCE" />
        </el-select>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="filteredBeds"
        row-key="id"
        class="bed-table"
        empty-text="暂无床位数据"
      >
        <el-table-column label="床位" width="125">
          <template #default="{ row }">
            <div class="bed-code">
              <strong>{{ row.roomNo }}</strong>
              <span>{{ row.bedNo }}床</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="所属病区" min-width="190">
          <template #default="{ row }">
            <div class="ward-info">
              <strong>{{ row.wardName }}</strong>
              <span>{{ row.wardNo }} · {{ row.departmentName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatus(row.status).type" effect="light" round>
              {{ getStatus(row.status).label }}
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

    <el-dialog v-model="dialogVisible" title="新增床位" width="540px" destroy-on-close align-center>
      <p class="dialog-description">新建床位默认处于空闲状态，占用状态只能通过办理入院产生。</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="所属病区" prop="wardId">
          <el-select
            v-model="form.wardId"
            :loading="loadingWards"
            filterable
            placeholder="请选择启用中的病区"
            class="full-width"
          >
            <el-option
              v-for="ward in wards"
              :key="ward.id"
              :label="`${ward.name}（${ward.wardNo}）`"
              :value="ward.id"
            />
          </el-select>
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="房间号" prop="roomNo">
            <el-input v-model="form.roomNo" maxlength="30" placeholder="例如：501" />
          </el-form-item>
          <el-form-item label="床位号" prop="bedNo">
            <el-input v-model="form.bedNo" maxlength="30" placeholder="例如：01" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确认创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="床位详情" width="540px" align-center>
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="病区编号">{{ detail.wardNo }}</el-descriptions-item>
        <el-descriptions-item label="所属科室">{{ detail.departmentName }}</el-descriptions-item>
        <el-descriptions-item label="病区名称" :span="2">{{ detail.wardName }}</el-descriptions-item>
        <el-descriptions-item label="房间号">{{ detail.roomNo }}</el-descriptions-item>
        <el-descriptions-item label="床位号">{{ detail.bedNo }}</el-descriptions-item>
        <el-descriptions-item label="状态" :span="2">{{ getStatus(detail.status).label }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ formatDate(detail.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </section>
</template>

<style scoped>
.bed-welcome {
  background:
    radial-gradient(circle at 84% 36%, rgba(255, 255, 255, 0.13) 0 11%, transparent 11.5%),
    linear-gradient(125deg, #3152a1, #4e70c9 55%, #667fc4);
}

.bed-stats {
  grid-template-columns: repeat(4, 1fr);
}

.stat-icon-orange {
  background: #fff1df;
  color: #d98a25;
}

.bed-table {
  width: 100%;
}

.bed-table :deep(.el-table__header th) {
  height: 48px;
  background: #f8fafd !important;
  color: #667189;
}

.bed-table :deep(.el-table__row td) {
  height: 68px;
}

.bed-code {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.bed-code strong {
  color: #2c4384;
  font-size: 16px;
}

.bed-code span,
.ward-info span {
  color: #8c97a9;
  font-size: 11px;
}

.ward-info strong,
.ward-info span {
  display: block;
}

.ward-info strong {
  margin-bottom: 5px;
  color: #273149;
  font-size: 13px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.full-width {
  width: 100%;
}
</style>
