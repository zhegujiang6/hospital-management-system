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
    <div class="welcome-strip patient-welcome">
      <div>
        <span class="eyebrow">PATIENT SERVICES</span>
        <h2>{{ user.realName }}，欢迎进入患者服务中心</h2>
        <p>你的账号已与患者档案安全绑定，后续订餐、配送位置和订单都将使用后端认证身份。</p>
      </div>
      <div class="role-badge">患者账号</div>
    </div>

    <div class="patient-grid">
      <article>
        <span>01</span>
        <div>
          <strong>身份已绑定</strong>
          <p>后端认证身份绑定患者编号 {{ user.patientId }}</p>
        </div>
      </article>
      <article>
        <span>02</span>
        <div>
          <strong>院内配送</strong>
          <p>住院患者的配送位置将自动关联病区、房间和床位</p>
        </div>
      </article>
      <article>
        <span>03</span>
        <div>
          <strong>餐饮服务</strong>
          <p>菜单、购物车和餐饮订单功能将在接下来的模块中开放</p>
        </div>
      </article>
    </div>

    <div class="panel identity-panel">
      <div class="panel-heading">
        <div>
          <h3>当前认证身份</h3>
          <p>这些信息来自登录接口，患者ID不能由页面自行修改</p>
        </div>
        <el-tag type="success" effect="light" round>认证有效</el-tag>
      </div>
      <dl>
        <div><dt>登录账号</dt><dd>{{ user.username }}</dd></div>
        <div><dt>患者姓名</dt><dd>{{ user.realName }}</dd></div>
        <div><dt>系统角色</dt><dd>{{ user.role }}</dd></div>
        <div><dt>患者档案ID</dt><dd>{{ user.patientId }}</dd></div>
      </dl>
    </div>
  </section>
</template>

<style scoped>
.patient-welcome {
  background:
    radial-gradient(circle at 85% 38%, rgba(255, 255, 255, 0.13) 0 11%, transparent 11.5%),
    linear-gradient(125deg, #a25a20, #c8792f 56%, #d89445);
}

.role-badge {
  padding: 11px 18px;
  border: 1px solid rgba(255, 255, 255, 0.32);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 13px;
  font-weight: 700;
}

.patient-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  margin: 22px 0;
}

.patient-grid article {
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

.patient-grid article > span {
  display: grid;
  width: 45px;
  height: 45px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 13px;
  background: #fff2e5;
  color: #c37329;
  font-size: 12px;
  font-weight: 700;
}

.patient-grid strong {
  color: #1d2940;
  font-size: 14px;
}

.patient-grid p {
  margin: 7px 0 0;
  color: #8a95a8;
  font-size: 11px;
  line-height: 1.6;
}

.identity-panel {
  padding-bottom: 20px;
}

.identity-panel dl {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin: 0;
  padding: 0 24px;
}

.identity-panel dl > div {
  padding: 16px 18px;
  border-radius: 11px;
  background: #f7f9fc;
}

.identity-panel dt {
  margin-bottom: 6px;
  color: #8d98aa;
  font-size: 11px;
}

.identity-panel dd {
  margin: 0;
  color: #273149;
  font-size: 13px;
  font-weight: 700;
}
</style>
