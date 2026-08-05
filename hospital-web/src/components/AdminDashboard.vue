<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getDashboardOverview } from '@/api/dashboard'

const emit = defineEmits(['navigate'])

const loading = ref(false)
const overview = ref(createEmptyOverview())

const todayText = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long',
}).format(new Date())

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 11) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const completionRate = computed(() => {
  const registrations = Number(overview.value.todayRegistrationCount) || 0
  const visits = Number(overview.value.todayVisitCount) || 0
  if (registrations === 0) return 0
  return Math.min(100, Math.round((visits / registrations) * 100))
})

const workloadMax = computed(() =>
  Math.max(
    Number(overview.value.todayScheduleCount) || 0,
    Number(overview.value.todayRegistrationCount) || 0,
    Number(overview.value.todayVisitCount) || 0,
    1,
  ),
)

function createEmptyOverview() {
  return {
    enabledDepartmentCount: 0,
    enabledDoctorCount: 0,
    enabledPatientCount: 0,
    todayScheduleCount: 0,
    todayRegistrationCount: 0,
    todayVisitCount: 0,
    pendingVisitCount: 0,
    pendingPaymentCount: 0,
    totalVisitCount: 0,
    todayRevenue: 0,
  }
}

async function loadOverview() {
  loading.value = true
  try {
    overview.value = {
      ...createEmptyOverview(),
      ...((await getDashboardOverview()) || {}),
    }
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

function formatMoney(value) {
  const amount = Number(value)
  return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
}

function workloadWidth(value) {
  return `${Math.round(((Number(value) || 0) / workloadMax.value) * 100)}%`
}

onMounted(loadOverview)
</script>

<template>
  <section v-loading="loading" class="content dashboard-content">
    <div class="welcome-strip dashboard-welcome">
      <div>
        <span class="eyebrow">HOSPITAL OVERVIEW</span>
        <h2>{{ greeting }}，今日运营情况已汇总</h2>
        <p>{{ todayText }} · 数据来自当前医院业务系统</p>
      </div>
      <button class="dashboard-refresh" type="button" @click="loadOverview">
        刷新数据
      </button>
    </div>

    <div class="primary-metrics">
      <article class="metric-card metric-blue">
        <div class="metric-mark">挂</div>
        <div>
          <span>今日挂号</span>
          <strong>{{ overview.todayRegistrationCount }}</strong>
          <small>今天创建的挂号订单</small>
        </div>
      </article>

      <article class="metric-card metric-green">
        <div class="metric-mark">诊</div>
        <div>
          <span>今日接诊</span>
          <strong>{{ overview.todayVisitCount }}</strong>
          <small>今天完成的就诊记录</small>
        </div>
      </article>

      <article class="metric-card metric-orange">
        <div class="metric-mark">¥</div>
        <div>
          <span>今日收入</span>
          <strong class="money-value">¥ {{ formatMoney(overview.todayRevenue) }}</strong>
          <small>今天成功支付的金额</small>
        </div>
      </article>

      <article class="metric-card metric-purple">
        <div class="metric-mark">候</div>
        <div>
          <span>待接诊</span>
          <strong>{{ overview.pendingVisitCount }}</strong>
          <small>已支付、等待医生处理</small>
        </div>
      </article>
    </div>

    <div class="dashboard-layout">
      <div class="panel operation-panel">
        <div class="panel-heading">
          <div>
            <h3>今日诊疗进度</h3>
            <p>查看排班、挂号和接诊之间的业务进展</p>
          </div>
          <div class="completion-badge">
            <strong>{{ completionRate }}%</strong>
            <span>今日接诊率</span>
          </div>
        </div>

        <div class="workload-list">
          <div class="workload-row">
            <div class="workload-label">
              <span>今日有效排班</span>
              <strong>{{ overview.todayScheduleCount }}</strong>
            </div>
            <div class="workload-track">
              <span class="workload-bar schedule-bar" :style="{ width: workloadWidth(overview.todayScheduleCount) }"></span>
            </div>
          </div>
          <div class="workload-row">
            <div class="workload-label">
              <span>今日新增挂号</span>
              <strong>{{ overview.todayRegistrationCount }}</strong>
            </div>
            <div class="workload-track">
              <span class="workload-bar registration-bar" :style="{ width: workloadWidth(overview.todayRegistrationCount) }"></span>
            </div>
          </div>
          <div class="workload-row">
            <div class="workload-label">
              <span>今日完成接诊</span>
              <strong>{{ overview.todayVisitCount }}</strong>
            </div>
            <div class="workload-track">
              <span class="workload-bar visit-bar" :style="{ width: workloadWidth(overview.todayVisitCount) }"></span>
            </div>
          </div>
        </div>

        <p class="progress-note">
          接诊率根据“今日完成接诊数 ÷ 今日新增挂号数”计算，仅用于快速观察当日进度。
        </p>
      </div>

      <div class="panel resource-panel">
        <div class="panel-heading">
          <div>
            <h3>系统资源</h3>
            <p>当前启用的基础数据</p>
          </div>
        </div>

        <div class="resource-list">
          <button type="button" @click="emit('navigate', 'departments')">
            <span class="resource-icon resource-department">科</span>
            <span><strong>{{ overview.enabledDepartmentCount }}</strong><small>启用科室</small></span>
            <i>查看</i>
          </button>
          <button type="button" @click="emit('navigate', 'doctors')">
            <span class="resource-icon resource-doctor">医</span>
            <span><strong>{{ overview.enabledDoctorCount }}</strong><small>在岗医生</small></span>
            <i>查看</i>
          </button>
          <button type="button" @click="emit('navigate', 'patients')">
            <span class="resource-icon resource-patient">患</span>
            <span><strong>{{ overview.enabledPatientCount }}</strong><small>启用患者</small></span>
            <i>查看</i>
          </button>
          <button type="button" @click="emit('navigate', 'records')">
            <span class="resource-icon resource-record">历</span>
            <span><strong>{{ overview.totalVisitCount }}</strong><small>累计病历</small></span>
            <i>查看</i>
          </button>
        </div>
      </div>
    </div>

    <div class="panel reminder-panel">
      <div class="panel-heading">
        <div>
          <h3>业务提醒</h3>
          <p>需要持续关注的业务状态</p>
        </div>
      </div>

      <div class="reminder-grid">
        <article>
          <div class="reminder-top">
            <span class="reminder-dot payment-dot"></span>
            <small>支付处理中</small>
          </div>
          <strong>{{ overview.pendingPaymentCount }}</strong>
          <p>笔挂号订单仍在等待患者支付</p>
          <button type="button" @click="emit('navigate', 'registrations')">查看挂号订单</button>
        </article>

        <article>
          <div class="reminder-top">
            <span class="reminder-dot visit-dot"></span>
            <small>等待接诊</small>
          </div>
          <strong>{{ overview.pendingVisitCount }}</strong>
          <p>位患者已完成支付，等待医生接诊</p>
          <button type="button" @click="emit('navigate', 'registrations')">查看待接诊情况</button>
        </article>

        <article>
          <div class="reminder-top">
            <span class="reminder-dot schedule-dot"></span>
            <small>今日排班</small>
          </div>
          <strong>{{ overview.todayScheduleCount }}</strong>
          <p>个有效排班正在承载今天的门诊工作</p>
          <button type="button" @click="emit('navigate', 'schedules')">查看排班安排</button>
        </article>
      </div>
    </div>
  </section>
</template>

<style scoped>
.dashboard-content {
  color: #202d43;
}

.dashboard-welcome {
  background:
    radial-gradient(circle at 82% 32%, rgba(255, 255, 255, 0.16) 0 5%, transparent 5.5%),
    radial-gradient(circle at 88% 44%, rgba(255, 255, 255, 0.1) 0 13%, transparent 13.5%),
    linear-gradient(125deg, #173f7a, #296bc2 55%, #2d8b95);
}

.dashboard-refresh {
  position: relative;
  z-index: 1;
  padding: 11px 18px;
  border: 1px solid rgba(255, 255, 255, 0.45);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.14);
  color: #fff;
  cursor: pointer;
  font-size: 12px;
  font-weight: 700;
  transition: background 0.18s ease;
}

.dashboard-refresh:hover {
  background: rgba(255, 255, 255, 0.22);
}

.primary-metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin: 20px 0;
}

.metric-card {
  display: flex;
  align-items: center;
  gap: 15px;
  min-height: 112px;
  padding: 20px;
  border: 1px solid #e7edf5;
  border-radius: 15px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(25, 47, 83, 0.045);
}

.metric-mark {
  display: grid;
  width: 48px;
  height: 48px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 14px;
  font-size: 15px;
  font-weight: 800;
}

.metric-blue .metric-mark {
  background: #e8f1ff;
  color: #3977dd;
}

.metric-green .metric-mark {
  background: #e3f7f1;
  color: #159877;
}

.metric-orange .metric-mark {
  background: #fff1df;
  color: #d48325;
}

.metric-purple .metric-mark {
  background: #f0eaff;
  color: #7b58cb;
}

.metric-card span,
.metric-card strong,
.metric-card small {
  display: block;
}

.metric-card span {
  color: #78859a;
  font-size: 11px;
}

.metric-card strong {
  margin: 5px 0 3px;
  color: #1b2940;
  font-size: 27px;
  line-height: 1;
}

.metric-card .money-value {
  font-size: 21px;
}

.metric-card small {
  color: #a0a8b5;
  font-size: 10px;
}

.dashboard-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(330px, 0.85fr);
  gap: 18px;
}

.completion-badge {
  text-align: right;
}

.completion-badge strong,
.completion-badge span {
  display: block;
}

.completion-badge strong {
  color: #2e70d1;
  font-size: 23px;
}

.completion-badge span {
  margin-top: 3px;
  color: #98a2b2;
  font-size: 10px;
}

.workload-list {
  padding: 4px 24px 12px;
}

.workload-row {
  margin-bottom: 20px;
}

.workload-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.workload-label span {
  color: #66758a;
  font-size: 12px;
}

.workload-label strong {
  color: #27364d;
  font-size: 13px;
}

.workload-track {
  height: 8px;
  overflow: hidden;
  border-radius: 999px;
  background: #edf1f6;
}

.workload-bar {
  display: block;
  min-width: 4px;
  height: 100%;
  border-radius: inherit;
  transition: width 0.35s ease;
}

.schedule-bar {
  background: linear-gradient(90deg, #5c8de5, #73a3f4);
}

.registration-bar {
  background: linear-gradient(90deg, #8b6bd6, #a58ae4);
}

.visit-bar {
  background: linear-gradient(90deg, #2aa586, #54bfa2);
}

.progress-note {
  margin: 0;
  padding: 13px 24px 18px;
  border-top: 1px solid #edf1f6;
  color: #9aa4b4;
  font-size: 10px;
}

.resource-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  padding: 4px 20px 22px;
}

.resource-list button {
  display: grid;
  grid-template-columns: 38px 1fr auto;
  align-items: center;
  gap: 10px;
  padding: 13px;
  border: 1px solid #e9edf3;
  border-radius: 11px;
  background: #fbfcfe;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.18s ease, transform 0.18s ease;
}

.resource-list button:hover {
  transform: translateY(-1px);
  border-color: #cbdaf0;
}

.resource-icon {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  border-radius: 11px;
  font-size: 12px;
  font-weight: 800;
}

.resource-department {
  background: #e9f1ff;
  color: #3977dd;
}

.resource-doctor {
  background: #e3f7f1;
  color: #159877;
}

.resource-patient {
  background: #fff1df;
  color: #d48325;
}

.resource-record {
  background: #f0eaff;
  color: #7b58cb;
}

.resource-list button > span:nth-child(2) strong,
.resource-list button > span:nth-child(2) small {
  display: block;
}

.resource-list button > span:nth-child(2) strong {
  color: #26354c;
  font-size: 17px;
}

.resource-list button > span:nth-child(2) small {
  margin-top: 2px;
  color: #8f99a9;
  font-size: 10px;
}

.resource-list i {
  color: #a7afbc;
  font-size: 9px;
  font-style: normal;
}

.reminder-panel {
  margin-top: 18px;
}

.reminder-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  padding: 3px 22px 23px;
}

.reminder-grid article {
  padding: 17px 18px;
  border: 1px solid #e8edf4;
  border-radius: 12px;
  background: #fbfcfe;
}

.reminder-top {
  display: flex;
  align-items: center;
  gap: 7px;
}

.reminder-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
}

.payment-dot {
  background: #e7a243;
  box-shadow: 0 0 0 4px rgba(231, 162, 67, 0.12);
}

.visit-dot {
  background: #775bc7;
  box-shadow: 0 0 0 4px rgba(119, 91, 199, 0.12);
}

.schedule-dot {
  background: #3e82de;
  box-shadow: 0 0 0 4px rgba(62, 130, 222, 0.12);
}

.reminder-top small {
  color: #7f8b9e;
  font-size: 11px;
}

.reminder-grid article > strong {
  display: block;
  margin: 12px 0 5px;
  color: #24334a;
  font-size: 27px;
}

.reminder-grid p {
  min-height: 32px;
  margin: 0 0 12px;
  color: #8d97a7;
  font-size: 11px;
  line-height: 1.55;
}

.reminder-grid button {
  padding: 0;
  border: 0;
  background: transparent;
  color: #3474d1;
  cursor: pointer;
  font-size: 11px;
}

@media (max-width: 1220px) {
  .primary-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dashboard-layout {
    grid-template-columns: 1fr;
  }
}
</style>
