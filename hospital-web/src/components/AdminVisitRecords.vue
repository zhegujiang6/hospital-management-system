<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getAdminVisitRecordDetail,
  getAdminVisitRecordList,
} from '@/api/visitRecord'

const records = ref([])
const loading = ref(false)
const keyword = ref('')
const departmentName = ref('')
const visitDateRange = ref([])
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)

const departmentOptions = computed(() =>
  [...new Set(records.value.map((item) => item.departmentName).filter(Boolean))].sort(),
)

const statistics = computed(() => ({
  total: records.value.length,
  patientCount: new Set(records.value.map((item) => item.patientId).filter(Boolean)).size,
  doctorCount: new Set(records.value.map((item) => item.doctorId).filter(Boolean)).size,
  departmentCount: new Set(records.value.map((item) => item.departmentId).filter(Boolean)).size,
}))

const filteredRecords = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  const [startDate, endDate] = visitDateRange.value || []

  return records.value.filter((item) => {
    const matchesKeyword =
      !text ||
      [
        item.recordNo,
        item.orderNo,
        item.patientNo,
        item.patientName,
        item.doctorNo,
        item.doctorName,
        item.departmentName,
        item.diagnosis,
      ].some((value) => value?.toLowerCase().includes(text))

    const matchesDepartment =
      !departmentName.value || item.departmentName === departmentName.value

    const visitDate = item.visitTime?.slice(0, 10)
    const matchesDate =
      (!startDate || (visitDate && visitDate >= startDate)) &&
      (!endDate || (visitDate && visitDate <= endDate))

    return matchesKeyword && matchesDepartment && matchesDate
  })
})

async function loadRecords() {
  loading.value = true
  try {
    records.value = (await getAdminVisitRecordList()) || []
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

async function openDetail(row) {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null

  try {
    detail.value = await getAdminVisitRecordDetail(row.id)
  } catch (error) {
    detailVisible.value = false
    ElMessage.error(error.message)
  } finally {
    detailLoading.value = false
  }
}

function resetFilters() {
  keyword.value = ''
  departmentName.value = ''
  visitDateRange.value = []
}

function formatPeriod(value) {
  return value === 'MORNING' ? '上午' : value === 'AFTERNOON' ? '下午' : value || '—'
}

function formatDateTime(value) {
  if (!value) return '—'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value.replace('T', ' ')

  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  })
    .format(date)
    .replaceAll('/', '-')
}

onMounted(loadRecords)
</script>

<template>
  <section class="content">
    <div class="welcome-strip admin-records-welcome">
      <div>
        <span class="eyebrow">CLINICAL RECORDS</span>
        <h2>全院就诊记录</h2>
        <p>统一查看已经完成接诊的病历，快速定位患者、接诊医生和诊断结果。</p>
      </div>
      <div class="record-total">
        <strong>{{ statistics.total }}</strong>
        <span>份病历记录</span>
      </div>
    </div>

    <div class="record-statistics">
      <article>
        <span>就诊患者</span>
        <strong>{{ statistics.patientCount }}</strong>
        <small>已形成病历的患者</small>
      </article>
      <article>
        <span>接诊医生</span>
        <strong>{{ statistics.doctorCount }}</strong>
        <small>参与接诊的医生</small>
      </article>
      <article>
        <span>涉及科室</span>
        <strong>{{ statistics.departmentCount }}</strong>
        <small>产生病历的科室</small>
      </article>
    </div>

    <div class="panel records-panel">
      <div class="panel-heading">
        <div>
          <h3>病历列表</h3>
          <p>当前显示 {{ filteredRecords.length }} 条，共 {{ records.length }} 条记录</p>
        </div>
        <el-button plain :loading="loading" @click="loadRecords">刷新数据</el-button>
      </div>

      <div class="record-filters">
        <el-input
          v-model="keyword"
          clearable
          placeholder="搜索病历号、患者、医生或诊断"
          class="keyword-filter"
        />
        <el-select
          v-model="departmentName"
          clearable
          placeholder="全部科室"
          class="department-filter"
        >
          <el-option
            v-for="item in departmentOptions"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
        <el-date-picker
          v-model="visitDateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          range-separator="至"
          class="date-filter"
        />
        <el-button text @click="resetFilters">清空条件</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="filteredRecords"
        row-key="id"
        class="department-table"
        empty-text="暂无符合条件的就诊记录"
      >
        <el-table-column label="病历信息" min-width="210">
          <template #default="{ row }">
            <div class="record-identity">
              <strong>{{ row.recordNo }}</strong>
              <small>挂号单：{{ row.orderNo }}</small>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="患者" min-width="135">
          <template #default="{ row }">
            <div class="person-cell">
              <strong>{{ row.patientName }}</strong>
              <small>{{ row.patientNo }}</small>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="接诊医生" min-width="145">
          <template #default="{ row }">
            <div class="person-cell">
              <strong>{{ row.doctorName }}</strong>
              <small>{{ row.doctorTitle || row.doctorNo }}</small>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="departmentName" label="科室" min-width="110" />
        <el-table-column label="接诊时间" min-width="165">
          <template #default="{ row }">
            <span class="record-time">{{ formatDateTime(row.visitTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="diagnosis" label="诊断结果" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default>
            <el-tag type="success" effect="light" round>已完成</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="right" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看病历</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="detailVisible" title="就诊记录详情" width="780px" align-center>
      <div v-loading="detailLoading" class="record-detail">
        <template v-if="detail">
          <div class="detail-banner">
            <div>
              <span>病历编号</span>
              <strong>{{ detail.recordNo }}</strong>
            </div>
            <div class="detail-banner-order">
              <span>挂号订单</span>
              <strong>{{ detail.orderNo }}</strong>
            </div>
            <el-tag type="success" effect="light" round>已完成</el-tag>
          </div>

          <div class="detail-info-grid">
            <div><span>患者</span><strong>{{ detail.patientName }} · {{ detail.patientNo }}</strong></div>
            <div><span>性别</span><strong>{{ detail.patientGender || '—' }}</strong></div>
            <div><span>联系电话</span><strong>{{ detail.patientPhone || '—' }}</strong></div>
            <div><span>科室</span><strong>{{ detail.departmentName || '—' }}</strong></div>
            <div><span>接诊医生</span><strong>{{ detail.doctorName }} · {{ detail.doctorTitle }}</strong></div>
            <div><span>医生工号</span><strong>{{ detail.doctorNo || '—' }}</strong></div>
            <div><span>预约安排</span><strong>{{ detail.scheduleDate }} {{ formatPeriod(detail.period) }}</strong></div>
            <div><span>接诊时间</span><strong>{{ formatDateTime(detail.visitTime) }}</strong></div>
          </div>

          <div class="medical-sections">
            <article>
              <span>患者主诉</span>
              <p>{{ detail.chiefComplaint || '—' }}</p>
            </article>
            <article>
              <span>现病史</span>
              <p>{{ detail.presentIllness || '未填写' }}</p>
            </article>
            <article class="diagnosis-section">
              <span>诊断结果</span>
              <p>{{ detail.diagnosis || '—' }}</p>
            </article>
            <article>
              <span>治疗方案</span>
              <p>{{ detail.treatmentPlan || '未填写' }}</p>
            </article>
            <article>
              <span>医生嘱咐</span>
              <p>{{ detail.doctorAdvice || '未填写' }}</p>
            </article>
          </div>
        </template>
      </div>
    </el-dialog>
  </section>
</template>

<style scoped>
.admin-records-welcome {
  background: linear-gradient(125deg, #324b74, #426b92 58%, #3f7f83);
}

.record-total {
  display: flex;
  min-width: 128px;
  flex-direction: column;
  align-items: center;
  padding: 17px 22px;
  border: 1px solid rgba(255, 255, 255, 0.24);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.12);
}

.record-total strong {
  font-size: 30px;
}

.record-total span {
  margin-top: 3px;
  color: #dce8f3;
  font-size: 11px;
}

.record-statistics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-top: 18px;
}

.record-statistics article {
  padding: 17px 20px;
  border: 1px solid #e8edf4;
  border-radius: 13px;
  background: #fff;
}

.record-statistics span,
.record-statistics strong,
.record-statistics small {
  display: block;
}

.record-statistics span {
  color: #7c899b;
  font-size: 11px;
}

.record-statistics strong {
  margin-top: 5px;
  color: #26364f;
  font-size: 25px;
}

.record-statistics small {
  margin-top: 3px;
  color: #a0a8b5;
  font-size: 10px;
}

.records-panel {
  margin-top: 18px;
}

.record-filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.keyword-filter {
  width: 300px;
}

.department-filter {
  width: 150px;
}

.date-filter {
  width: 260px;
}

.record-identity strong,
.record-identity small,
.person-cell strong,
.person-cell small {
  display: block;
}

.record-identity strong {
  color: #405574;
  font-family: Consolas, monospace;
  font-size: 11px;
}

.record-identity small,
.person-cell small {
  margin-top: 4px;
  color: #929cab;
  font-size: 10px;
}

.person-cell strong {
  color: #26344c;
  font-size: 13px;
}

.record-time {
  color: #667287;
  font-size: 12px;
}

.record-detail {
  min-height: 170px;
}

.detail-banner {
  display: flex;
  align-items: center;
  gap: 28px;
  margin: -5px 0 18px;
  padding: 16px 18px;
  border-radius: 12px;
  background: #f2f6fc;
}

.detail-banner > div:first-child {
  flex: 1;
}

.detail-banner-order {
  min-width: 180px;
}

.detail-banner span,
.detail-banner strong,
.detail-info-grid span,
.detail-info-grid strong {
  display: block;
}

.detail-banner span,
.detail-info-grid span,
.medical-sections span {
  color: #8591a5;
  font-size: 10px;
}

.detail-banner strong {
  margin-top: 5px;
  color: #33496d;
  font-family: Consolas, monospace;
  font-size: 12px;
}

.detail-info-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 18px;
}

.detail-info-grid > div {
  padding: 13px 14px;
  border: 1px solid #e8edf5;
  border-radius: 10px;
}

.detail-info-grid strong {
  margin-top: 6px;
  color: #28354d;
  font-size: 11px;
}

.medical-sections {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.medical-sections article {
  min-height: 98px;
  padding: 16px;
  border-radius: 11px;
  background: #f8fafc;
}

.medical-sections .diagnosis-section {
  grid-column: 1 / -1;
  background: #edf6f4;
}

.medical-sections p {
  margin: 8px 0 0;
  color: #374359;
  font-size: 13px;
  line-height: 1.75;
  white-space: pre-wrap;
}

@media (max-width: 900px) {
  .record-statistics {
    grid-template-columns: 1fr;
  }

  .keyword-filter,
  .department-filter,
  .date-filter {
    width: 100%;
  }

  .detail-info-grid,
  .medical-sections {
    grid-template-columns: 1fr;
  }

  .medical-sections .diagnosis-section {
    grid-column: auto;
  }
}
</style>
