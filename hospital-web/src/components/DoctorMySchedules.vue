<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyScheduleList } from '@/api/schedule'

const schedules = ref([])
const loading = ref(false)

const filters = reactive({
  range: 'UPCOMING',
  period: '',
})

const today = computed(() => formatLocalDate(new Date()))

const statistics = computed(() => ({
  today: schedules.value.filter((item) => item.scheduleDate === today.value).length,
  upcoming: schedules.value.filter(
    (item) => item.scheduleDate >= today.value && item.status === 1,
  ).length,
  remaining: schedules.value
    .filter((item) => item.scheduleDate >= today.value && item.status === 1)
    .reduce((total, item) => total + Number(item.remainingSlots || 0), 0),
  history: schedules.value.filter((item) => item.scheduleDate < today.value).length,
}))

const filteredSchedules = computed(() => {
  return schedules.value.filter((item) => {
    const matchesPeriod = !filters.period || item.period === filters.period
    const matchesRange =
      filters.range === 'ALL' ||
      (filters.range === 'TODAY' && item.scheduleDate === today.value) ||
      (filters.range === 'UPCOMING' && item.scheduleDate >= today.value) ||
      (filters.range === 'HISTORY' && item.scheduleDate < today.value)

    return matchesPeriod && matchesRange
  })
})

async function loadSchedules() {
  loading.value = true

  try {
    schedules.value = (await getMyScheduleList()) || []
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.range = 'UPCOMING'
  filters.period = ''
}

function formatLocalDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatPeriod(value) {
  return value === 'MORNING' ? '上午' : value === 'AFTERNOON' ? '下午' : value || '—'
}

function formatMoney(value) {
  return `¥${Number(value || 0).toFixed(2)}`
}

function usedSlots(row) {
  return Math.max(0, Number(row.totalSlots || 0) - Number(row.remainingSlots || 0))
}

function usagePercentage(row) {
  const total = Number(row.totalSlots || 0)
  return total > 0 ? Math.round((usedSlots(row) / total) * 100) : 0
}

function scheduleState(row) {
  if (row.status === 0) return { label: '已停诊', type: 'danger' }
  if (row.scheduleDate < today.value) return { label: '已结束', type: 'info' }
  if (Number(row.remainingSlots || 0) === 0) return { label: '已约满', type: 'warning' }
  if (row.scheduleDate === today.value) return { label: '今日出诊', type: 'success' }
  return { label: '待出诊', type: 'primary' }
}

onMounted(loadSchedules)
</script>

<template>
  <section class="content">
    <div class="welcome-strip my-schedule-welcome">
      <div>
        <span class="eyebrow">MY SCHEDULES</span>
        <h2>查看我的出诊安排</h2>
        <p>排班由管理员统一维护；这里仅展示当前登录医生自己的日期、时段和号源使用情况。</p>
      </div>
      <el-button class="schedule-refresh" @click="loadSchedules">刷新排班</el-button>
    </div>

    <div class="stats-grid schedule-stats-grid">
      <article class="stat-card">
        <div class="stat-icon stat-icon-green">今</div>
        <div>
          <span>今日排班</span>
          <strong>{{ statistics.today }}</strong>
        </div>
      </article>
      <article class="stat-card">
        <div class="stat-icon stat-icon-blue">待</div>
        <div>
          <span>未来有效排班</span>
          <strong>{{ statistics.upcoming }}</strong>
        </div>
      </article>
      <article class="stat-card">
        <div class="stat-icon schedule-slot-icon">号</div>
        <div>
          <span>未来剩余号源</span>
          <strong>{{ statistics.remaining }}</strong>
        </div>
      </article>
      <article class="stat-card">
        <div class="stat-icon stat-icon-gray">历</div>
        <div>
          <span>历史排班</span>
          <strong>{{ statistics.history }}</strong>
        </div>
      </article>
    </div>

    <div class="panel">
      <div class="panel-heading">
        <div>
          <h3>我的排班列表</h3>
          <p>共 {{ filteredSchedules.length }} 条符合条件的数据</p>
        </div>
      </div>

      <div class="filters schedule-filters">
        <el-segmented
          v-model="filters.range"
          :options="[
            { label: '未来排班', value: 'UPCOMING' },
            { label: '今日', value: 'TODAY' },
            { label: '历史', value: 'HISTORY' },
            { label: '全部', value: 'ALL' },
          ]"
        />
        <el-select v-model="filters.period" placeholder="全部时段" class="period-filter">
          <el-option label="全部时段" value="" />
          <el-option label="上午" value="MORNING" />
          <el-option label="下午" value="AFTERNOON" />
        </el-select>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="filteredSchedules"
        row-key="id"
        class="department-table"
        empty-text="当前没有符合条件的排班"
      >
        <el-table-column label="出诊日期" min-width="145">
          <template #default="{ row }">
            <div class="schedule-date">
              <strong>{{ row.scheduleDate }}</strong>
              <small>{{ row.scheduleDate === today ? '今天' : formatPeriod(row.period) }}</small>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="出诊时段" width="105">
          <template #default="{ row }">
            <el-tag effect="plain" round>{{ formatPeriod(row.period) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="科室" min-width="140">
          <template #default="{ row }">
            <div class="department-info">
              <strong>{{ row.departmentName }}</strong>
              <small>{{ row.doctorName }} · {{ row.doctorNo }}</small>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="号源使用" min-width="220">
          <template #default="{ row }">
            <div class="slot-progress">
              <div>
                <span>已预约 {{ usedSlots(row) }}</span>
                <strong>剩余 {{ row.remainingSlots }} / {{ row.totalSlots }}</strong>
              </div>
              <el-progress
                :percentage="usagePercentage(row)"
                :stroke-width="7"
                :show-text="false"
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column label="挂号费" width="105">
          <template #default="{ row }">
            <strong class="schedule-fee">{{ formatMoney(row.registrationFee) }}</strong>
          </template>
        </el-table-column>
        <el-table-column label="排班状态" width="115">
          <template #default="{ row }">
            <el-tag :type="scheduleState(row).type" effect="light" round>
              {{ scheduleState(row).label }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </section>
</template>

<style scoped>
.my-schedule-welcome {
  background: linear-gradient(125deg, #315f8f, #347e9f 55%, #27918b);
}

.schedule-refresh {
  border-color: rgba(255, 255, 255, 0.45);
  background: #fff;
  color: #267683;
  font-weight: 700;
}

.schedule-stats-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.schedule-slot-icon {
  background: #fff3d9;
  color: #c4821d;
}

.schedule-filters {
  align-items: center;
  flex-wrap: wrap;
}

.period-filter {
  width: 135px;
}

.schedule-date strong,
.schedule-date small,
.department-info strong,
.department-info small {
  display: block;
}

.schedule-date strong,
.department-info strong {
  color: #26334a;
  font-size: 13px;
}

.schedule-date small,
.department-info small {
  margin-top: 5px;
  color: #8b96a8;
  font-size: 11px;
}

.slot-progress {
  width: 100%;
  max-width: 190px;
}

.slot-progress > div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  color: #7f8a9c;
  font-size: 10px;
}

.slot-progress strong {
  color: #425069;
  font-size: 11px;
}

.schedule-fee {
  color: #287e86;
  font-size: 13px;
}
</style>
