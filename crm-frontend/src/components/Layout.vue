<template>
  <el-container class="crm-layout">
    <!-- Sidebar -->
    <el-aside :width="isCollapse ? '68px' : '232px'" class="crm-sidebar">
      <div class="sidebar-brand" :class="{ collapsed: isCollapse }">
        <div class="brand-icon">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <rect x="3" y="3" width="7" height="7" rx="1"/>
            <rect x="14" y="3" width="7" height="7" rx="1"/>
            <rect x="3" y="14" width="7" height="7" rx="1"/>
            <rect x="14" y="14" width="7" height="7" rx="1"/>
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
          <el-icon :size="18"><component :is="item.icon" /></el-icon>
          <span class="nav-label">{{ item.label }}</span>
          <span v-if="route.path === item.path || route.path.startsWith(item.path + '/')" class="nav-indicator" />
        </router-link>
      </div>

      <div class="sidebar-footer">
        <button class="collapse-btn" @click="isCollapse = !isCollapse" :title="isCollapse ? '展开' : '收起'">
          <el-icon :size="16">
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
          <el-breadcrumb separator="">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <template v-if="route.meta.title">
              <span class="breadcrumb-sep">/</span>
              <el-breadcrumb-item>{{ route.meta.title }}</el-breadcrumb-item>
            </template>
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
</template>

<script setup>
import { ref, computed } from 'vue'
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
}

/* Sidebar */
.crm-sidebar {
  background: var(--crm-sidebar);
  display: flex;
  flex-direction: column;
  transition: width 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  position: relative;
}

.sidebar-brand {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 12px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}
.sidebar-brand.collapsed {
  padding: 0 20px;
  justify-content: center;
}
.brand-icon {
  color: #60a5fa;
  display: flex;
  flex-shrink: 0;
}
.brand-text {
  font-size: 18px;
  font-weight: 700;
  color: #f1f5f9;
  letter-spacing: 0.04em;
  white-space: nowrap;
}

/* Nav */
.sidebar-nav {
  flex: 1;
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 6px;
  color: var(--crm-sidebar-text);
  text-decoration: none;
  font-size: 14px;
  position: relative;
  transition: all 0.15s ease;
  white-space: nowrap;
  overflow: hidden;
}
.nav-item:hover {
  color: #e2e8f0;
  background: var(--crm-sidebar-hover);
}
.nav-item.active {
  color: #fff;
  background: rgba(37, 99, 235, 0.25);
}
.nav-indicator {
  position: absolute;
  left: 0;
  top: 10px;
  bottom: 10px;
  width: 3px;
  background: var(--crm-sidebar-active);
  border-radius: 0 2px 2px 0;
}
.nav-label {
  transition: opacity 0.2s ease;
}

/* Sidebar footer */
.sidebar-footer {
  padding: 8px;
  border-top: 1px solid rgba(255,255,255,0.06);
}
.collapse-btn {
  width: 100%;
  padding: 8px;
  border: none;
  background: transparent;
  color: var(--crm-sidebar-text);
  cursor: pointer;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s ease;
}
.collapse-btn:hover {
  color: #e2e8f0;
  background: var(--crm-sidebar-hover);
}

/* Header */
.crm-main {
  flex-direction: column;
}
.crm-header {
  height: 48px !important;
  background: var(--crm-surface);
  border-bottom: 1px solid var(--crm-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
}
.header-left {
  display: flex;
  align-items: center;
}
.breadcrumb-sep {
  margin: 0 6px;
  color: var(--crm-text-muted);
  font-size: 13px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.header-badge {
  font-size: 13px;
  color: var(--crm-text-secondary);
}
.header-avatar {
  background: #e2e8f0;
  color: var(--crm-text-muted);
}

/* Content */
.crm-content {
  background: var(--crm-bg);
  padding: 20px 24px;
  overflow-y: auto;
  flex: 1;
}

/* Page transition */
.page-enter-active,
.page-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.page-enter-from {
  opacity: 0;
  transform: translateY(6px);
}
.page-leave-to {
  opacity: 0;
  transform: translateY(-6px);
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
