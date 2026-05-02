<template>
  <div class="dashboard">
    <!-- Welcome -->
    <div class="welcome">
      <div class="welcome-left">
        <h1 class="welcome-title">{{ greeting }}</h1>
        <p class="welcome-date">{{ today }}</p>
      </div>
    </div>

    <!-- Stat Cards -->
    <div class="stat-grid">
      <div v-for="card in statCards" :key="card.label" class="stat-card">
        <div class="stat-top">
          <span class="stat-label">{{ card.label }}</span>
          <div class="stat-icon-wrap" :style="{ color: card.color }">
            <el-icon :size="16"><component :is="card.icon" /></el-icon>
          </div>
        </div>
        <div class="stat-value crm-mono">{{ card.value }}</div>
        <div class="stat-trend" :class="card.trendDir">
          <span class="trend-arrow" v-if="card.trendDir === 'up'">↑</span>
          <span class="trend-arrow" v-else-if="card.trendDir === 'down'">↓</span>
          <span>{{ card.trend }}</span>
        </div>
      </div>
    </div>

    <!-- Chart -->
    <div class="card chart-section">
      <div class="card-header">
        <span class="card-title">销售趋势</span>
        <div class="period-tabs">
          <button
            v-for="p in periods"
            :key="p.value"
            class="period-tab"
            :class="{ active: selectedPeriod === p.value }"
            @click="changePeriod(p.value)"
          >{{ p.label }}</button>
        </div>
      </div>
      <div ref="chartRef" class="chart-body"></div>
      <div v-if="!hasTrendData" class="chart-empty">
        <div class="empty-visual">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M3 3v18h18"/>
            <path d="M7 14l4-4 3 3 5-5"/>
          </svg>
        </div>
        <p class="empty-label">暂无数据</p>
        <p class="empty-desc">创建订单后将在此展示趋势</p>
      </div>
    </div>

    <!-- Bottom: Quick Actions + Activity + Todos -->
    <div class="bottom-row">
      <!-- Quick Actions -->
      <div class="card quick-section">
        <div class="card-header">
          <span class="card-title">快捷操作</span>
        </div>
        <div class="quick-list">
          <button class="quick-item" @click="$router.push('/customers')">
            <el-icon :size="16"><Plus /></el-icon>
            <span>新增客户</span>
          </button>
          <button class="quick-item" @click="$router.push('/pool')">
            <el-icon :size="16"><Coin /></el-icon>
            <span>公海池领取</span>
          </button>
          <button class="quick-item" @click="$router.push('/orders')">
            <el-icon :size="16"><ShoppingCart /></el-icon>
            <span>新增订单</span>
          </button>
          <button class="quick-item" @click="$router.push('/follow-ups')">
            <el-icon :size="16"><ChatDotSquare /></el-icon>
            <span>回访记录</span>
          </button>
        </div>
      </div>

      <!-- Recent Activity -->
      <div class="card activity-section">
        <div class="card-header">
          <span class="card-title">最近动态</span>
        </div>
        <div class="activity-list">
          <div v-for="(item, i) in recentActivities" :key="i" class="activity-item">
            <div class="activity-line" :style="{ background: item.color }"></div>
            <div class="activity-body">
              <span class="activity-text">{{ item.text }}</span>
              <span class="activity-time">{{ item.time }}</span>
            </div>
          </div>
          <div v-if="recentActivities.length === 0" class="section-empty">
            <p>暂无动态</p>
          </div>
        </div>
      </div>

      <!-- Todo Summary -->
      <div class="card todo-section">
        <div class="card-header">
          <span class="card-title">待办事项</span>
          <router-link to="/todos" class="card-link">全部 →</router-link>
        </div>
        <div class="todo-body">
          <div class="todo-summary">
            <div class="todo-big-num crm-mono">{{ todoPending }}</div>
            <div class="todo-big-label">项待处理</div>
          </div>
          <div class="todo-items">
            <div v-for="(item, i) in todoItems" :key="i" class="todo-row">
              <span class="todo-type-dot" :style="{ background: todoColor(item.type) }"></span>
              <span class="todo-text">{{ item.content }}</span>
              <span class="todo-deadline">{{ item.deadline }}</span>
            </div>
            <div v-if="todoItems.length === 0" class="section-empty">
              <p>暂无待办</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { User, Plus, TrendCharts, Coin, ShoppingCart, ChatDotSquare } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import request from '../utils/request'
import { getTodos } from '../api/employee'

const today = new Date().toLocaleDateString('zh-CN', { year:'numeric', month:'long', day:'numeric', weekday:'long' })

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了，注意休息'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const statCards = ref([
  { label: '客户总数', value: '0', icon: User, color: '#1a1a2e', trend: '较上月', trendDir: 'flat' },
  { label: '本月新增', value: '0', icon: Plus, color: '#6b9e7a', trend: '较上月', trendDir: 'flat' },
  { label: '待跟进', value: '0', icon: TrendCharts, color: '#c9a87c', trend: '需处理', trendDir: 'flat' },
  { label: '本月销售额', value: '¥0', icon: Coin, color: '#c47a7a', trend: '较上月', trendDir: 'flat' },
])

const periods = [
  { label: '7天', value: 7 },
  { label: '30天', value: 30 },
  { label: '90天', value: 90 },
]
const selectedPeriod = ref(7)

const chartRef = ref(null)
let chartInstance = null
const hasTrendData = ref(false)

const recentActivities = ref([])
const todoPending = ref(0)
const todoItems = ref([])

function todoColor(t) { return { '投诉':'#c47a7a', '回访':'#c9a87c', '其他':'#7a8ec4' }[t] || '#9e9eb0' }

async function fetchStats() {
  try {
    const res = await request.get('/dashboard/stats')
    const data = res.data || {}
    statCards.value[0].value = (data.totalCustomers ?? 0).toLocaleString()
    statCards.value[1].value = (data.newCustomersThisMonth ?? 0).toLocaleString()
    statCards.value[2].value = (data.pendingLeads ?? 0).toLocaleString()
    const amount = Number(data.monthlySales || 0)
    statCards.value[3].value = '¥' + amount.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })
    buildRecentActivities(data)
  } catch { /* defaults */ }
}

async function fetchSalesTrend() {
  try {
    const res = await request.get('/analytics/sales-trend', { params: { days: selectedPeriod.value } })
    const trend = res.data || []
    if (trend.length > 0) {
      hasTrendData.value = true
      await nextTick()
      initChart(trend)
    } else {
      hasTrendData.value = false
    }
  } catch { hasTrendData.value = false }
}

function changePeriod(days) {
  selectedPeriod.value = days
  if (chartInstance) { chartInstance.dispose(); chartInstance = null }
  hasTrendData.value = false
  fetchSalesTrend()
}

function initChart(data) {
  if (!chartRef.value) return
  chartInstance = echarts.init(chartRef.value)

  chartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: 'rgba(0,0,0,0.06)',
      textStyle: { color: '#1a1a2e', fontSize: 13 },
      boxShadow: '0 4px 16px rgba(0,0,0,0.06)',
      valueFormatter: (v) => '¥' + Number(v).toLocaleString()
    },
    grid: { left: 8, right: 16, top: 16, bottom: 0, containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(d => d.date),
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#9e9eb0', fontSize: 11 },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(0,0,0,0.04)' } },
      axisLabel: { color: '#9e9eb0', fontSize: 11, formatter: (v) => '¥' + v },
    },
    series: [{
      type: 'line',
      data: data.map(d => d.amount),
      smooth: true,
      symbol: 'circle',
      symbolSize: 5,
      showSymbol: false,
      lineStyle: { width: 2, color: '#1a1a2e' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(26,26,46,0.08)' },
          { offset: 1, color: 'rgba(26,26,46,0.0)' }
        ])
      },
      itemStyle: { color: '#1a1a2e' },
    }]
  })
}

function buildRecentActivities(stats) {
  const items = []
  if (stats?.totalCustomers > 0) items.push({ text: `系统共有 ${stats.totalCustomers} 位客户`, color: '#1a1a2e', time: '刚刚' })
  if (stats?.newCustomersThisMonth > 0) items.push({ text: `本月新增 ${stats.newCustomersThisMonth} 位客户`, color: '#6b9e7a', time: '本月' })
  if (stats?.pendingLeads > 0) items.push({ text: `${stats.pendingLeads} 条线索待跟进`, color: '#c9a87c', time: '待处理' })
  if (stats?.monthlySales > 0) items.push({ text: `本月销售额 ¥${Number(stats.monthlySales).toLocaleString()}`, color: '#c47a7a', time: '本月' })
  if (items.length === 0) items.push({ text: '系统已就绪，开始录入数据', color: '#c9a87c', time: '刚刚' })
  recentActivities.value = items
}

async function fetchTodos() {
  try {
    const res = await getTodos({ page: 1, size: 5 })
    todoPending.value = res.data?.total || 0
    todoItems.value = (res.data?.records || []).slice(0, 4)
  } catch { /* defaults */ }
}

function handleResize() { chartInstance?.resize() }

onMounted(async () => {
  await fetchStats()
  await nextTick()
  fetchSalesTrend()
  fetchTodos()
  window.addEventListener('resize', handleResize)
})
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
/* ===== Welcome ===== */
.welcome {
  margin-bottom: 32px;
}
.welcome-title {
  font-size: 28px;
  font-weight: 300;
  color: var(--crm-text);
  margin: 0;
  letter-spacing: -0.02em;
}
.welcome-date {
  font-size: 13px;
  color: var(--crm-text-muted);
  margin: 6px 0 0;
  font-weight: 400;
  letter-spacing: 0.02em;
}

/* ===== Stat Grid ===== */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}
.stat-card {
  background: var(--crm-surface);
  backdrop-filter: blur(var(--crm-glass-blur));
  -webkit-backdrop-filter: blur(var(--crm-glass-blur));
  border-radius: var(--crm-radius);
  padding: 20px 22px;
  border: 1px solid var(--crm-border);
  box-shadow: var(--crm-shadow);
  transition: box-shadow 0.3s ease;
}
.stat-card:hover {
  box-shadow: var(--crm-shadow-hover);
}
.stat-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.stat-label {
  font-size: 12px;
  color: var(--crm-text-muted);
  font-weight: 500;
  letter-spacing: 0.04em;
}
.stat-icon-wrap {
  opacity: 0.5;
}
.stat-value {
  font-size: 30px;
  font-weight: 700;
  color: var(--crm-text);
  line-height: 1;
  letter-spacing: -0.02em;
}
.stat-trend {
  font-size: 11px;
  font-weight: 500;
  margin-top: 10px;
  color: var(--crm-text-muted);
  display: flex;
  align-items: center;
  gap: 4px;
}
.stat-trend.up { color: #6b9e7a; }
.stat-trend.down { color: #c47a7a; }

/* ===== Card Base ===== */
.card {
  background: var(--crm-surface);
  backdrop-filter: blur(var(--crm-glass-blur));
  -webkit-backdrop-filter: blur(var(--crm-glass-blur));
  border-radius: var(--crm-radius);
  border: 1px solid var(--crm-border);
  box-shadow: var(--crm-shadow);
}
.card-header {
  padding: 16px 22px;
  border-bottom: 1px solid var(--crm-border-subtle);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--crm-text);
  letter-spacing: 0.02em;
}
.card-link {
  font-size: 12px;
  color: var(--crm-text-muted);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}
.card-link:hover {
  color: var(--crm-text);
}

/* ===== Chart ===== */
.chart-section {
  margin-bottom: 20px;
}
.period-tabs {
  display: flex;
  gap: 2px;
  background: rgba(26, 26, 46, 0.03);
  border-radius: 6px;
  padding: 2px;
}
.period-tab {
  padding: 4px 12px;
  border: none;
  background: transparent;
  color: var(--crm-text-muted);
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s ease;
}
.period-tab.active {
  background: var(--crm-surface-solid);
  color: var(--crm-text);
  box-shadow: 0 1px 2px rgba(0,0,0,0.06);
}
.period-tab:hover:not(.active) {
  color: var(--crm-text-secondary);
}

.chart-body {
  height: 280px;
  padding: 8px 4px;
}
.chart-empty {
  height: 280px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
}
.empty-visual {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(26, 26, 46, 0.03);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--crm-text-muted);
}
.empty-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--crm-text-secondary);
  margin: 0;
}
.empty-desc {
  font-size: 12px;
  color: var(--crm-text-muted);
  margin: 0;
}

/* ===== Bottom Row ===== */
.bottom-row {
  display: grid;
  grid-template-columns: 240px 1fr 1fr;
  gap: 12px;
}

/* Quick Actions */
.quick-list {
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.quick-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--crm-text-secondary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
  width: 100%;
}
.quick-item:hover {
  background: rgba(26, 26, 46, 0.03);
  color: var(--crm-text);
}

/* Activity */
.activity-list {
  padding: 6px 14px;
}
.activity-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 4px;
  border-bottom: 1px solid var(--crm-border-subtle);
}
.activity-item:last-child {
  border-bottom: none;
}
.activity-line {
  width: 2px;
  height: 16px;
  border-radius: 1px;
  flex-shrink: 0;
  opacity: 0.5;
}
.activity-body {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-width: 0;
}
.activity-text {
  font-size: 13px;
  color: var(--crm-text-secondary);
  font-weight: 400;
}
.activity-time {
  font-size: 11px;
  color: var(--crm-text-muted);
  flex-shrink: 0;
  margin-left: 16px;
}

/* Todo */
.todo-body {
  padding: 16px 20px;
}
.todo-summary {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--crm-border-subtle);
}
.todo-big-num {
  font-size: 32px;
  font-weight: 700;
  color: var(--crm-text);
  line-height: 1;
  letter-spacing: -0.02em;
}
.todo-big-label {
  font-size: 13px;
  color: var(--crm-text-muted);
  font-weight: 400;
}
.todo-items {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.todo-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
}
.todo-type-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
  opacity: 0.6;
}
.todo-text {
  flex: 1;
  font-size: 13px;
  color: var(--crm-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.todo-deadline {
  font-size: 11px;
  color: var(--crm-text-muted);
  flex-shrink: 0;
}

.section-empty {
  padding: 24px 0;
  text-align: center;
}
.section-empty p {
  color: var(--crm-text-muted);
  font-size: 12px;
  margin: 0;
}
</style>
