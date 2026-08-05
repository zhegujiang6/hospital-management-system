<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDepartmentOptions } from '@/api/department'
import {
  createDoctor,
  deleteDoctor,
  getDoctorDetail,
  getDoctorList,
  updateDoctor,
} from '@/api/doctor'

const doctors = ref([])
const departments = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const submitting = ref(false)
const formRef = ref()

const filters = reactive({
  keyword: '',
  departmentId: '',
  status: '',
})

const form = reactive({
  id: null,
  doctorNo: '',
  departmentId: null,
  name: '',
  gender: 1,
  title: '',
  phone: '',
  specialty: '',
  introduction: '',
  status: 1,
})

const rules = {
  doctorNo: [
    { required: true, message: '请输入医生工号', trigger: 'blur' },
    { max: 30, message: '医生工号不能超过30个字符', trigger: 'blur' },
    {
      pattern: /^[A-Za-z0-9-]+$/,
      message: '医生工号只能包含字母、数字和横线',
      trigger: 'blur',
    },
  ],
  departmentId: [{ required: true, message: '请选择所属科室', trigger: 'change' }],
  name: [
    { required: true, message: '请输入医生姓名', trigger: 'blur' },
    { max: 50, message: '医生姓名不能超过50个字符', trigger: 'blur' },
  ],
  gender: [{ required: true, message: '请选择医生性别', trigger: 'change' }],
  title: [
    { required: true, message: '请输入医生职称', trigger: 'blur' },
    { max: 30, message: '医生职称不能超过30个字符', trigger: 'blur' },
  ],
  phone: [{ max: 11, message: '联系电话不能超过11个字符', trigger: 'blur' }],
  specialty: [{ max: 255, message: '擅长领域不能超过255个字符', trigger: 'blur' }],
  introduction: [{ max: 500, message: '医生简介不能超过500个字符', trigger: 'blur' }],
  status: [{ required: true, message: '请选择医生状态', trigger: 'change' }],
}

const enabledDepartments = computed(() =>
  departments.value.filter((item) => item.status === 1),
)

const filteredDoctors = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()

  return doctors.value.filter((item) => {
    const matchesKeyword =
      !keyword ||
      item.doctorNo?.toLowerCase().includes(keyword) ||
      item.name?.toLowerCase().includes(keyword) ||
      item.title?.toLowerCase().includes(keyword) ||
      item.departmentName?.toLowerCase().includes(keyword) ||
      item.specialty?.toLowerCase().includes(keyword) ||
      item.phone?.includes(keyword)
    const matchesDepartment =
      filters.departmentId === '' || item.departmentId === filters.departmentId
    const matchesStatus = filters.status === '' || item.status === filters.status

    return matchesKeyword && matchesDepartment && matchesStatus
  })
})

const statistics = computed(() => ({
  total: doctors.value.length,
  enabled: doctors.value.filter((item) => item.status === 1).length,
  disabled: doctors.value.filter((item) => item.status === 0).length,
}))

const dialogTitle = computed(() => (dialogMode.value === 'create' ? '新增医生' : '编辑医生'))

async function loadPageData() {
  loading.value = true

  try {
    const [doctorData, departmentData] = await Promise.all([
      getDoctorList(),
      getDepartmentOptions(),
    ])
    doctors.value = doctorData || []
    departments.value = departmentData || []
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.keyword = ''
  filters.departmentId = ''
  filters.status = ''
}

async function openCreateDialog() {
  dialogMode.value = 'create'
  Object.assign(form, {
    id: null,
    doctorNo: '',
    departmentId: enabledDepartments.value[0]?.id ?? null,
    name: '',
    gender: 1,
    title: '',
    phone: '',
    specialty: '',
    introduction: '',
    status: 1,
  })
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

async function openEditDialog(row) {
  try {
    const detail = await getDoctorDetail(row.id)
    dialogMode.value = 'edit'
    Object.assign(form, {
      id: detail.id,
      doctorNo: detail.doctorNo,
      departmentId: detail.departmentId,
      name: detail.name,
      gender: detail.gender,
      title: detail.title,
      phone: detail.phone || '',
      specialty: detail.specialty || '',
      introduction: detail.introduction || '',
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

  submitting.value = true
  const payload = {
    doctorNo: form.doctorNo.trim(),
    departmentId: form.departmentId,
    name: form.name.trim(),
    gender: form.gender,
    title: form.title.trim(),
    phone: form.phone.trim() || null,
    specialty: form.specialty.trim() || null,
    introduction: form.introduction.trim() || null,
  }

  try {
    if (dialogMode.value === 'create') {
      await createDoctor(payload)
      ElMessage.success('医生新增成功')
    } else {
      await updateDoctor(form.id, {
        ...payload,
        status: form.status,
      })
      ElMessage.success('医生修改成功')
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
    await ElMessageBox.confirm(`确定删除医生“${row.name}”吗？`, '删除确认', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }

  try {
    await deleteDoctor(row.id)
    ElMessage.success('医生删除成功')
    await loadPageData()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

function formatGender(value) {
  return value === 1 ? '男' : '女'
}

onMounted(loadPageData)
</script>

<template>
  <section class="content">
    <div class="welcome-strip doctor-welcome">
      <div>
        <span class="eyebrow">DOCTOR CENTER</span>
        <h2>维护医生档案与执业信息</h2>
        <p>医生必须归属于一个启用的科室，页面数据来自医生与科室的联表查询。</p>
      </div>
      <button class="primary-action" type="button" @click="openCreateDialog">
        <span>＋</span>
        新增医生
      </button>
    </div>

    <div class="stats-grid">
      <article class="stat-card">
        <div class="stat-icon stat-icon-blue">医</div>
        <div>
          <span>医生总数</span>
          <strong>{{ statistics.total }}</strong>
        </div>
      </article>
      <article class="stat-card">
        <div class="stat-icon stat-icon-green">在</div>
        <div>
          <span>正常在岗</span>
          <strong>{{ statistics.enabled }}</strong>
        </div>
      </article>
      <article class="stat-card">
        <div class="stat-icon stat-icon-gray">停</div>
        <div>
          <span>已停用</span>
          <strong>{{ statistics.disabled }}</strong>
        </div>
      </article>
    </div>

    <div class="panel">
      <div class="panel-heading">
        <div>
          <h3>医生列表</h3>
          <p>共 {{ filteredDoctors.length }} 条符合条件的数据</p>
        </div>
        <el-button plain @click="loadPageData">刷新数据</el-button>
      </div>

      <div class="filters doctor-filters">
        <el-input
          v-model="filters.keyword"
          clearable
          placeholder="搜索姓名、工号、职称或专长"
          class="keyword-input"
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
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="filteredDoctors"
        row-key="id"
        class="department-table"
        empty-text="暂无医生数据"
      >
        <el-table-column prop="doctorNo" label="医生工号" width="130" />
        <el-table-column label="医生信息" min-width="170">
          <template #default="{ row }">
            <div class="doctor-identity">
              <span class="doctor-avatar">{{ row.name?.slice(0, 1) }}</span>
              <div>
                <strong>{{ row.name }}</strong>
                <small>{{ row.title }}</small>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="departmentName" label="所属科室" min-width="130" />
        <el-table-column label="性别" width="75">
          <template #default="{ row }">{{ formatGender(row.gender) }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="135">
          <template #default="{ row }">{{ row.phone || '—' }}</template>
        </el-table-column>
        <el-table-column label="擅长领域" min-width="190">
          <template #default="{ row }">
            <span class="specialty-text">{{ row.specialty || '暂未填写' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="95">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light" round>
              {{ row.status === 1 ? '启用' : '停用' }}
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
        {{ dialogMode === 'create' ? '填写医生的基本档案，并为其选择所属科室。' : '修改医生档案、所属科室与启停状态。' }}
      </p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="doctor-form-grid">
          <el-form-item label="医生工号" prop="doctorNo">
            <el-input v-model="form.doctorNo" maxlength="30" placeholder="例如：DOC001" />
          </el-form-item>
          <el-form-item label="所属科室" prop="departmentId">
            <el-select v-model="form.departmentId" placeholder="请选择科室" style="width: 100%">
              <el-option
                v-for="item in enabledDepartments"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="医生姓名" prop="name">
            <el-input v-model="form.name" maxlength="50" placeholder="请输入医生姓名" />
          </el-form-item>
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="form.gender">
              <el-radio-button :value="1">男</el-radio-button>
              <el-radio-button :value="0">女</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="医生职称" prop="title">
            <el-input v-model="form.title" maxlength="30" placeholder="例如：主任医师" />
          </el-form-item>
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="form.phone" maxlength="11" placeholder="请输入联系电话" />
          </el-form-item>
        </div>

        <el-form-item label="擅长领域" prop="specialty">
          <el-input
            v-model="form.specialty"
            maxlength="255"
            show-word-limit
            placeholder="例如：高血压、冠心病的诊断与治疗"
          />
        </el-form-item>

        <el-form-item label="医生简介" prop="introduction">
          <el-input
            v-model="form.introduction"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="简要填写医生的从业经历与专业介绍"
          />
        </el-form-item>

        <el-form-item v-if="dialogMode === 'edit'" label="医生状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio-button :value="1">启用</el-radio-button>
            <el-radio-button :value="0">停用</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">
          {{ dialogMode === 'create' ? '确认新增' : '保存修改' }}
        </el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.doctor-welcome {
  background:
    radial-gradient(circle at 82% 45%, rgba(255, 255, 255, 0.16) 0 4%, transparent 4.5%),
    radial-gradient(circle at 88% 42%, rgba(255, 255, 255, 0.1) 0 12%, transparent 12.5%),
    linear-gradient(125deg, #176d75, #168a8c 55%, #2e8bc0);
}

.doctor-filters {
  flex-wrap: wrap;
}

.filter-select {
  width: 160px;
}

.doctor-identity {
  display: flex;
  align-items: center;
  gap: 10px;
}

.doctor-avatar {
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

.doctor-identity strong,
.doctor-identity small {
  display: block;
}

.doctor-identity strong {
  color: #273149;
  font-size: 13px;
}

.doctor-identity small {
  margin-top: 3px;
  color: #8c96a8;
  font-size: 11px;
}

.specialty-text {
  display: -webkit-box;
  overflow: hidden;
  color: #7d8799;
  font-size: 12px;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.doctor-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 18px;
}
</style>
