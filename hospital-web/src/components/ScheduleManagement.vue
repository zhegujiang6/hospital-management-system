<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDoctorList } from '@/api/doctor'
import {
  createSchedule,
  deleteSchedule,
  getScheduleDetail,
  getScheduleList,
  updateSchedule,
} from '@/api/schedule'

const schedules = ref([])
const doctors = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const submitting = ref(false)
const formRef = ref()

const filters = reactive({
  keyword: '',
  scheduleDate: '',
  departmentId: '',
  status: '',
})

const form = reactive({
  id: null,
  doctorId: null,
  scheduleDate: '',
  period: 'MORNING',
  registrationFee: 20,
  totalSlots: 20,
  status: 1,
})

const rules = {
  doctorId: [{ required: true, message: '请选择出诊医生', trigger: 'change' }],
  scheduleDate: [{ required: true, message: '请选择排班日期', trigger: 'change' }],
  period: [{ required: true, message: '请选择排班时段', trigger: 'change' }],
  registrationFee: [
    { required: true, message: '请输入挂号费', trigger: 'blur' },
    { type: 'number', min: 0, message: '挂号费不能小于0', trigger: 'blur' },
  ],
  totalSlots: [
    { required: true, message: '请输入总号源数', trigger: 'blur' },
    { type: 'number', min: 1, max: 500, message: '号源数必须在1到500之间', trigger: 'blur' },
  ],
  status: [{ required: true, message: '请选择排班状态', trigger: 'change' }],
}

const enabledDoctors = computed(() => doctors.value.filter((item) => item.status === 1))

const departments = computed(() => {
  const uniqueDepartments = new Map()

  doctors.value.forEach((doctor) => {
    if (doctor.departmentId != null && doctor.departmentName) {
      uniqueDepartments.set(String(doctor.departmentId), {
        id: doctor.departmentId,
        name: doctor.departmentName,
      })
    }
  })

  return [...uniqueDepartments.values()]
})

const filteredSchedules = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()

  return schedules.value.filter((item) => {
    const matchesKeyword =
      !keyword ||
      item.doctorNo?.toLowerCase().includes(keyword) ||
      item.doctorName?.toLowerCase().includes(keyword) ||
      item.departmentName?.toLowerCase().includes(keyword)
    const matchesDate = !filters.scheduleDate || item.scheduleDate === filters.scheduleDate
    const matchesDepartment =
      filters.departmentId === '' || String(item.departmentId) === String(filters.departmentId)
    const matchesStatus = filters.status === '' || item.status === filters.status

    return matchesKeyword && matchesDate && matchesDepartment && matchesStatus
  })
})

const statistics = computed(() => ({
  total: schedules.value.length,
  active: schedules.value.filter((item) => item.status === 1).length,
  remaining: schedules.value.reduce((sum, item) => sum + (item.remainingSlots || 0), 0),
}))

const dialogTitle = computed(() => (dialogMode.value === 'create' ? '新增排班' : '编辑排班'))

async function loadPageData() {
  loading.value = true

  try {
    const [scheduleData, doctorData] = await Promise.all([
      getScheduleList(),
      getDoctorList(),
    ])
    schedules.value = scheduleData || []
    doctors.value = doctorData || []
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.keyword = ''
  filters.scheduleDate = ''
  filters.departmentId = ''
  filters.status = ''
}

function getTomorrow() {
  const date = new Date()
  date.setDate(date.getDate() + 1)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

async function openCreateDialog() {
  dialogMode.value = 'create'
  Object.assign(form, {
    id: null,
    doctorId: enabledDoctors.value[0]?.id ?? null,
    scheduleDate: getTomorrow(),
    period: 'MORNING',
    registrationFee: 20,
    totalSlots: 20,
    status: 1,
  })
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

async function openEditDialog(row) {
  try {
    const detail = await getScheduleDetail(row.id)
    dialogMode.value = 'edit'
    Object.assign(form, {
      id: detail.id,
      doctorId: detail.doctorId,
      scheduleDate: detail.scheduleDate,
      period: detail.period,
      registrationFee: Number(detail.registrationFee),
      totalSlots: detail.totalSlots,
      status: detail.status,
    })
    dialogVisible.value = true
    await nextTick()
    formRef.value?.clearValidate()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  const payload = {
    doctorId: form.doctorId,
    scheduleDate: form.scheduleDate,
    period: form.period,
    registrationFee: Number(form.registrationFee),
    totalSlots: Number(form.totalSlots),
  }

  submitting.value = true

  try {
    if (dialogMode.value === 'create') {
      await createSchedule(payload)
      ElMessage.success('排班创建成功')
    } else {
      await updateSchedule(form.id, {
        ...payload,
        status: form.status,
      })
      ElMessage.success('排班修改成功')
    }

    dialogVisible.value = false
    await loadPageData()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除${row.scheduleDate} ${formatPeriod(row.period)}的“${row.doctorName}”排班吗？`,
      '删除确认',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
  } catch {
    return
  }

  try {
    await deleteSchedule(row.id)
    ElMessage.success('排班删除成功')
    await loadPageData()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

function formatPeriod(value) {
  return value === 'MORNING' ? '上午' : '下午'
}

function formatMoney(value) {
  return `¥${Number(value || 0).toFixed(2)}`
}

function registeredSlots(row) {
  return Math.max(0, (row.totalSlots || 0) - (row.remainingSlots || 0))
}

function disablePastDates(date) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date < today
}

onMounted(loadPageData)
</script>

<template>
  <section class="content">
    <div class="welcome-strip schedule-welcome">
      <div>
        <span class="eyebrow">SCHEDULE CENTER</span>
        <h2>安排医生出诊与门诊号源</h2>
        <p>按医生、日期和时段维护排班，为下一步患者挂号提供可预约号源。</p>
      </div>
      <button class="primary-action" type="button" @click="openCreateDialog">
        <span>＋</span>
        新增排班
      </button>
    </div>

    <div class="stats-grid">
      <article class="stat-card">
        <div class="stat-icon stat-icon-blue">班</div>
        <div>
          <span>排班总数</span>
          <strong>{{ statistics.total }}</strong>
        </div>
      </article>
      <article class="stat-card">
        <div class="stat-icon stat-icon-green">诊</div>
        <div>
          <span>正常出诊</span>
          <strong>{{ statistics.active }}</strong>
        </div>
      </article>
      <article class="stat-card">
        <div class="stat-icon schedule-slots-icon">号</div>
        <div>
          <span>剩余号源</span>
          <strong>{{ statistics.remaining }}</strong>
        </div>
      </article>
    </div>

    <div class="panel">
      <div class="panel-heading">
        <div>
          <h3>出诊排班列表</h3>
          <p>共 {{ filteredSchedules.length }} 条符合条件的数据</p>
        </div>
        <el-button plain @click="loadPageData">刷新数据</el-button>
      </div>

      <div class="filters schedule-filters">
        <el-input
          v-model="filters.keyword"
          clearable
          placeholder="搜索医生工号、姓名或科室"
          class="keyword-input"
        />
        <el-date-picker
          v-model="filters.scheduleDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="全部日期"
          class="schedule-date-filter"
        />
        <el-select v-model="filters.departmentId" placeholder="全部科室" class="filter-select">
          <el-option label="全部科室" value="" />
          <el-option
            v-for="item in departments"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
        <el-select v-model="filters.status" placeholder="全部状态" class="status-select">
          <el-option label="全部状态" value="" />
          <el-option label="正常" :value="1" />
          <el-option label="停诊" :value="0" />
        </el-select>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="filteredSchedules"
        row-key="id"
        class="department-table"
        empty-text="暂无排班数据"
      >
        <el-table-column prop="scheduleDate" label="出诊日期" width="125" />
        <el-table-column label="出诊时段" width="105">
          <template #default="{ row }">
            <span :class="['period-badge', row.period === 'MORNING' ? 'morning' : 'afternoon']">
              {{ formatPeriod(row.period) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="医生信息" min-width="160">
          <template #default="{ row }">
            <div class="schedule-doctor">
              <span>{{ row.doctorName?.slice(0, 1) }}</span>
              <div>
                <strong>{{ row.doctorName }}</strong>
                <small>{{ row.doctorNo }}</small>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="departmentName" label="所属科室" min-width="125" />
        <el-table-column label="挂号费" width="100">
          <template #default="{ row }">{{ formatMoney(row.registrationFee) }}</template>
        </el-table-column>
        <el-table-column label="号源情况" min-width="165">
          <template #default="{ row }">
            <div class="slot-summary">
              <span>剩余 {{ row.remainingSlots }} / {{ row.totalSlots }}</span>
              <el-progress
                :percentage="row.totalSlots ? Math.round(registeredSlots(row) / row.totalSlots * 100) : 0"
                :show-text="false"
                :stroke-width="5"
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="95">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="light" round>
              {{ row.status === 1 ? '正常' : '停诊' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="135" align="right" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      destroy-on-close
      align-center
    >
      <p class="dialog-description">
        {{ dialogMode === 'create' ? '选择出诊医生、日期和时段，并配置门诊号源。' : '修改排班信息、号源数量或停诊状态。' }}
      </p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="schedule-form-grid">
          <el-form-item label="出诊医生" prop="doctorId">
            <el-select v-model="form.doctorId" filterable placeholder="请选择医生" style="width: 100%">
              <el-option
                v-for="item in enabledDoctors"
                :key="item.id"
                :label="`${item.name} · ${item.departmentName}`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="排班日期" prop="scheduleDate">
            <el-date-picker
              v-model="form.scheduleDate"
              type="date"
              value-format="YYYY-MM-DD"
              :disabled-date="disablePastDates"
              placeholder="请选择日期"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="出诊时段" prop="period">
            <el-radio-group v-model="form.period">
              <el-radio-button value="MORNING">上午</el-radio-button>
              <el-radio-button value="AFTERNOON">下午</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="挂号费" prop="registrationFee">
            <el-input-number
              v-model="form.registrationFee"
              :min="0"
              :max="99999999"
              :precision="2"
              :step="5"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="总号源数" prop="totalSlots">
            <el-input-number
              v-model="form.totalSlots"
              :min="1"
              :max="500"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item v-if="dialogMode === 'edit'" label="排班状态" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio-button :value="1">正常</el-radio-button>
              <el-radio-button :value="0">停诊</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">
          {{ dialogMode === 'create' ? '确认排班' : '保存修改' }}
        </el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.schedule-welcome {
  background:
    radial-gradient(circle at 82% 45%, rgba(255, 255, 255, 0.16) 0 4%, transparent 4.5%),
    radial-gradient(circle at 88% 42%, rgba(255, 255, 255, 0.1) 0 12%, transparent 12.5%),
    linear-gradient(125deg, #b65e22, #d58532 55%, #d6a337);
}

.schedule-slots-icon {
  background: #fff2de;
  color: #c57a20;
}

.schedule-filters {
  flex-wrap: wrap;
}

.schedule-date-filter {
  width: 160px !important;
}

.filter-select {
  width: 150px;
}

.period-badge {
  display: inline-flex;
  align-items: center;
  padding: 5px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.period-badge.morning {
  background: #fff1d8;
  color: #b66d15;
}

.period-badge.afternoon {
  background: #e8efff;
  color: #506fc2;
}

.schedule-doctor {
  display: flex;
  align-items: center;
  gap: 10px;
}

.schedule-doctor > span {
  display: grid;
  width: 34px;
  height: 34px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 50%;
  background: #e7f7f5;
  color: #168778;
  font-size: 13px;
  font-weight: 700;
}

.schedule-doctor strong,
.schedule-doctor small {
  display: block;
}

.schedule-doctor strong {
  color: #273149;
  font-size: 13px;
}

.schedule-doctor small {
  margin-top: 3px;
  color: #8c96a8;
  font-size: 11px;
}

.slot-summary {
  width: 125px;
}

.slot-summary > span {
  display: block;
  margin-bottom: 7px;
  color: #647087;
  font-size: 12px;
}

.schedule-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 18px;
}
</style>
