<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-grain"></div>
    </div>

    <div class="login-card">
      <div class="login-header">
        <div class="logo-mark">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="8" cy="8" r="3"/>
            <circle cx="16" cy="8" r="2"/>
            <circle cx="12" cy="16" r="2.5"/>
            <path d="M10.5 9.5L14 8.5"/>
            <path d="M9.5 11L11 14"/>
            <path d="M15 10L13.5 14"/>
          </svg>
        </div>
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
  background: #f7f6f3;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
}
.bg-grain {
  position: absolute;
  inset: 0;
  background-image:
    radial-gradient(circle at 30% 70%, rgba(201, 168, 124, 0.06) 0%, transparent 50%),
    radial-gradient(circle at 70% 30%, rgba(26, 26, 46, 0.03) 0%, transparent 50%);
}

.login-card {
  position: relative;
  width: 380px;
  padding: 48px 40px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.login-header {
  text-align: center;
  margin-bottom: 36px;
}
.logo-mark {
  width: 56px;
  height: 56px;
  margin: 0 auto 20px;
  background: rgba(26, 26, 46, 0.04);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--crm-primary);
}
.login-header h1 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 6px;
  letter-spacing: 0.02em;
}
.login-header p {
  font-size: 13px;
  color: #9e9eb0;
  margin: 0;
  font-weight: 400;
}
.login-hint {
  text-align: center;
  font-size: 12px;
  color: #c4c4d0;
  margin: 24px 0 0;
  font-weight: 400;
}
</style>
