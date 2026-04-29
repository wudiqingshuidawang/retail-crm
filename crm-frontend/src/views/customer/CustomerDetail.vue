<template>
  <div class="customer-detail">
    <!-- Header -->
    <div style="display: flex; align-items: center; margin-bottom: 20px">
      <el-button :icon="ArrowLeft" @click="$router.push('/customers')" text style="margin-right: 12px">
        返回
      </el-button>
      <h3 style="margin: 0; color: #303133">客户详情</h3>
    </div>

    <!-- Loading -->
    <el-skeleton :loading="loading" animated :count="3" v-if="loading" />

    <!-- Empty -->
    <el-empty v-else-if="!customer" description="未找到客户信息" />

    <!-- Content -->
    <template v-else>
      <el-card shadow="hover" style="margin-bottom: 20px">
        <template #header>
          <div style="display: flex; align-items: center; justify-content: space-between">
            <span>基本信息</span>
            <div>
              <el-button v-if="!customer.ownerId" type="primary" size="small" :icon="Coin" @click="handleClaim">
                领取客户
              </el-button>
              <el-button type="warning" size="small" :icon="Edit" @click="handleEdit">
                编辑
              </el-button>
            </div>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名" align="center">
            {{ customer.name || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="电话" align="center">
            {{ customer.phone || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱" align="center">
            {{ customer.email || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="性别" align="center">
            {{ customer.gender || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="年龄" align="center">
            {{ customer.age ?? '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="来源" align="center">
            {{ customer.source || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="等级" align="center">
            <el-tag v-if="customer.level" :type="levelTagType(customer.level)" size="small" effect="dark">
              {{ customer.level }}
            </el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="归属员工" align="center">
            {{ customer.ownerId || '未分配' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态" align="center">
            <el-tag :type="customer.status === 'active' ? 'success' : 'info'" size="small">
              {{ customer.status === 'active' ? '正常' : customer.status || '未知' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" align="center">
            {{ customer.created_at || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" align="center" :span="2">
            {{ customer.remark || '无' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- Follow-up Info -->
      <el-card shadow="hover">
        <template #header>
          <span>跟进信息</span>
        </template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="跟进状态" align="center">
            <el-tag :type="followStatusType" size="small">
              {{ followStatus || '未跟进' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="跟进次数" align="center">
            {{ leadInfo?.followCount ?? 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="最近跟进时间" align="center">
            {{ leadInfo?.lastFollowTime || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Coin, Edit } from '@element-plus/icons-vue'
import { getCustomer, claimCustomer, updateCustomer } from '../../api/customer'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const customer = ref(null)

const levelTagMap = {
  '高价值': 'danger',
  '普通': '',
  '沉睡': 'info'
}

function levelTagType(level) {
  return levelTagMap[level] || ''
}

const leadInfo = computed(() => {
  return customer.value?.leadInfo || {}
})

const followStatus = computed(() => {
  return customer.value?.leadInfo?.followStatus || ''
})

const followStatusType = computed(() => {
  const status = followStatus.value
  if (status === '已跟进') return 'success'
  if (status === '跟进中') return 'warning'
  return 'info'
})

async function fetchDetail() {
  loading.value = true
  try {
    const id = route.params.id
    const res = await getCustomer(id)
    customer.value = res.data || null
  } catch (e) {
    customer.value = null
  } finally {
    loading.value = false
  }
}

async function handleClaim() {
  try {
    await claimCustomer(route.params.id)
    ElMessage.success('领取成功')
    fetchDetail()
  } catch (e) {
    // Error handled by interceptor
  }
}

async function handleEdit() {
  try {
    await ElMessageBox({
      title: '编辑客户',
      message: '编辑功能请在客户列表页操作',
      type: 'info',
      confirmButtonText: '去列表页',
      showCancelButton: true,
      cancelButtonText: '取消'
    })
    router.push('/customers')
  } catch {
    // cancelled
  }
}

onMounted(() => {
  fetchDetail()
})
</script>
