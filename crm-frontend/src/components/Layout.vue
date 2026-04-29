<template>
  <el-container style="height: 100vh">
    <el-aside :width="isCollapse ? '64px' : '220px'" style="background-color: #304156; transition: width 0.3s">
      <div style="height: 60px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 20px; font-weight: bold; border-bottom: 1px solid rgba(255,255,255,0.1)">
        <span v-if="!isCollapse">CRM系统</span>
        <span v-else>CRM</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
        style="border-right: none"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/customers">
          <el-icon><User /></el-icon>
          <span>客户管理</span>
        </el-menu-item>
        <el-menu-item index="/pool">
          <el-icon><Coin /></el-icon>
          <span>公海池</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <el-icon><ShoppingCart /></el-icon>
          <span>销售管理</span>
        </el-menu-item>
        <el-menu-item index="/follow-ups">
          <el-icon><ChatDotSquare /></el-icon>
          <span>回访记录</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background: #fff; border-bottom: 1px solid #e6e6e6; display: flex; align-items: center; justify-content: space-between; height: 60px; padding: 0 20px">
        <div style="display: flex; align-items: center">
          <el-icon style="cursor: pointer; font-size: 20px; margin-right: 15px" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div style="display: flex; align-items: center; gap: 12px">
          <el-icon><Bell /></el-icon>
          <el-dropdown trigger="click">
            <span style="cursor: pointer; display: flex; align-items: center; gap: 5px">
              <el-avatar :size="30" icon="UserFilled" />
              <span>{{ userInfo.realName || userInfo.username || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main style="background-color: #f0f2f5; padding: 20px; overflow-y: auto">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { removeToken } from '../utils/auth'
import { getUserInfo } from '../api/auth'
import {
  HomeFilled, User, Coin, Fold, Expand,
  Bell, UserFilled, ArrowDown, SwitchButton,
  ShoppingCart, ChatDotSquare
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)
const userInfo = ref({})

const activeMenu = computed(() => route.path)

onMounted(async () => {
  try {
    const res = await getUserInfo()
    userInfo.value = res.data || {}
  } catch (e) {
    // ignore
  }
})

function logout() {
  removeToken()
  router.push('/login')
}
</script>

<style scoped>
.el-menu:not(.el-menu--collapse) {
  width: 220px;
}
.el-aside ::-webkit-scrollbar {
  display: none;
}
</style>
