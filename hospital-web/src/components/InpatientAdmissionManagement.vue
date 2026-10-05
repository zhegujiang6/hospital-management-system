<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBedList } from '@/api/bed'
import {
  createInpatientAdmission,
  dischargeInpatientAdmission,
  getInpatientAdmissionDetail,
  getInpatientAdmissionList,
} from '@/api/inpatientAdmission'
import { getPatientList } from '@/api/patient'

const admissions = ref([])
const patients = ref([])
const beds = ref([])
const loading = ref(false)
const loadingOptions = ref(false)
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
  patientId: null,
  bedId: null,
  dietaryNotes: '',
})

const rules = {
  patientId: [{ required: true, message: '请选择患者', trigger: 'change' }],
  bedId: [{ required: true, message: '请选择空闲床位', trigger: 'change' }],
  dietaryNotes: [{ max: 500, message: '饮食注意事项不能超过500个字符', trigger: 'blur' }],
}

const availablePatients = computed(() => {
  const activePatientIds = new Set(
    admissions.value
      .filter((item) => item.status === 'ACTIVE')
      .map((item) => item.patientId),
  )

  return patients.value.filter(
    (patient) => patient.status === 1 && !activePatientIds.has(patient.id),
  )
})

const availableBeds = computed(() => beds.value.filter((bed) => bed.status === 'AVAILABLE'))

const filteredAdmissions = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()

  return admissions.value.filter((item) => {
    const matchesKeyword =
      !keyword ||
      [
        item.admissionNo,
        item.patientNo,
        item.patientName,
        item.departmentName,
        item.wardName,
        item.building,
        item.roomNo,
        item.bedNo,
      ]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(keyword))

    const matchesStatus = filters.status === '' || item.status === filters.status
    return matchesKeyword && matchesStatus
  })
})

const statistics = computed(() => ({
  total: admissions.value.length,
  active: admissions.value.filter((item) => item.status === 'ACTIVE').length,
  discharged: admissions.value.filter((item) => item.status === 'DISCHARGED').length,
  dietary: admissions.value.filter((item) => item.status === 'ACTIVE' && item.dietaryNotes).length,
}))

async function loadAdmissions() {
  loading.value = true

  try {
    admissions.value = (await getInpatientAdmissionList()) || []
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

async function loadOptions() {
  loadingOptions.value = true

  try {
    const [patientList, bedList] = await Promise.all([getPatientList(), getBedList()])
    patients.value = patientList || []
    beds.value = bedList || []
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loadingOptions.value = false
  }
}

async function openCreateDialog() {
  Object.assign(form, {
    patientId: null,
    bedId: null,
    dietaryNotes: '',
  })
  await loadOptions()
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true

  try {
    await createInpatientAdmission({
      patientId: form.patientId,
      bedId: form.bedId,
      dietaryNotes: form.dietaryNotes.trim() || null,
    })

    dialogVisible.value = false
    ElMessage.success('患者入院办理成功')
    await Promise.all([loadAdmissions(), loadOptions()])
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    submitting.value = false
  }
}

async function showDetail(row) {
  try {
    detail.value = await getInpatientAdmissionDetail(row.id)
    detailVisible.value = true
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function handleDischarge(row) {
  try {
    await ElMessageBox.confirm(
      `确定为患者“${row.patientName}”办理出院吗？对应床位将恢复为空闲状态。`,
      '办理出院',
      {
        confirmButtonText: '确认出院',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
  } catch {
    return
  }

  try {
    await dischargeInpatientAdmission(row.id)
    ElMessage.success('患者出院办理成功，床位已释放')
    await Promise.all([loadAdmissions(), loadOptions()])
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
  loadAdmissions()
  loadOptions()
})
</script>

<template>
  <section class="content">
    <div class="welcome-strip admission-welcome">
      <div>
        <span class="eyebrow">INPATIENT ADMISSION</span>
        <h2>让患者、床位与配送位置保持一致</h2>
        <p>办理入院时原子占用床位，饮食备注将为后续院内餐饮推荐与限制提供依据。</p>
      </div>
      <button class="primary-action" type="button" @click="openCreateDialog">
        <span>＋</span>
        办理入院
      </button>
    </div>

    <div class="stats-grid admission-stats">
      <article class="stat-card">
        <div class="stat-icon stat-icon-blue">档</div>
        <div><span>住院记录</span><strong>{{ statistics.total }}</strong></div>
      </article>
      <article class="stat-card">
        <div class="stat-icon stat-icon-green">住</div>
        <div><span>当前住院</span><strong>{{ statistics.active }}</strong></div>
      </article>
      <article class="stat-card">
        <div class="stat-icon stat-icon-gray">出</div>
        <div><span>已经出院</span><strong>{{ statistics.discharged }}</strong></div>
      </article>
      <article class="stat-card">
        <div class="stat-icon stat-icon-orange">食</div>
        <div><span>饮食备注</span><strong>{{ statistics.dietary }}</strong></div>
      </article>
    </div>

    <div class="panel">
      <div class="panel-heading">
        <div>
          <h3>住院记录</h3>
          <p>当前显示 {{ filteredAdmissions.length }} 条，共 {{ admissions.length }} 条数据</p>
        </div>
        <el-button plain @click="loadAdmissions">刷新数据</el-button>
      </div>

      <div class="filters">
        <el-input
          v-model="filters.keyword"
          clearable
          placeholder="搜索住院号、患者、病区或床位"
          class="keyword-input"
        />
        <el-select v-model="filters.status" class="status-select" placeholder="全部状态">
          <el-option label="全部状态" value="" />
          <el-option label="住院中" value="ACTIVE" />
          <el-option label="已出院" value="DISCHARGED" />
        </el-select>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="filteredAdmissions"
        row-key="id"
        class="admission-table"
        empty-text="暂无住院记录"
      >
        <el-table-column prop="admissionNo" label="住院号" width="235" />
        <el-table-column label="患者" min-width="150">
          <template #default="{ row }">
            <div class="patient-info">
              <span>{{ row.patientName?.slice(0, 1) || '患' }}</span>
              <div>
                <strong>{{ row.patientName }}</strong>
                <small>{{ row.patientNo }}</small>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="住院位置" min-width="220">
          <template #default="{ row }">
            <strong class="location-main">{{ row.wardName }} · {{ row.roomNo }}室</strong>
            <span class="location-sub">{{ row.building }} {{ row.floorNo }} · {{ row.bedNo }}床</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="105">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" effect="light" round>
              {{ row.status === 'ACTIVE' ? '住院中' : '已出院' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="入院时间" width="175">
          <template #default="{ row }">
            <span class="date-text">{{ formatDate(row.admittedAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">查看</el-button>
            <el-button
              v-if="row.status === 'ACTIVE'"
              link
              type="danger"
              @click="handleDischarge(row)"
            >
              办理出院
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" title="办理患者入院" width="620px" destroy-on-close align-center>
      <p class="dialog-description">提交成功后，所选床位将自动从空闲状态变为已占用。</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="患者" prop="patientId">
          <el-select
            v-model="form.patientId"
            :loading="loadingOptions"
            filterable
            placeholder="请选择未住院的启用患者"
            class="full-width"
          >
            <el-option
              v-for="patient in availablePatients"
              :key="patient.id"
              :label="`${patient.name}（${patient.patientNo}）`"
              :value="patient.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="空闲床位" prop="bedId">
          <el-select
            v-model="form.bedId"
            :loading="loadingOptions"
            filterable
            placeholder="请选择空闲床位"
            class="full-width"
          >
            <el-option
              v-for="bed in availableBeds"
              :key="bed.id"
              :label="`${bed.wardName} · ${bed.roomNo}室 ${bed.bedNo}床`"
              :value="bed.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="饮食注意事项" prop="dietaryNotes">
          <el-input
            v-model="form.dietaryNotes"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="例如：低盐饮食、花生过敏；没有可不填写"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确认入院</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="住院详情" width="650px" align-center>
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="住院号" :span="2">{{ detail.admissionNo }}</el-descriptions-item>
        <el-descriptions-item label="患者姓名">{{ detail.patientName }}</el-descriptions-item>
        <el-descriptions-item label="患者编号">{{ detail.patientNo }}</el-descriptions-item>
        <el-descriptions-item label="所属科室">{{ detail.departmentName }}</el-descriptions-item>
        <el-descriptions-item label="所在病区">{{ detail.wardName }}</el-descriptions-item>
        <el-descriptions-item label="楼栋楼层">{{ detail.building }} {{ detail.floorNo }}</el-descriptions-item>
        <el-descriptions-item label="房间床位">{{ detail.roomNo }}室 {{ detail.bedNo }}床</el-descriptions-item>
        <el-descriptions-item label="入院时间">{{ formatDate(detail.admittedAt) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.status === 'ACTIVE' ? '住院中' : '已出院' }}</el-descriptions-item>
        <el-descriptions-item label="饮食注意事项" :span="2">{{ detail.dietaryNotes || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </section>
</template>

<style scoped>
.admission-welcome {
  background:
    radial-gradient(circle at 85% 40%, rgba(255, 255, 255, 0.14) 0 11%, transparent 11.5%),
    linear-gradient(125deg, #176d64, #258d80 55%, #38a392);
}

.admission-stats {
  grid-template-columns: repeat(4, 1fr);
}

.stat-icon-orange {
  background: #fff1df;
  color: #d98a25;
}

.admission-table {
  width: 100%;
}

.admission-table :deep(.el-table__header th) {
  height: 48px;
  background: #f8fafd !important;
  color: #667189;
}

.admission-table :deep(.el-table__row td) {
  height: 70px;
}

.patient-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.patient-info > span {
  display: grid;
  width: 35px;
  height: 35px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 50%;
  background: #e4f7f1;
  color: #16856d;
  font-size: 13px;
  font-weight: 700;
}

.patient-info strong,
.patient-info small,
.location-main,
.location-sub {
  display: block;
}

.patient-info strong,
.location-main {
  color: #273149;
  font-size: 13px;
}

.patient-info small,
.location-sub {
  margin-top: 4px;
  color: #8c97a9;
  font-size: 11px;
}

.full-width {
  width: 100%;
}
</style>
