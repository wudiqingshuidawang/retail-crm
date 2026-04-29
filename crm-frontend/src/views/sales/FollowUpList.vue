<template>
  <div class="follow-up-list">
    <!-- Search Bar -->
    <el-card shadow="hover" style="margin-bottom: 20px">
      <el-form :model="searchForm" inline label-width="80px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="状态">
              <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 100%">
                <el-option label="全部" value="" />
                <el-option label="待处理" value="待处理" />
                <el-option label="处理中" value="处理中" />
                <el-option label="已完成" value="已完成" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="16" style="text-align: right">
            <el-form-item>
              <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增回访</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- Tabs -->
    <el-card shadow="hover">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="回访" name="回访" />
        <el-tab-pane label="投诉" name="投诉" />
      </el-tabs>

      <!-- Table -->
      <el-table
        :data="list"
        stripe
        border
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="customerName" label="客户名称" min-width="120" />
        <el-table-column prop="type" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === '投诉' ? 'danger' : 'primary'" size="small" effect="plain">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="planTime" label="计划时间" width="170" align="center">
          <template #default="{ row }">
            {{ formatDate(row.planTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="actualTime" label="实际时间" width="170" align="center">
          <template #default="{ row }">
            {{ formatDate(row.actualTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容摘要" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.content || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small" effect="dark">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === '待处理'"
              type="success"
              link
              size="small"
              :icon="Select"
              @click="openCompleteDialog(row)"
            >
              完成
            </el-button>
            <span v-else>-</span>
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

    <!-- Create Dialog -->
    <el-dialog
      v-model="createVisible"
      title="新增回访"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="选择客户" prop="customerId">
          <el-select
            v-model="createForm.customerId"
            placeholder="请搜索并选择客户"
            filterable
            remote
            :remote-method="searchCustomers"
            :loading="customerLoading"
            style="width: 100%"
            clearable
          >
            <el-option
              v-for="c in customerOptions"
              :key="c.id"
              :label="c.name + (c.phone ? ' (' + c.phone + ')' : '')"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="createForm.type">
            <el-radio value="回访">回访</el-radio>
            <el-radio value="投诉">投诉</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="计划时间" prop="planTime">
          <el-date-picker
            v-model="createForm.planTime"
            type="datetime"
            placeholder="请选择计划时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="createForm.content" type="textarea" :rows="3" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleCreate">确认</el-button>
      </template>
    </el-dialog>

    <!-- Complete Dialog -->
    <el-dialog
      v-model="completeVisible"
      title="完成回访"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="completeFormRef" :model="completeForm" :rules="completeRules" label-width="100px">
        <el-form-item label="实际时间" prop="actualTime">
          <el-date-picker
            v-model="completeForm.actualTime"
            type="datetime"
            placeholder="请选择实际完成时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="完成内容" prop="content">
          <el-input v-model="completeForm.content" type="textarea" :rows="3" placeholder="请输入完成内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeVisible = false">取消</el-button>
        <el-button type="primary" :loading="completeLoading" @click="handleComplete">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Select } from '@element-plus/icons-vue'
import { getFollowUps, createFollowUp, updateFollowUp } from '../../api/sales'
import { getCustomers } from '../../api/customer'

// Search & tabs
const searchForm = reactive({
  status: ''
})
const activeTab = ref('回访')

// Table
const list = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

// Create dialog
const createVisible = ref(false)
const createFormRef = ref(null)
const submitLoading = ref(false)
const customerLoading = ref(false)
const customerOptions = ref([])

const createForm = reactive({
  customerId: null,
  type: '回访',
  planTime: '',
  content: ''
})

const createRules = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  planTime: [{ required: true, message: '请选择计划时间', trigger: 'change' }]
}

// Complete dialog
const completeVisible = ref(false)
const completeFormRef = ref(null)
const completeLoading = ref(false)
const currentFollowUpId = ref(null)

const completeForm = reactive({
  actualTime: '',
  content: ''
})

const completeRules = {
  actualTime: [{ required: true, message: '请选择实际时间', trigger: 'change' }],
  content: [{ required: true, message: '请输入完成内容', trigger: 'blur' }]
}

function statusTagType(status) {
  const map = {
    '待处理': 'warning',
    '处理中': 'primary',
    '已完成': 'success'
  }
  return map[status] || ''
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, size: pageSize.value, type: activeTab.value }
    if (searchForm.status) params.status = searchForm.status

    const res = await getFollowUps(params)
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  page.value = 1
  fetchData()
}

async function searchCustomers(query) {
  if (!query) {
    customerOptions.value = []
    return
  }
  customerLoading.value = true
  try {
    const res = await getCustomers({ page: 1, size: 20, name: query })
    customerOptions.value = res.data.records || []
  } catch (e) {
    customerOptions.value = []
  } finally {
    customerLoading.value = false
  }
}

function openCreateDialog() {
  createForm.customerId = null
  createForm.type = activeTab.value
  createForm.planTime = ''
  createForm.content = ''
  customerOptions.value = []
  createVisible.value = true
}

async function handleCreate() {
  if (!createFormRef.value) return
  try {
    await createFormRef.value.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    await createFollowUp({ ...createForm })
    ElMessage.success('创建成功')
    createVisible.value = false
    fetchData()
  } catch (e) {
    // Handled by interceptor
  } finally {
    submitLoading.value = false
  }
}

function openCompleteDialog(row) {
  currentFollowUpId.value = row.id
  completeForm.actualTime = new Date().toISOString().slice(0, 19)
  completeForm.content = row.content || ''
  completeVisible.value = true
}

async function handleComplete() {
  if (!completeFormRef.value) return
  try {
    await completeFormRef.value.validate()
  } catch {
    return
  }

  completeLoading.value = true
  try {
    await updateFollowUp(currentFollowUpId.value, {
      actualTime: completeForm.actualTime,
      content: completeForm.content,
      status: '已完成'
    })
    ElMessage.success('已完成')
    completeVisible.value = false
    fetchData()
  } catch (e) {
    // Handled by interceptor
  } finally {
    completeLoading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.follow-up-list :deep(.el-form--inline .el-form-item) {
  margin-right: 0;
  width: 100%;
}
</style>
