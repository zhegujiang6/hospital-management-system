<script setup>
defineProps({
  user: {
    type: Object,
    required: true,
  },
})
</script>

<template>
  <section class="content">
    <div class="welcome-strip doctor-portal-welcome">
      <div>
        <span class="eyebrow">DOCTOR WORKSPACE</span>
        <h2>{{ user.realName }}，欢迎进入医生工作台</h2>
        <p>这里将集中展示属于你的排班、挂号患者和就诊任务，管理员功能不会出现在医生账号中。</p>
      </div>
      <div class="role-badge">医生账号</div>
    </div>

    <div class="portal-grid">
      <article>
        <span>01</span>
        <div>
          <strong>身份隔离</strong>
          <p>医生账号只进入自己的诊疗工作台</p>
        </div>
      </article>
      <article>
        <span>02</span>
        <div>
          <strong>待接诊患者</strong>
          <p>查看已支付并且属于当前医生的挂号订单</p>
        </div>
      </article>
      <article>
        <span>03</span>
        <div>
          <strong>历史病历</strong>
          <p>查询自己完成的诊断、治疗方案和医生嘱咐</p>
        </div>
      </article>
    </div>

    <div class="panel portal-panel">
      <div class="panel-heading">
        <div>
          <h3>当前登录身份</h3>
          <p>登录信息来自后端认证结果，不包含密码</p>
        </div>
      </div>
      <dl>
        <div><dt>登录账号</dt><dd>{{ user.username }}</dd></div>
        <div><dt>真实姓名</dt><dd>{{ user.realName }}</dd></div>
        <div><dt>系统角色</dt><dd>{{ user.role }}</dd></div>
        <div><dt>医生编号</dt><dd>{{ user.doctorId || '暂未绑定' }}</dd></div>
      </dl>
    </div>
  </section>
</template>

<style scoped>
.doctor-portal-welcome {
  background: linear-gradient(125deg, #176b73, #178b89 58%, #297cab);
}

.role-badge {
  padding: 11px 18px;
  border: 1px solid rgba(255, 255, 255, 0.32);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 13px;
  font-weight: 700;
}

.portal-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  margin: 22px 0;
}

.portal-grid article {
  display: flex;
  min-height: 116px;
  align-items: center;
  gap: 16px;
  padding: 21px;
  border: 1px solid #e7ecf3;
  border-radius: 15px;
  background: #fff;
  box-shadow: 0 8px 25px rgba(26, 48, 88, 0.045);
}

.portal-grid article > span {
  display: grid;
  width: 45px;
  height: 45px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 13px;
  background: #e9f4f5;
  color: #168178;
  font-size: 12px;
  font-weight: 700;
}

.portal-grid strong {
  color: #1d2940;
  font-size: 14px;
}

.portal-grid p {
  margin: 7px 0 0;
  color: #8a95a8;
  font-size: 11px;
  line-height: 1.6;
}

.portal-panel {
  padding-bottom: 20px;
}

.portal-panel dl {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin: 0;
  padding: 0 24px;
}

.portal-panel dl > div {
  padding: 16px 18px;
  border-radius: 11px;
  background: #f7f9fc;
}

.portal-panel dt {
  margin-bottom: 6px;
  color: #8d98aa;
  font-size: 11px;
}

.portal-panel dd {
  margin: 0;
  color: #273149;
  font-size: 13px;
  font-weight: 700;
}
</style>
