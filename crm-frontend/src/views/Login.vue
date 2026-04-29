<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-grid"></div>
    </div>
    <div class="login-card">
      <div class="login-header">
        <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#2563eb" stroke-width="2" stroke-linecap="round">
          <rect x="3" y="3" width="7" height="7" rx="1"/>
          <rect x="14" y="3" width="7" height="7" rx="1"/>
          <rect x="3" y="14" width="7" height="7" rx="1"/>
          <rect x="14" y="14" width="7" height="7" rx="1"/>
        </svg>
        <h1>零售业CRM</h1>
        <p>客户关系管理平台</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>
      <p class="login-hint">默认账号 admin / admin123</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '../api/auth'
import { setToken } from '../utils/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  loading.value = true
  try {
    const res = await login(form)
    setToken(res.data.token)
    ElMessage.success('登录成功')
    router.push('/')
  } catch { /* interceptor handles */ }
  finally { loading.value = false }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: #f8fafc;
}
.login-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}
.bg-grid {
  position: absolute;
  inset: -50%;
  background-image:
    linear-gradient(rgba(37,99,235,0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(37,99,235,0.03) 1px, transparent 1px);
  background-size: 60px 60px;
  transform: rotate(-12deg) scale(1.5);
}
.login-card {
  position: relative;
  width: 380px;
  padding: 40px 36px;
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04), 0 20px 60px rgba(0,0,0,0.06);
}
.login-header {
  text-align: center;
  margin-bottom: 32px;
}
.login-header h1 {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  margin: 12px 0 4px;
  letter-spacing: -0.01em;
}
.login-header p {
  font-size: 13px;
  color: #94a3b8;
  margin: 0;
}
.login-hint {
  text-align: center;
  font-size: 12px;
  color: #cbd5e1;
  margin: 16px 0 0;
}
</style>
