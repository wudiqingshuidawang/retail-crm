<template>
  <div class="lead-pool">
    <!-- Search Bar -->
    <el-card shadow="hover" style="margin-bottom: 20px">
      <el-form :model="searchForm" inline label-width="60px" @keyup.enter="handleSearch">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="姓名">
              <el-input v-model="searchForm.name" placeholder="请输入客户姓名" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="电话">
              <el-input v-model="searchForm.phone" placeholder="请输入联系电话" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="等级">
              <el-select v-model="searchForm.level" placeholder="全部等级" clearable style="width: 100%">
                <el-option label="全部" value="" />
                <el-option label="高价值" value="高价值" />
                <el-option label="普通" value="普通" />
                <el-option label="沉睡" value="沉睡" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6" style="text-align: right">
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
              <el-button :icon="Refresh" @click="handleReset">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- Table -->
    <el-card shadow="hover">
      <el-table
        :data="poolList"
        stripe
        border
        highlight-current-row
        v-loading="loading"
        style="width: 100%"
        empty-text="公海池暂无客户"
      >
        <el-table-column prop="name" label="姓名" min-width="120" />
        <el-table-column prop="phone" label="电话" min-width="140" />
        <el-table-column prop="level" label="等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.level" :type="levelTagType(row.level)" size="small" effect="dark">
              {{ row.level }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="120" align="center">
          <template #default="{ row }">
            {{ row.source || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="80" align="center">
          <template #default="{ row }">
            {{ row.gender || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="lastFollowTime" label="最近跟进时间" width="180" align="center">
          <template #default="{ row }">
            {{ row.lastFollowTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              :icon="Plus"
              @click="handleClaim(row)"
              :loading="claimingId === row.id"
            >
              领取
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div style="display: flex; justify-content: flex-end; margin-top: 16px">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getPoolCustomers, claimCustomer } from '../../api/customer'

// Search
const searchForm = reactive({
  name: '',
  phone: '',
  level: ''
})

// Table
const poolList = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const claimingId = ref(null)

const levelTagMap = {
  '高价值': 'danger',
  '普通': '',
  '沉睡': 'info'
}

function levelTagType(level) {
  return levelTagMap[level] || ''
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: pageSize.value
    }
    if (searchForm.name) params.name = searchForm.name
    if (searchForm.phone) params.phone = searchForm.phone
    if (searchForm.level) params.level = searchForm.level

    const res = await getPoolCustomers(params)
    poolList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    poolList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchData()
}

function handleReset() {
  searchForm.name = ''
  searchForm.phone = ''
  searchForm.level = ''
  page.value = 1
  fetchData()
}

async function handleClaim(row) {
  claimingId.value = row.id
  try {
    await claimCustomer(row.id)
    ElMessage.success('领取成功')
    fetchData()
  } catch (e) {
    // Error handled by interceptor
  } finally {
    claimingId.value = null
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.lead-pool :deep(.el-form--inline .el-form-item) {
  margin-right: 0;
  width: 100%;
}
</style>
