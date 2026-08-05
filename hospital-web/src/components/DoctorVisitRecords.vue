<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getVisitRecordDetail, getVisitRecordList } from '@/api/visitRecord'

const records = ref([])
const loading = ref(false)
const keyword = ref('')
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)

const filteredRecords = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  if (!text) return records.value

  return records.value.filter(
    (item) =>
      item.recordNo?.toLowerCase().includes(text) ||
      item.orderNo?.toLowerCase().includes(text) ||
      item.patientNo?.toLowerCase().includes(text) ||
      item.patientName?.toLowerCase().includes(text) ||
      item.diagnosis?.toLowerCase().includes(text),
  )
})

async function loadRecords() {
  loading.value = true
  try {
    records.value = (await getVisitRecordList()) || []
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
    detail.value = await getVisitRecordDetail(row.id)
  } catch (error) {
    detailVisible.value = false
    ElMessage.error(error.message)
  } finally {
    detailLoading.value = false
  }
}

function formatPeriod(value) {
  return value === 'MORNING' ? '上午' : value === 'AFTERNOON' ? '下午' : value || '—'
}

function formatDateTime(value) {
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

onMounted(loadRecords)
</script>

<template>
  <section class="content">
    <div class="welcome-strip records-welcome">
      <div>
        <span class="eyebrow">MEDICAL RECORDS</span>
        <h2>我的历史病历</h2>
        <p>只显示当前登录医生完成的就诊记录，病历详情不能被其他医生越权查看。</p>
      </div>
      <div class="record-total">
        <strong>{{ records.length }}</strong>
        <span>份历史病历</span>
      </div>
    </div>

    <div class="panel records-panel">
      <div class="panel-heading">
        <div>
          <h3>就诊记录</h3>
          <p>共 {{ filteredRecords.length }} 条符合条件的数据</p>
        </div>
        <el-button plain @click="loadRecords">刷新数据</el-button>
      </div>

      <div class="filters">
        <el-input
          v-model="keyword"
          clearable
          placeholder="搜索病历号、患者或诊断结果"
          class="record-search"
        />
      </div>

      <el-table
        v-loading="loading"
        :data="filteredRecords"
        row-key="id"
        class="department-table"
        empty-text="暂无历史病历"
      >
        <el-table-column label="病历编号" min-width="220">
          <template #default="{ row }">
            <span class="record-number">{{ row.recordNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="患者" min-width="145">
          <template #default="{ row }">
            <div class="record-patient">
              <strong>{{ row.patientName }}</strong>
              <small>{{ row.patientNo }}</small>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="接诊时间" min-width="165">
          <template #default="{ row }">
            <span class="record-time">{{ formatDateTime(row.visitTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="diagnosis" label="诊断结果" min-width="240" show-overflow-tooltip />
        <el-table-column prop="departmentName" label="科室" min-width="120" />
        <el-table-column label="状态" width="100">
          <template #default>
            <el-tag type="primary" effect="light" round>已完成</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="right" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看病历</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="detailVisible" title="就诊记录详情" width="760px" align-center>
      <div v-loading="detailLoading" class="record-detail">
        <template v-if="detail">
          <div class="detail-banner">
            <div>
              <span>病历编号</span>
              <strong>{{ detail.recordNo }}</strong>
            </div>
            <el-tag type="primary" effect="light" round>已完成</el-tag>
          </div>

          <div class="detail-info-grid">
            <div><span>患者</span><strong>{{ detail.patientName }} · {{ detail.patientNo }}</strong></div>
            <div><span>性别</span><strong>{{ detail.patientGender || '—' }}</strong></div>
            <div><span>联系电话</span><strong>{{ detail.patientPhone || '—' }}</strong></div>
            <div><span>预约安排</span><strong>{{ detail.scheduleDate }} {{ formatPeriod(detail.period) }}</strong></div>
            <div><span>接诊医生</span><strong>{{ detail.doctorName }} · {{ detail.doctorTitle }}</strong></div>
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
.records-welcome {
  background: linear-gradient(125deg, #354f80, #496ca7 58%, #467f9d);
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
  color: #dce8fb;
  font-size: 11px;
}

.records-panel {
  margin-top: 22px;
}

.record-search {
  width: 360px;
}

.record-number {
  color: #53637c;
  font-family: Consolas, monospace;
  font-size: 11px;
}

.record-patient strong,
.record-patient small {
  display: block;
}

.record-patient strong {
  color: #253149;
  font-size: 13px;
}

.record-patient small {
  margin-top: 4px;
  color: #9099a9;
  font-size: 11px;
}

.record-time {
  color: #667287;
  font-size: 12px;
}

.record-detail {
  min-height: 160px;
}

.detail-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: -5px 0 18px;
  padding: 16px 18px;
  border-radius: 12px;
  background: #f2f6fc;
}

.detail-banner span,
.detail-banner strong {
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
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 18px;
}

.detail-info-grid > div {
  padding: 14px 15px;
  border: 1px solid #e8edf5;
  border-radius: 10px;
}

.detail-info-grid span,
.detail-info-grid strong {
  display: block;
}

.detail-info-grid strong {
  margin-top: 6px;
  color: #28354d;
  font-size: 12px;
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
  background: #edf5ff;
}

.medical-sections p {
  margin: 8px 0 0;
  color: #374359;
  font-size: 13px;
  line-height: 1.75;
  white-space: pre-wrap;
}
</style>
