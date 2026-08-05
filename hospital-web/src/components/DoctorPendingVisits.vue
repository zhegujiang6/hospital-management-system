<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createVisitRecord, getPendingVisits } from '@/api/visitRecord'

const visits = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const selectedOrder = ref(null)
const formRef = ref()

const form = reactive({
  registrationOrderId: null,
  chiefComplaint: '',
  presentIllness: '',
  diagnosis: '',
  treatmentPlan: '',
  doctorAdvice: '',
})

const rules = {
  chiefComplaint: [
    { required: true, message: '请输入患者主诉', trigger: 'blur' },
    { max: 500, message: '患者主诉不能超过500个字符', trigger: 'blur' },
  ],
  presentIllness: [
    { max: 1000, message: '现病史不能超过1000个字符', trigger: 'blur' },
  ],
  diagnosis: [
    { required: true, message: '请输入诊断结果', trigger: 'blur' },
    { max: 500, message: '诊断结果不能超过500个字符', trigger: 'blur' },
  ],
  treatmentPlan: [
    { max: 1000, message: '治疗方案不能超过1000个字符', trigger: 'blur' },
  ],
  doctorAdvice: [
    { max: 1000, message: '医生嘱咐不能超过1000个字符', trigger: 'blur' },
  ],
}

async function loadPendingVisits() {
  loading.value = true

  try {
    visits.value = (await getPendingVisits()) || []
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

async function openVisitDialog(row) {
  selectedOrder.value = row
  Object.assign(form, {
    registrationOrderId: row.id,
    chiefComplaint: '',
    presentIllness: '',
    diagnosis: '',
    treatmentPlan: '',
    doctorAdvice: '',
  })
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

async function submitVisitRecord() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true

  try {
    await createVisitRecord({
      registrationOrderId: form.registrationOrderId,
      chiefComplaint: form.chiefComplaint.trim(),
      presentIllness: form.presentIllness.trim() || null,
      diagnosis: form.diagnosis.trim(),
      treatmentPlan: form.treatmentPlan.trim() || null,
      doctorAdvice: form.doctorAdvice.trim() || null,
    })
    ElMessage.success('就诊记录保存成功，挂号订单已完成')
    dialogVisible.value = false
    await loadPendingVisits()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    submitting.value = false
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

onMounted(loadPendingVisits)
</script>

<template>
  <section class="content">
    <div class="welcome-strip pending-welcome">
      <div>
        <span class="eyebrow">TODAY'S CONSULTATIONS</span>
        <h2>待接诊患者</h2>
        <p>这里只显示属于当前医生、已经支付并且已到预约日期的挂号订单。</p>
      </div>
      <div class="pending-count">
        <strong>{{ visits.length }}</strong>
        <span>人待接诊</span>
      </div>
    </div>

    <div class="panel pending-panel">
      <div class="panel-heading">
        <div>
          <h3>接诊队列</h3>
          <p>完成病历后，挂号订单会自动从已支付变成已完成</p>
        </div>
        <el-button plain @click="loadPendingVisits">刷新队列</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="visits"
        row-key="id"
        class="department-table"
        empty-text="当前没有待接诊患者"
      >
        <el-table-column label="患者" min-width="170">
          <template #default="{ row }">
            <div class="patient-cell">
              <span>{{ row.patientName?.slice(0, 1) }}</span>
              <div>
                <strong>{{ row.patientName }}</strong>
                <small>{{ row.patientNo }}</small>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="预约时间" min-width="150">
          <template #default="{ row }">
            <strong class="schedule-time">{{ row.scheduleDate }} {{ formatPeriod(row.period) }}</strong>
          </template>
        </el-table-column>
        <el-table-column prop="departmentName" label="科室" min-width="120" />
        <el-table-column label="挂号订单" min-width="220">
          <template #default="{ row }">
            <div class="order-meta">
              <strong>{{ row.orderNo }}</strong>
              <small>支付于 {{ formatDateTime(row.paidTime) }}</small>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default>
            <el-tag type="success" effect="light" round>已支付</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="right" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openVisitDialog(row)">开始接诊</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="dialogVisible"
      title="填写就诊记录"
      width="760px"
      destroy-on-close
      align-center
    >
      <div v-if="selectedOrder" class="visit-patient-summary">
        <div>
          <span>患者</span>
          <strong>{{ selectedOrder.patientName }} · {{ selectedOrder.patientNo }}</strong>
        </div>
        <div>
          <span>预约时间</span>
          <strong>{{ selectedOrder.scheduleDate }} {{ formatPeriod(selectedOrder.period) }}</strong>
        </div>
        <div>
          <span>挂号订单</span>
          <strong>{{ selectedOrder.orderNo }}</strong>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="患者主诉" prop="chiefComplaint">
          <el-input
            v-model="form.chiefComplaint"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="记录患者主要症状、持续时间和就诊原因"
          />
        </el-form-item>

        <el-form-item label="现病史" prop="presentIllness">
          <el-input
            v-model="form.presentIllness"
            type="textarea"
            :rows="3"
            maxlength="1000"
            show-word-limit
            placeholder="记录症状发生、发展和既往处理情况（选填）"
          />
        </el-form-item>

        <el-form-item label="诊断结果" prop="diagnosis">
          <el-input
            v-model="form.diagnosis"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="请输入本次诊断结果"
          />
        </el-form-item>

        <div class="medical-form-grid">
          <el-form-item label="治疗方案" prop="treatmentPlan">
            <el-input
              v-model="form.treatmentPlan"
              type="textarea"
              :rows="4"
              maxlength="1000"
              show-word-limit
              placeholder="用药、检查或其他治疗安排（选填）"
            />
          </el-form-item>
          <el-form-item label="医生嘱咐" prop="doctorAdvice">
            <el-input
              v-model="form.doctorAdvice"
              type="textarea"
              :rows="4"
              maxlength="1000"
              show-word-limit
              placeholder="复诊、饮食和生活注意事项（选填）"
            />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">暂不提交</el-button>
        <el-button type="primary" :loading="submitting" @click="submitVisitRecord">
          保存并完成接诊
        </el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.pending-welcome {
  background: linear-gradient(125deg, #176b73, #178b89 58%, #297cab);
}

.pending-count {
  display: flex;
  min-width: 120px;
  flex-direction: column;
  align-items: center;
  padding: 17px 22px;
  border: 1px solid rgba(255, 255, 255, 0.24);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.12);
}

.pending-count strong {
  font-size: 30px;
}

.pending-count span {
  margin-top: 3px;
  color: #d5f3f1;
  font-size: 11px;
}

.pending-panel {
  margin-top: 22px;
}

.patient-cell {
  display: flex;
  align-items: center;
  gap: 11px;
}

.patient-cell > span {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border-radius: 12px;
  background: #e5f5f3;
  color: #148279;
  font-weight: 700;
}

.patient-cell strong,
.patient-cell small,
.order-meta strong,
.order-meta small {
  display: block;
}

.patient-cell strong,
.schedule-time {
  color: #243149;
  font-size: 13px;
}

.patient-cell small,
.order-meta small {
  margin-top: 4px;
  color: #8d97a8;
  font-size: 11px;
}

.order-meta strong {
  color: #59657a;
  font-family: Consolas, monospace;
  font-size: 11px;
}

.visit-patient-summary {
  display: grid;
  grid-template-columns: 1fr 1fr 1.35fr;
  gap: 12px;
  margin: -6px 0 22px;
  padding: 16px;
  border-radius: 12px;
  background: #f3f8f8;
}

.visit-patient-summary span,
.visit-patient-summary strong {
  display: block;
}

.visit-patient-summary span {
  margin-bottom: 6px;
  color: #83919f;
  font-size: 10px;
}

.visit-patient-summary strong {
  color: #26414a;
  font-size: 12px;
}

.medical-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}
</style>
