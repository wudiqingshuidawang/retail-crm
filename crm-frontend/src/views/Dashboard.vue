<template>
  <div class="dashboard">
    <h2 style="margin: 0 0 20px 0; color: #303133">欢迎使用零售业CRM系统</h2>

    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <el-card shadow="hover" :body-style="{ padding: '20px' }">
          <div class="stat-card">
            <div class="stat-icon" :style="{ background: card.bgColor }">
              <el-icon :size="28" color="#fff">
                <component :is="card.icon" />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <span>近7天销售额趋势</span>
          </template>
          <div ref="chartRef" style="height: 350px; width: 100%"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>快速操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" :icon="Plus" @click="$router.push('/customers')" style="margin-bottom: 12px; width: 100%">
              新增客户
            </el-button>
            <el-button :icon="Search" @click="$router.push('/customers')" style="margin-bottom: 12px; width: 100%">
              查询客户
            </el-button>
            <el-button :icon="Coin" @click="$router.push('/pool')" style="margin-bottom: 12px; width: 100%">
              公海池
            </el-button>
            <el-button type="success" :icon="ShoppingCart" @click="$router.push('/orders')" style="width: 100%">
              销售管理
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { User, Plus, TrendCharts, Coin, Search, ShoppingCart } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import request from '../utils/request'

const statCards = ref([
  { label: '客户总数', value: 0, icon: User, bgColor: 'linear-gradient(135deg, #409EFF, #337ecc)' },
  { label: '本月新增', value: 0, icon: Plus, bgColor: 'linear-gradient(135deg, #67C23A, #529b2e)' },
  { label: '待跟进线索', value: 0, icon: TrendCharts, bgColor: 'linear-gradient(135deg, #E6A23C, #cf9236)' },
  { label: '本月销售额', value: '¥0', icon: Coin, bgColor: 'linear-gradient(135deg, #F56C6C, #d95353)' }
])

const chartRef = ref(null)
let chartInstance = null

async function fetchStats() {
  try {
    const res = await request.get('/dashboard/stats')
    const data = res.data || {}
    statCards.value[0].value = data.totalCustomers ?? 0
    statCards.value[1].value = data.newCustomersThisMonth ?? 0
    statCards.value[2].value = data.pendingLeads ?? 0
    const amount = data.monthlySales || 0
    statCards.value[3].value = '¥' + Number(amount).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
  } catch (e) {
    // Keep default zeros
  }
}

async function fetchSalesTrend() {
  try {
    const res = await request.get('/analytics/sales-trend')
    const trend = res.data || []
    initChart(trend)
  } catch (e) {
    // No data
  }
}

function initChart(trendData) {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)

  const dates = trendData.map(d => d.date)
  const amounts = trendData.map(d => d.amount)

  const option = {
    tooltip: {
      trigger: 'axis',
      valueFormatter: (value) => '¥' + Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        color: '#909399'
      },
      axisLine: {
        lineStyle: { color: '#e6e6e6' }
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        color: '#909399',
        formatter: (val) => '¥' + val
      },
      splitLine: {
        lineStyle: { color: '#f0f0f0' }
      }
    },
    series: [
      {
        type: 'line',
        data: amounts,
        smooth: true,
        showSymbol: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 3,
          color: '#409EFF'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        },
        itemStyle: {
          color: '#409EFF'
        }
      }
    ]
  }

  chartInstance.setOption(option)
}

function handleResize() {
  chartInstance?.resize()
}

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
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-info {
  flex: 1;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}
.quick-actions {
  display: flex;
  flex-direction: column;
}
</style>
