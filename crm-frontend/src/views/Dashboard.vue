<template>
  <div class="dashboard">
    <div class="crm-page-header">
      <h1 class="crm-page-title">概览</h1>
      <span class="header-date">{{ today }}</span>
    </div>

    <!-- Stat Cards -->
    <div class="stat-grid">
      <div
        v-for="card in statCards"
        :key="card.label"
        class="stat-card"
        :class="card.accent"
      >
        <div class="stat-icon" :style="{ background: card.color }">
          <el-icon :size="20" color="#fff"><component :is="card.icon" /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-value crm-mono">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </div>
    </div>

    <!-- Chart + Actions -->
    <div class="content-grid">
      <div class="content-main">
        <div class="card chart-card">
          <div class="card-header">
            <span class="card-title">近7天销售额</span>
          </div>
          <div ref="chartRef" class="chart-container"></div>
          <div v-if="!hasTrendData" class="chart-empty">
            <el-empty description="暂无销售数据" :image-size="60" />
          </div>
        </div>
      </div>
      <div class="content-side">
        <div class="card quick-card">
          <div class="card-header">
            <span class="card-title">快捷操作</span>
          </div>
          <div class="quick-list">
            <button class="quick-btn primary" @click="$router.push('/customers')">
              <el-icon :size="16"><Plus /></el-icon>
              <span>新增客户</span>
            </button>
            <button class="quick-btn" @click="$router.push('/pool')">
              <el-icon :size="16"><Coin /></el-icon>
              <span>公海池领取</span>
            </button>
            <button class="quick-btn" @click="$router.push('/orders')">
              <el-icon :size="16"><ShoppingCart /></el-icon>
              <span>新增订单</span>
            </button>
            <button class="quick-btn" @click="$router.push('/follow-ups')">
              <el-icon :size="16"><ChatDotSquare /></el-icon>
              <span>回访记录</span>
            </button>
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

const today = new Date().toLocaleDateString('zh-CN', { year:'numeric', month:'long', day:'numeric', weekday:'short' })

const statCards = ref([
  { label: '客户总数', value: 0, icon: User, color: '#2563eb', accent: 'blue' },
  { label: '本月新增', value: 0, icon: Plus, color: '#16a34a', accent: 'green' },
  { label: '待跟进线索', value: 0, icon: TrendCharts, color: '#d97706', accent: 'amber' },
  { label: '本月销售额', value: '¥0', icon: Coin, color: '#dc2626', accent: 'red' },
])

const chartRef = ref(null)
let chartInstance = null
const hasTrendData = ref(false)

async function fetchStats() {
  try {
    const res = await request.get('/dashboard/stats')
    const data = res.data || {}
    statCards.value[0].value = (data.totalCustomers ?? 0).toLocaleString()
    statCards.value[1].value = (data.newCustomersThisMonth ?? 0).toLocaleString()
    statCards.value[2].value = (data.pendingLeads ?? 0).toLocaleString()
    const amount = Number(data.monthlySales || 0)
    statCards.value[3].value = '¥' + amount.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })
  } catch { /* defaults */ }
}

async function fetchSalesTrend() {
  try {
    const res = await request.get('/analytics/sales-trend')
    const trend = res.data || []
    if (trend.length > 0) {
      hasTrendData.value = true
      await nextTick()
      initChart(trend)
    }
  } catch { /* no data */ }
}

function initChart(data) {
  if (!chartRef.value) return
  chartInstance = echarts.init(chartRef.value)

  chartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#e2e8f0',
      textStyle: { color: '#1e293b', fontSize: 13, fontFamily: 'var(--crm-font-mono)' },
      boxShadow: '0 4px 16px rgba(0,0,0,0.08)',
      valueFormatter: (v) => '¥' + Number(v).toLocaleString()
    },
    grid: { left: 8, right: 16, top: 8, bottom: 0, containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(d => d.date),
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisTick: { show: false },
      axisLabel: { color: '#94a3b8', fontSize: 11 },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } },
      axisLabel: { color: '#94a3b8', fontSize: 11, formatter: (v) => '¥' + v },
    },
    series: [{
      type: 'line',
      data: data.map(d => d.amount),
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      showSymbol: false,
      lineStyle: { width: 2.5, color: '#2563eb' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(37,99,235,0.12)' },
          { offset: 1, color: 'rgba(37,99,235,0.0)' }
        ])
      },
      itemStyle: { color: '#2563eb' },
    }]
  })
}

function handleResize() { chartInstance?.resize() }

onMounted(async () => {
  await fetchStats()
  await nextTick()
  fetchSalesTrend()
  window.addEventListener('resize', handleResize)
})
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.header-date {
  font-size: 13px;
  color: var(--crm-text-muted);
}

/* Stat Grid */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.stat-card {
  background: var(--crm-surface);
  border-radius: var(--crm-radius);
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid var(--crm-border);
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}
.stat-card:hover {
  box-shadow: var(--crm-shadow-hover);
  transform: translateY(-1px);
}
.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: var(--crm-text);
  line-height: 1.1;
}
.stat-label {
  font-size: 12px;
  color: var(--crm-text-muted);
  margin-top: 4px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

/* Content Grid */
.content-grid {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 16px;
}
.card {
  background: var(--crm-surface);
  border-radius: var(--crm-radius);
  border: 1px solid var(--crm-border);
}
.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--crm-border);
}
.card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--crm-text);
}
.chart-container {
  height: 320px;
  padding: 8px 4px;
}
.chart-empty {
  height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Quick Actions */
.quick-list {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.quick-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border: 1px solid var(--crm-border);
  border-radius: 6px;
  background: var(--crm-surface);
  color: var(--crm-text);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s ease;
  width: 100%;
}
.quick-btn:hover {
  border-color: #93c5fd;
  background: #f0f7ff;
  color: var(--crm-primary);
}
.quick-btn.primary {
  background: var(--crm-primary);
  border-color: var(--crm-primary);
  color: #fff;
}
.quick-btn.primary:hover {
  background: var(--crm-primary-light);
  border-color: var(--crm-primary-light);
}
</style>
