<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createPatient,
  deletePatient,
  getPatientDetail,
  getPatientList,
  updatePatient,
} from '@/api/patient'

const patients = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const submitting = ref(false)
const formRef = ref()

const filters = reactive({
  keyword: '',
  gender: '',
  status: '',
})

const form = reactive({
  id: null,
  name: '',
  gender: '男',
  idCard: '',
  phone: '',
  birthDate: '',
  address: '',
  status: 1,
})

const rules = {
  name: [
    { required: true, message: '请输入患者姓名', trigger: 'blur' },
    { max: 50, message: '患者姓名不能超过50个字符', trigger: 'blur' },
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    {
      pattern: /^(\d{15}|\d{17}[0-9Xx])$/,
      message: '请输入15位或18位身份证号',
      trigger: 'blur',
    },
  ],
  phone: [
    { pattern: /^$|^1\d{10}$/, message: '请输入正确的11位手机号', trigger: 'blur' },
  ],
  birthDate: [{ required: true, message: '请选择出生日期', trigger: 'change' }],
  address: [{ max: 255, message: '家庭住址不能超过255个字符', trigger: 'blur' }],
  status: [{ required: true, message: '请选择患者状态', trigger: 'change' }],
}

const filteredPatients = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()

  return patients.value.filter((item) => {
    const matchesKeyword =
      !keyword ||
      item.patientNo?.toLowerCase().includes(keyword) ||
      item.name?.toLowerCase().includes(keyword) ||
      item.idCard?.toLowerCase().includes(keyword) ||
      item.phone?.includes(keyword)
    const matchesGender = filters.gender === '' || item.gender === filters.gender
    const matchesStatus = filters.status === '' || item.status === filters.status

    return matchesKeyword && matchesGender && matchesStatus
  })
})

const statistics = computed(() => ({
  total: patients.value.length,
  enabled: patients.value.filter((item) => item.status === 1).length,
  disabled: patients.value.filter((item) => item.status === 0).length,
}))

const dialogTitle = computed(() => (dialogMode.value === 'create' ? '新增患者' : '编辑患者'))

async function loadPatients() {
  loading.value = true

  try {
    patients.value = (await getPatientList()) || []
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.keyword = ''
  filters.gender = ''
  filters.status = ''
}

async function openCreateDialog() {
  dialogMode.value = 'create'
  Object.assign(form, {
    id: null,
    name: '',
    gender: '男',
    idCard: '',
    phone: '',
    birthDate: '',
    address: '',
    status: 1,
  })
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

async function openEditDialog(row) {
  try {
    const detail = await getPatientDetail(row.id)
    dialogMode.value = 'edit'
    Object.assign(form, {
      id: detail.id,
      name: detail.name,
      gender: detail.gender,
      idCard: detail.idCard,
      phone: detail.phone || '',
      birthDate: detail.birthDate || '',
      address: detail.address || '',
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
    name: form.name.trim(),
    gender: form.gender,
    idCard: form.idCard.trim().toUpperCase(),
    phone: form.phone.trim(),
    birthDate: form.birthDate,
    address: form.address.trim() || null,
  }

  submitting.value = true

  try {
    if (dialogMode.value === 'create') {
      await createPatient(payload)
      ElMessage.success('患者档案创建成功')
    } else {
      await updatePatient(form.id, {
        ...payload,
        status: form.status,
      })
      ElMessage.success('患者档案修改成功')
    }

    dialogVisible.value = false
    await loadPatients()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除患者“${row.name}”的档案吗？该操作完成后无法在列表中恢复。`,
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
    await deletePatient(row.id)
    ElMessage.success('患者档案删除成功')
    await loadPatients()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

function maskIdCard(value) {
  if (!value || value.length < 10) return value || '—'
  return `${value.slice(0, 6)}********${value.slice(-4)}`
}

function calculateAge(value) {
  if (!value) return '—'

  const birthday = new Date(`${value}T00:00:00`)
  const today = new Date()
  let age = today.getFullYear() - birthday.getFullYear()
  const beforeBirthday =
    today.getMonth() < birthday.getMonth() ||
    (today.getMonth() === birthday.getMonth() && today.getDate() < birthday.getDate())

  if (beforeBirthday) age -= 1
  return `${age}岁`
}

function disableTodayAndFuture(date) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date >= today
}

onMounted(loadPatients)
</script>

<template>
  <section class="content">
    <div class="welcome-strip patient-welcome">
      <div>
        <span class="eyebrow">PATIENT CENTER</span>
        <h2>建立与维护患者基础档案</h2>
        <p>患者档案将作为后续挂号、接诊与病历记录的统一身份基础。</p>
      </div>
      <button class="primary-action" type="button" @click="openCreateDialog">
        <span>＋</span>
        新增患者
      </button>
    </div>

    <div class="stats-grid">
      <article class="stat-card">
        <div class="stat-icon stat-icon-blue">患</div>
        <div>
          <span>患者档案总数</span>
          <strong>{{ statistics.total }}</strong>
        </div>
      </article>
      <article class="stat-card">
        <div class="stat-icon stat-icon-green">正</div>
        <div>
          <span>正常档案</span>
          <strong>{{ statistics.enabled }}</strong>
        </div>
      </article>
      <article class="stat-card">
        <div class="stat-icon stat-icon-gray">停</div>
        <div>
          <span>已停用档案</span>
          <strong>{{ statistics.disabled }}</strong>
        </div>
      </article>
    </div>

    <div class="panel">
      <div class="panel-heading">
        <div>
          <h3>患者档案列表</h3>
          <p>共 {{ filteredPatients.length }} 条符合条件的数据</p>
        </div>
        <el-button plain @click="loadPatients">刷新数据</el-button>
      </div>

      <div class="filters patient-filters">
        <el-input
          v-model="filters.keyword"
          clearable
          placeholder="搜索患者编号、姓名、身份证或手机号"
          class="keyword-input patient-keyword"
        />
        <el-select v-model="filters.gender" placeholder="全部性别" class="status-select">
          <el-option label="全部性别" value="" />
          <el-option label="男" value="男" />
          <el-option label="女" value="女" />
        </el-select>
        <el-select v-model="filters.status" placeholder="全部状态" class="status-select">
          <el-option label="全部状态" value="" />
          <el-option label="正常" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="filteredPatients"
        row-key="id"
        class="department-table"
        empty-text="暂无患者数据"
      >
        <el-table-column prop="patientNo" label="患者编号" width="150" />
        <el-table-column label="患者信息" min-width="145">
          <template #default="{ row }">
            <div class="patient-identity">
              <span>{{ row.name?.slice(0, 1) }}</span>
              <div>
                <strong>{{ row.name }}</strong>
                <small>{{ row.gender }} · {{ calculateAge(row.birthDate) }}</small>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="身份证号" width="190">
          <template #default="{ row }">
            <span class="muted-text">{{ maskIdCard(row.idCard) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="130">
          <template #default="{ row }">{{ row.phone || '—' }}</template>
        </el-table-column>
        <el-table-column prop="address" label="家庭住址" min-width="180">
          <template #default="{ row }">
            <span class="address-text">{{ row.address || '暂未填写' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="95">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light" round>
              {{ row.status === 1 ? '正常' : '停用' }}
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
      width="720px"
      destroy-on-close
      align-center
    >
      <p class="dialog-description">
        {{ dialogMode === 'create' ? '填写患者身份与联系方式，系统将自动生成患者编号。' : '修改患者的基础档案与启停状态。' }}
      </p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="patient-form-grid">
          <el-form-item label="患者姓名" prop="name">
            <el-input v-model="form.name" maxlength="50" placeholder="请输入患者姓名" />
          </el-form-item>
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="form.gender">
              <el-radio-button value="男">男</el-radio-button>
              <el-radio-button value="女">女</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="身份证号" prop="idCard">
            <el-input v-model="form.idCard" maxlength="18" placeholder="请输入15位或18位身份证号" />
          </el-form-item>
          <el-form-item label="出生日期" prop="birthDate">
            <el-date-picker
              v-model="form.birthDate"
              type="date"
              value-format="YYYY-MM-DD"
              :disabled-date="disableTodayAndFuture"
              placeholder="请选择出生日期"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="form.phone" maxlength="11" placeholder="请输入11位手机号" />
          </el-form-item>
          <el-form-item v-if="dialogMode === 'edit'" label="患者状态" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio-button :value="1">正常</el-radio-button>
              <el-radio-button :value="0">停用</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </div>

        <el-form-item label="家庭住址" prop="address">
          <el-input
            v-model="form.address"
            type="textarea"
            :rows="3"
            maxlength="255"
            show-word-limit
            placeholder="请输入患者家庭住址"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">
          {{ dialogMode === 'create' ? '确认建档' : '保存修改' }}
        </el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.patient-welcome {
  background:
    radial-gradient(circle at 82% 45%, rgba(255, 255, 255, 0.16) 0 4%, transparent 4.5%),
    radial-gradient(circle at 88% 42%, rgba(255, 255, 255, 0.1) 0 12%, transparent 12.5%),
    linear-gradient(125deg, #6a4cc7, #7564d8 55%, #3b8ec1);
}

.patient-filters {
  flex-wrap: wrap;
}

.patient-keyword {
  width: 340px;
}

.patient-identity {
  display: flex;
  align-items: center;
  gap: 10px;
}

.patient-identity > span {
  display: grid;
  width: 34px;
  height: 34px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 50%;
  background: #f0ebff;
  color: #7656c9;
  font-size: 13px;
  font-weight: 700;
}

.patient-identity strong,
.patient-identity small {
  display: block;
}

.patient-identity strong {
  color: #273149;
  font-size: 13px;
}

.patient-identity small {
  margin-top: 3px;
  color: #8c96a8;
  font-size: 11px;
}

.muted-text,
.address-text {
  color: #7d8799;
  font-size: 12px;
}

.address-text {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.patient-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 18px;
}
</style>
