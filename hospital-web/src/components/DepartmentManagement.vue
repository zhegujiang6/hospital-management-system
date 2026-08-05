<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createDepartment,
  deleteDepartment,
  getDepartmentDetail,
  getDepartmentList,
  updateDepartment,
} from '@/api/department'

const departments = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const submitting = ref(false)
const formRef = ref()

const filters = reactive({
  keyword: '',
  status: '',
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
})

const form = reactive({
  id: null,
  name: '',
  description: '',
  status: 1,
})

const rules = {
  name: [
    { required: true, message: '请输入科室名称', trigger: 'blur' },
    { max: 50, message: '科室名称不能超过50个字符', trigger: 'blur' },
  ],
  description: [{ max: 255, message: '科室简介不能超过255个字符', trigger: 'blur' }],
  status: [{ required: true, message: '请选择科室状态', trigger: 'change' }],
}

const statistics = computed(() => ({
  total: pagination.total,
  enabled: departments.value.filter((item) => item.status === 1).length,
  disabled: departments.value.filter((item) => item.status === 0).length,
}))

const dialogTitle = computed(() => (dialogMode.value === 'create' ? '新增科室' : '编辑科室'))

async function loadDepartments() {
  loading.value = true

  try {
    const params = {
      pageNo: pagination.current,
      pageSize: pagination.size,
    }

    const keyword = filters.keyword.trim()
    if (keyword) params.keyword = keyword
    if (filters.status !== '') params.status = filters.status

    const page = (await getDepartmentList(params)) || {}

    departments.value = page.records || []
    pagination.total = Number(page.total) || 0
    pagination.current = Number(page.current) || pagination.current
    pagination.size = Number(page.size) || pagination.size
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  loadDepartments()
}

function handleCurrentChange(pageNo) {
  pagination.current = pageNo
  loadDepartments()
}

function handleSizeChange(pageSize) {
  pagination.size = pageSize
  pagination.current = 1
  loadDepartments()
}

function resetFilters() {
  filters.keyword = ''
  filters.status = ''
  pagination.current = 1
  loadDepartments()
}

async function openCreateDialog() {
  dialogMode.value = 'create'
  Object.assign(form, {
    id: null,
    name: '',
    description: '',
    status: 1,
  })
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

async function openEditDialog(row) {
  try {
    const detail = await getDepartmentDetail(row.id)
    dialogMode.value = 'edit'
    Object.assign(form, {
      id: detail.id,
      name: detail.name,
      description: detail.description || '',
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

  try {
    if (dialogMode.value === 'create') {
      await createDepartment({
        name: form.name.trim(),
        description: form.description.trim() || null,
      })
      ElMessage.success('科室新增成功')
      pagination.current = 1
    } else {
      await updateDepartment(form.id, {
        name: form.name.trim(),
        description: form.description.trim() || null,
        status: form.status,
      })
      ElMessage.success('科室修改成功')
    }

    dialogVisible.value = false
    await loadDepartments()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除科室“${row.name}”吗？`, '删除确认', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }

  try {
    await deleteDepartment(row.id)
    ElMessage.success('科室删除成功')

    if (departments.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }

    await loadDepartments()
  } catch (error) {
    ElMessage.error(error.message)
  }
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

onMounted(loadDepartments)
</script>

<template>
  <section class="content">
    <div class="welcome-strip">
      <div>
        <span class="eyebrow">DEPARTMENT CENTER</span>
        <h2>维护医院科室基础信息</h2>
        <p>在这里管理科室名称、介绍和启停状态，为医生与排班模块提供基础数据。</p>
      </div>
      <button class="primary-action" type="button" @click="openCreateDialog">
        <span>＋</span>
        新增科室
      </button>
    </div>

    <div class="stats-grid">
      <article class="stat-card">
        <div class="stat-icon stat-icon-blue">全</div>
        <div>
          <span>符合条件</span>
          <strong>{{ statistics.total }}</strong>
        </div>
      </article>
      <article class="stat-card">
        <div class="stat-icon stat-icon-green">启</div>
        <div>
          <span>本页启用</span>
          <strong>{{ statistics.enabled }}</strong>
        </div>
      </article>
      <article class="stat-card">
        <div class="stat-icon stat-icon-gray">停</div>
        <div>
          <span>本页停用</span>
          <strong>{{ statistics.disabled }}</strong>
        </div>
      </article>
    </div>

    <div class="panel">
      <div class="panel-heading">
        <div>
          <h3>科室列表</h3>
          <p>共 {{ pagination.total }} 条符合条件的数据</p>
        </div>
        <el-button plain @click="loadDepartments">刷新数据</el-button>
      </div>

      <div class="filters">
        <el-input
          v-model="filters.keyword"
          clearable
          placeholder="搜索科室名称或简介"
          class="keyword-input"
          @keyup.enter="handleSearch"
        />
        <el-select
          v-model="filters.status"
          placeholder="全部状态"
          class="status-select"
          @change="handleSearch"
        >
          <el-option label="全部状态" value="" />
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="departments"
        row-key="id"
        class="department-table"
        empty-text="暂无科室数据"
      >
        <el-table-column prop="id" label="编号" width="90" />
        <el-table-column prop="name" label="科室名称" min-width="150">
          <template #default="{ row }">
            <div class="department-name">
              <span>{{ row.name.slice(0, 1) }}</span>
              <strong>{{ row.name }}</strong>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="科室简介" min-width="280">
          <template #default="{ row }">
            <span class="description-text">{{ row.description || '暂无简介' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
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
        <el-table-column label="操作" width="150" align="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[5, 10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      destroy-on-close
      align-center
    >
      <p class="dialog-description">
        {{ dialogMode === 'create' ? '填写信息后创建新的医院科室。' : '修改科室的基本信息与使用状态。' }}
      </p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="科室名称" prop="name">
          <el-input v-model="form.name" maxlength="50" show-word-limit placeholder="例如：心内科" />
        </el-form-item>

        <el-form-item label="科室简介" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            maxlength="255"
            show-word-limit
            placeholder="简要说明该科室负责的诊疗范围"
          />
        </el-form-item>

        <el-form-item v-if="dialogMode === 'edit'" label="科室状态" prop="status">
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
.table-pagination {
  display: flex;
  justify-content: flex-end;
  padding: 20px 24px 22px;
  border-top: 1px solid #edf1f6;
}
</style>
