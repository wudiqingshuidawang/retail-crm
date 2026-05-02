<template>
  <div class="crm-layout">
    <!-- Subtle background texture -->
    <div class="bg-texture"></div>

    <el-container class="crm-container">
      <!-- Sidebar -->
      <el-aside :width="isCollapse ? '72px' : '220px'" class="crm-sidebar">
        <div class="sidebar-brand" :class="{ collapsed: isCollapse }">
          <div class="brand-mark">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="8" cy="8" r="3"/>
              <circle cx="16" cy="8" r="2"/>
              <circle cx="12" cy="16" r="2.5"/>
              <path d="M10.5 9.5L14 8.5"/>
              <path d="M9.5 11L11 14"/>
              <path d="M15 10L13.5 14"/>
            </svg>
          </div>
          <transition name="fade">
            <span v-if="!isCollapse" class="brand-text">CRM</span>
          </transition>
        </div>

        <div class="sidebar-nav">
          <router-link
            v-for="item in menuItems"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ active: route.path === item.path || route.path.startsWith(item.path + '/') }"
          >
            <el-icon :size="17"><component :is="item.icon" /></el-icon>
            <span class="nav-label">{{ item.label }}</span>
          </router-link>
        </div>

        <div class="sidebar-footer">
          <button class="collapse-btn" @click="isCollapse = !isCollapse" :title="isCollapse ? '展开' : '收起'">
            <el-icon :size="15">
              <DArrowLeft v-if="!isCollapse" />
              <DArrowRight v-else />
            </el-icon>
          </button>
        </div>
      </el-aside>

      <!-- Main -->
      <el-container class="crm-main">
        <el-header class="crm-header">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <span class="header-badge">管理员</span>
            <el-avatar :size="28" icon="UserFilled" class="header-avatar" />
          </div>
        </el-header>

        <el-main class="crm-content">
          <router-view v-slot="{ Component }">
            <transition name="page" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import {
  HomeFilled, User, Coin, ShoppingCart, ChatDotSquare,
  DArrowLeft, DArrowRight, UserFilled, Star, Present, Trophy, Bell, Ticket
} from '@element-plus/icons-vue'

const route = useRoute()
const isCollapse = ref(false)

const menuItems = [
  { path: '/dashboard', label: '首页', icon: HomeFilled },
  { path: '/customers', label: '客户管理', icon: User },
  { path: '/pool', label: '公海池', icon: Coin },
  { path: '/members', label: '会员管理', icon: Star },
  { path: '/orders', label: '销售管理', icon: ShoppingCart },
  { path: '/follow-ups', label: '回访记录', icon: ChatDotSquare },
  { path: '/campaigns', label: '营销管理', icon: Present },
  { path: '/coupons', label: '优惠券管理', icon: Ticket },
  { path: '/performance', label: '业绩排行', icon: Trophy },
  { path: '/todos', label: '待办提醒', icon: Bell },
]
</script>

<style scoped>
.crm-layout {
  height: 100vh;
  overflow: hidden;
  position: relative;
}

/* ===== Subtle Background ===== */
.bg-texture {
  position: fixed;
  inset: 0;
  z-index: 0;
  background: #f7f6f3;
  background-image:
    radial-gradient(circle at 20% 80%, rgba(201, 168, 124, 0.04) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(26, 26, 46, 0.02) 0%, transparent 50%);
}

/* ===== Container ===== */
.crm-container {
  position: relative;
  z-index: 1;
  height: 100vh;
}

/* ===== Sidebar ===== */
.crm-sidebar {
  background: #1a1a2e;
  display: flex;
  flex-direction: column;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  position: relative;
}

.sidebar-brand {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 22px;
  gap: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}
.sidebar-brand.collapsed {
  padding: 0 22px;
  justify-content: center;
}
.brand-mark {
  color: var(--crm-accent);
  display: flex;
  flex-shrink: 0;
}
.brand-text {
  font-size: 15px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.9);
  letter-spacing: 0.12em;
  white-space: nowrap;
}

/* Nav */
.sidebar-nav {
  flex: 1;
  padding: 16px 10px;
  display: flex;
  flex-direction: column;
  gap: 1px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 9px 14px;
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.45);
  text-decoration: none;
  font-size: 13px;
  font-weight: 500;
  position: relative;
  transition: all 0.2s ease;
  white-space: nowrap;
  overflow: hidden;
}
.nav-item:hover {
  color: rgba(255, 255, 255, 0.75);
  background: rgba(255, 255, 255, 0.04);
}
.nav-item.active {
  color: var(--crm-accent);
  background: rgba(201, 168, 124, 0.08);
  font-weight: 600;
}
.nav-label {
  transition: opacity 0.2s ease;
}

/* Sidebar footer */
.sidebar-footer {
  padding: 8px 10px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}
.collapse-btn {
  width: 100%;
  padding: 7px;
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.3);
  cursor: pointer;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}
.collapse-btn:hover {
  color: rgba(255, 255, 255, 0.6);
  background: rgba(255, 255, 255, 0.04);
}

/* ===== Header ===== */
.crm-main {
  flex-direction: column;
}
.crm-header {
  height: 52px !important;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--crm-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  flex-shrink: 0;
}
.header-left {
  display: flex;
  align-items: center;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}
.header-badge {
  font-size: 12px;
  font-weight: 500;
  color: var(--crm-text-muted);
  letter-spacing: 0.02em;
}
.header-avatar {
  background: rgba(26, 26, 46, 0.06);
  color: var(--crm-text-muted);
}

/* ===== Content ===== */
.crm-content {
  background: transparent;
  padding: 32px;
  overflow-y: auto;
  flex: 1;
}

/* Page transition */
.page-enter-active,
.page-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.page-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.page-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
