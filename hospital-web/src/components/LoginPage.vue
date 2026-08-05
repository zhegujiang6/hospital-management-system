<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { saveAuth } from '@/utils/auth'

const emit = defineEmits(['login-success'])

const formRef = ref()
const submitting = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { max: 50, message: '用户名不能超过50个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度必须在6到50个字符之间', trigger: 'blur' },
  ],
}

async function submitLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true

  try {
    const loginData = await login({
      username: form.username.trim(),
      password: form.password,
    })
    const user = saveAuth(loginData)
    ElMessage.success(`欢迎回来，${user.realName}`)
    emit('login-success', user)
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-showcase">
      <div class="showcase-content">
        <div class="login-brand">
          <span class="login-brand-mark">十</span>
          <div>
            <strong>安和医院</strong>
            <small>门诊管理系统</small>
          </div>
        </div>

        <div class="showcase-copy">
          <span class="showcase-eyebrow">HOSPITAL OPERATIONS</span>
          <h1>让每一次诊疗协作<br />清晰、有序、可信赖</h1>
          <p>统一管理科室、医生与诊疗业务，根据登录角色提供专属工作空间。</p>
        </div>

        <div class="showcase-features">
          <div>
            <strong>角色隔离</strong>
            <span>管理员与医生拥有不同的操作范围</span>
          </div>
          <div>
            <strong>安全访问</strong>
            <span>JWT 身份验证保护每一次业务请求</span>
          </div>
        </div>
      </div>
    </section>

    <section class="login-panel">
      <div class="login-card">
        <div class="login-heading">
          <span>欢迎使用</span>
          <h2>登录医院管理系统</h2>
          <p>请输入系统分配的账号和密码</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          @keyup.enter="submitLogin"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="form.username"
              size="large"
              autocomplete="username"
              placeholder="请输入用户名"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              size="large"
              autocomplete="current-password"
              show-password
              placeholder="请输入密码"
            />
          </el-form-item>

          <el-button
            class="login-button"
            type="primary"
            size="large"
            :loading="submitting"
            @click="submitLogin"
          >
            登录系统
          </el-button>
        </el-form>

        <div class="demo-account">
          <span>本地测试账号</span>
          <strong>admin / 123456</strong>
        </div>
      </div>

      <p class="login-footer">安和医院信息中心 · 仅限授权人员使用</p>
    </section>
  </main>
</template>

<style scoped>
.login-page {
  display: grid;
  min-width: 1080px;
  min-height: 100vh;
  grid-template-columns: minmax(520px, 1.18fr) minmax(480px, 0.82fr);
  background: #f5f7fb;
}

.login-showcase {
  position: relative;
  display: flex;
  min-height: 100vh;
  overflow: hidden;
  padding: 54px 7vw;
  background:
    radial-gradient(circle at 82% 18%, rgba(88, 205, 191, 0.18), transparent 24%),
    radial-gradient(circle at 12% 82%, rgba(72, 129, 245, 0.2), transparent 30%),
    linear-gradient(145deg, #0c2145 0%, #113467 58%, #115c70 100%);
  color: #fff;
}

.login-showcase::after {
  position: absolute;
  right: -120px;
  bottom: -180px;
  width: 470px;
  height: 470px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 50%;
  box-shadow:
    0 0 0 70px rgba(255, 255, 255, 0.035),
    0 0 0 145px rgba(255, 255, 255, 0.025);
  content: '';
}

.showcase-content {
  position: relative;
  z-index: 1;
  display: flex;
  width: 100%;
  max-width: 720px;
  flex-direction: column;
}

.login-brand {
  display: flex;
  align-items: center;
  gap: 13px;
}

.login-brand-mark {
  display: grid;
  width: 47px;
  height: 47px;
  place-items: center;
  border: 1px solid rgba(255, 255, 255, 0.35);
  border-radius: 14px;
  background: linear-gradient(135deg, #388bff, #26c5b6);
  box-shadow: 0 12px 30px rgba(16, 37, 79, 0.32);
  font-size: 30px;
}

.login-brand strong,
.login-brand small {
  display: block;
}

.login-brand strong {
  margin-bottom: 4px;
  font-size: 18px;
  letter-spacing: 0.08em;
}

.login-brand small {
  color: #9db6d7;
  font-size: 12px;
}

.showcase-copy {
  margin: auto 0;
  padding: 70px 0;
}

.showcase-eyebrow {
  color: #69dfd1;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.22em;
}

.showcase-copy h1 {
  margin: 18px 0 22px;
  font-size: clamp(38px, 4vw, 60px);
  line-height: 1.2;
  letter-spacing: -0.035em;
}

.showcase-copy p {
  max-width: 560px;
  margin: 0;
  color: #b8cae1;
  font-size: 15px;
  line-height: 1.9;
}

.showcase-features {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.showcase-features div {
  padding: 18px 20px;
  border: 1px solid rgba(255, 255, 255, 0.11);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.055);
  backdrop-filter: blur(10px);
}

.showcase-features strong,
.showcase-features span {
  display: block;
}

.showcase-features strong {
  margin-bottom: 7px;
  font-size: 13px;
}

.showcase-features span {
  color: #9fb3cf;
  font-size: 11px;
  line-height: 1.6;
}

.login-panel {
  display: flex;
  min-height: 100vh;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  padding: 50px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  padding: 42px;
  border: 1px solid #e5eaf2;
  border-radius: 22px;
  background: #fff;
  box-shadow: 0 24px 60px rgba(27, 46, 82, 0.1);
}

.login-heading {
  margin-bottom: 30px;
}

.login-heading > span {
  color: #2d72df;
  font-size: 12px;
  font-weight: 700;
}

.login-heading h2 {
  margin: 9px 0 8px;
  color: #17223a;
  font-size: 25px;
}

.login-heading p {
  margin: 0;
  color: #8a95a8;
  font-size: 13px;
}

.login-button {
  width: 100%;
  margin-top: 8px;
  border-radius: 10px;
}

.demo-account {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 24px;
  padding: 13px 15px;
  border-radius: 10px;
  background: #f2f6fd;
  color: #7e8ba0;
  font-size: 11px;
}

.demo-account strong {
  color: #3569b8;
  font-size: 12px;
}

.login-footer {
  margin: 22px 0 0;
  color: #a3acba;
  font-size: 11px;
}
</style>
