<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DepartmentManagement from '@/components/DepartmentManagement.vue'
import WardManagement from '@/components/WardManagement.vue'
import BedManagement from '@/components/BedManagement.vue'
import InpatientAdmissionManagement from '@/components/InpatientAdmissionManagement.vue'
import DoctorManagement from '@/components/DoctorManagement.vue'
import PatientManagement from '@/components/PatientManagement.vue'
import ScheduleManagement from '@/components/ScheduleManagement.vue'
import RegistrationManagement from '@/components/RegistrationManagement.vue'
import AdminVisitRecords from '@/components/AdminVisitRecords.vue'
import AdminDashboard from '@/components/AdminDashboard.vue'
import DoctorPortal from '@/components/DoctorPortal.vue'
import MealManagement from '@/components/MealManagement.vue'
import MealOrdering from '@/components/MealOrdering.vue'
import MealOrderHistory from '@/components/MealOrderHistory.vue'
import MealAiCustomerService from '@/components/MealAiCustomerService.vue'
import AiCustomerServiceAdmin from '@/components/AiCustomerServiceAdmin.vue'
import DoctorPendingVisits from '@/components/DoctorPendingVisits.vue'
import DoctorVisitRecords from '@/components/DoctorVisitRecords.vue'
import DoctorMySchedules from '@/components/DoctorMySchedules.vue'
import LoginPage from '@/components/LoginPage.vue'
import { clearAuth, getCurrentUser, getToken } from '@/utils/auth'

const savedUser = getToken() ? getCurrentUser() : null
if (!savedUser) clearAuth()

const currentUser = ref(savedUser)
const isAdmin = computed(() => currentUser.value?.role === 'ADMIN')
const isDoctor = computed(() => currentUser.value?.role === 'DOCTOR')
const isPatient = computed(() => currentUser.value?.role === 'PATIENT')

function getRoleHome(role) {
  if (role === 'ADMIN') return 'overview'
  if (role === 'DOCTOR') return 'doctor-home'
  if (role === 'PATIENT') return 'meal-ordering'
  return 'unknown-home'
}

const activeModule = ref(getRoleHome(currentUser.value?.role))

const pageMeta = computed(() => {
  if (isPatient.value) {
    if (activeModule.value === 'meal-ai-service') {
      return {
        breadcrumb: '医院服务 / 院内餐饮',
        title: '餐饮智能客服',
      }
    }

    if (activeModule.value === 'meal-orders') {
      return {
        breadcrumb: '医院服务 / 院内餐饮',
        title: '我的餐饮订单',
      }
    }

    return {
      breadcrumb: '医院服务 / 院内餐饮',
      title: '院内点餐',
    }
  }

  if (isDoctor.value) {
    if (activeModule.value === 'my-schedules') {
      return {
        breadcrumb: '医院管理 / 医生工作台',
        title: '我的排班',
      }
    }

    if (activeModule.value === 'my-registrations') {
      return {
        breadcrumb: '医院管理 / 医生工作台',
        title: '待接诊患者',
      }
    }

    if (activeModule.value === 'my-records') {
      return {
        breadcrumb: '医院管理 / 医生工作台',
        title: '就诊记录',
      }
    }

    return {
      breadcrumb: '医院管理 / 医生工作台',
      title: '我的工作台',
    }
  }

  if (activeModule.value === 'overview') {
    return {
      breadcrumb: '医院管理 / 管理工作台',
      title: '数据概览',
    }
  }

  if (activeModule.value === 'doctors') {
    return {
      breadcrumb: '医院管理 / 医务人员',
      title: '医生管理',
    }
  }

  if (activeModule.value === 'patients') {
    return {
      breadcrumb: '医院管理 / 患者服务',
      title: '患者管理',
    }
  }

  if (activeModule.value === 'wards') {
    return {
      breadcrumb: '医院管理 / 住院服务',
      title: '病区管理',
    }
  }

  if (activeModule.value === 'beds') {
    return {
      breadcrumb: '医院管理 / 住院服务',
      title: '床位管理',
    }
  }

  if (activeModule.value === 'inpatient-admissions') {
    return {
      breadcrumb: '医院管理 / 住院服务',
      title: '住院管理',
    }
  }

  if (activeModule.value === 'meal-management') {
    return {
      breadcrumb: '医院管理 / 院内餐饮',
      title: '院内餐饮',
    }
  }

  if (activeModule.value === 'ai-customer-service') {
    return {
      breadcrumb: '医院管理 / 智能客服',
      title: '人工客服工作台',
    }
  }

  if (activeModule.value === 'schedules') {
    return {
      breadcrumb: '医院管理 / 诊疗业务',
      title: '排班管理',
    }
  }

  if (activeModule.value === 'registrations') {
    return {
      breadcrumb: '医院管理 / 诊疗业务',
      title: '挂号管理',
    }
  }

  if (activeModule.value === 'records') {
    return {
      breadcrumb: '医院管理 / 诊疗业务',
      title: '就诊记录',
    }
  }

  return {
    breadcrumb: '医院管理 / 基础信息',
    title: '科室管理',
  }
})

const roleLabel = computed(() => {
  if (isAdmin.value) return '系统管理员'
  if (isDoctor.value) return '医生'
  if (isPatient.value) return '患者'
  return '未知角色'
})

function handleMenuSelect(index) {
  const adminModules = ['overview', 'departments', 'doctors', 'patients', 'wards', 'beds', 'inpatient-admissions', 'meal-management', 'ai-customer-service', 'schedules', 'registrations', 'records']

  if (isAdmin.value && adminModules.includes(index)) {
    activeModule.value = index
  } else if (
    isDoctor.value &&
    ['doctor-home', 'my-schedules', 'my-registrations', 'my-records'].includes(index)
  ) {
    activeModule.value = index
  } else if (
    isPatient.value &&
    ['meal-ordering', 'meal-orders', 'meal-ai-service'].includes(index)
  ) {
    activeModule.value = index
  }
}

function handleLoginSuccess(user) {
  currentUser.value = user
  activeModule.value = getRoleHome(user.role)
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定退出当前账号吗？', '退出登录', {
      confirmButtonText: '确认退出',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }

  clearAuth()
  currentUser.value = null
  activeModule.value = 'departments'
  ElMessage.success('已安全退出')
}

function handleAuthExpired() {
  if (!currentUser.value) return

  currentUser.value = null
  activeModule.value = 'departments'
  ElMessage.warning('登录状态已失效，请重新登录')
}

onMounted(() => window.addEventListener('auth-expired', handleAuthExpired))
onBeforeUnmount(() => window.removeEventListener('auth-expired', handleAuthExpired))
</script>

<template>
  <LoginPage v-if="!currentUser" @login-success="handleLoginSuccess" />

  <div v-else class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">十</div>
        <div>
          <strong>安和医院</strong>
          <span>医院综合服务平台</span>
        </div>
      </div>

      <template v-if="isAdmin">
        <div class="menu-caption">管理工作台</div>
        <el-menu
          class="side-menu"
          :default-active="activeModule"
          @select="handleMenuSelect"
        >
          <el-menu-item index="overview">
            <span class="menu-symbol">◫</span>
            <span>数据概览</span>
          </el-menu-item>
          <el-menu-item index="departments">
            <span class="menu-symbol">＋</span>
            <span>科室管理</span>
          </el-menu-item>
          <el-menu-item index="doctors">
            <span class="menu-symbol">♙</span>
            <span>医生管理</span>
          </el-menu-item>
          <el-menu-item index="patients">
            <span class="menu-symbol">♧</span>
            <span>患者管理</span>
          </el-menu-item>

          <div class="menu-caption menu-caption-inner">住院服务</div>
          <el-menu-item index="wards">
            <span class="menu-symbol">▥</span>
            <span>病区管理</span>
          </el-menu-item>
          <el-menu-item index="beds">
            <span class="menu-symbol">▰</span>
            <span>床位管理</span>
          </el-menu-item>
          <el-menu-item index="inpatient-admissions">
            <span class="menu-symbol">▣</span>
            <span>住院管理</span>
          </el-menu-item>

          <div class="menu-caption menu-caption-inner">院内餐饮</div>
          <el-menu-item index="meal-management">
            <span class="menu-symbol">餐</span>
            <span>院内餐饮</span>
          </el-menu-item>

          <div class="menu-caption menu-caption-inner">智能客服</div>
          <el-menu-item index="ai-customer-service">
            <span class="menu-symbol">客</span>
            <span>人工客服工作台</span>
          </el-menu-item>

          <div class="menu-caption menu-caption-inner">诊疗业务</div>
          <el-menu-item index="schedules">
            <span class="menu-symbol">▦</span>
            <span>排班管理</span>
          </el-menu-item>
          <el-menu-item index="registrations">
            <span class="menu-symbol">✓</span>
            <span>挂号管理</span>
          </el-menu-item>
          <el-menu-item index="records">
            <span class="menu-symbol">▤</span>
            <span>就诊记录</span>
          </el-menu-item>

        </el-menu>
      </template>

      <template v-else-if="isDoctor">
        <div class="menu-caption">医生工作台</div>
        <el-menu
          class="side-menu"
          :default-active="activeModule"
          @select="handleMenuSelect"
        >
          <el-menu-item index="doctor-home">
            <span class="menu-symbol">⌂</span>
            <span>我的首页</span>
          </el-menu-item>
          <el-menu-item index="my-schedules">
            <span class="menu-symbol">▦</span>
            <span>我的排班</span>
          </el-menu-item>
          <el-menu-item index="my-registrations">
            <span class="menu-symbol">✓</span>
            <span>待接诊</span>
          </el-menu-item>
          <el-menu-item index="my-records">
            <span class="menu-symbol">▤</span>
            <span>就诊记录</span>
          </el-menu-item>
        </el-menu>
      </template>

      <template v-else-if="isPatient">
        <div class="menu-caption">患者服务中心</div>
        <el-menu class="side-menu" :default-active="activeModule" @select="handleMenuSelect">
          <el-menu-item index="meal-ordering">
            <span class="menu-symbol">餐</span>
            <span>院内点餐</span>
          </el-menu-item>
          <el-menu-item index="meal-orders">
            <span class="menu-symbol">单</span>
            <span>我的订单</span>
          </el-menu-item>
          <el-menu-item index="meal-ai-service">
            <span class="menu-symbol">AI</span>
            <span>餐饮客服</span>
          </el-menu-item>
        </el-menu>
      </template>

      <div class="sidebar-footer">
        <div class="status-dot"></div>
        <span>身份认证已启用</span>
      </div>
    </aside>

    <main class="main-area">
      <header class="topbar">
        <div>
          <div class="breadcrumb">{{ pageMeta.breadcrumb }}</div>
          <h1>{{ pageMeta.title }}</h1>
        </div>
        <div class="account-actions">
          <div class="user-card">
            <div class="avatar">{{ currentUser.realName?.slice(0, 1) }}</div>
            <div>
              <strong>{{ currentUser.realName }}</strong>
              <span>{{ roleLabel }} · {{ currentUser.username }}</span>
            </div>
          </div>
          <button class="logout-button" type="button" @click="handleLogout">退出</button>
        </div>
      </header>

      <template v-if="isAdmin">
        <AdminDashboard
          v-if="activeModule === 'overview'"
          @navigate="handleMenuSelect"
        />
        <DepartmentManagement v-else-if="activeModule === 'departments'" />
        <WardManagement v-else-if="activeModule === 'wards'" />
        <BedManagement v-else-if="activeModule === 'beds'" />
        <InpatientAdmissionManagement v-else-if="activeModule === 'inpatient-admissions'" />
        <DoctorManagement v-else-if="activeModule === 'doctors'" />
        <PatientManagement v-else-if="activeModule === 'patients'" />
        <ScheduleManagement v-else-if="activeModule === 'schedules'" />
        <RegistrationManagement v-else-if="activeModule === 'registrations'" />
        <AdminVisitRecords v-else-if="activeModule === 'records'" />
        <MealManagement v-else-if="activeModule === 'meal-management'" />
        <AiCustomerServiceAdmin v-else-if="activeModule === 'ai-customer-service'" />
      </template>
      <template v-else-if="isDoctor">
        <DoctorPortal
          v-if="activeModule === 'doctor-home'"
          :user="currentUser"
        />
        <DoctorPendingVisits
          v-else-if="activeModule === 'my-registrations'"
        />
        <DoctorMySchedules
          v-else-if="activeModule === 'my-schedules'"
        />
        <DoctorVisitRecords
          v-else-if="activeModule === 'my-records'"
        />
      </template>
      <template v-else-if="isPatient">
        <MealOrdering
          v-if="activeModule === 'meal-ordering'"
          :user="currentUser"
          @navigate-orders="handleMenuSelect('meal-orders')"
        />
        <MealOrderHistory v-else-if="activeModule === 'meal-orders'" />
        <MealAiCustomerService v-else-if="activeModule === 'meal-ai-service'" />
      </template>
    </main>
  </div>
</template>

<style scoped>
.account-actions {
  display: flex;
  align-items: center;
  gap: 18px;
}

.logout-button {
  padding: 7px 12px;
  border: 1px solid #dfe5ee;
  border-radius: 8px;
  background: #fff;
  color: #6f7a8c;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.18s ease;
}

.logout-button:hover {
  border-color: #f0a5a5;
  color: #d95050;
}
</style>
