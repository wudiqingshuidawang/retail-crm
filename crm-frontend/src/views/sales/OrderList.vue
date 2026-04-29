<template>
  <div class="order-list">
    <!-- Search Bar -->
    <el-card shadow="hover" style="margin-bottom: 20px">
      <el-form :model="searchForm" inline label-width="80px" @keyup.enter="handleSearch">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="客户名称">
              <el-input v-model="searchForm.customerName" placeholder="请输入客户名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="订单状态">
              <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 100%">
                <el-option label="全部" value="" />
                <el-option label="待付款" value="待付款" />
                <el-option label="已付款" value="已付款" />
                <el-option label="已退款" value="已退款" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8" style="text-align: right">
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
              <el-button :icon="Refresh" @click="handleReset">重置</el-button>
              <el-button type="primary" :icon="Plus" @click="openCreateDialog" style="margin-left: 8px">新增订单</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- Table -->
    <el-card shadow="hover">
      <el-table
        :data="orderList"
        stripe
        border
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="id" label="订单ID" width="80" align="center" />
        <el-table-column prop="customerName" label="客户名称" min-width="120" />
        <el-table-column prop="totalAmount" label="金额" width="120" align="center">
          <template #default="{ row }">
            ¥{{ row.totalAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small" effect="dark">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" :icon="View" @click="viewDetail(row)">
              查看详情
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

    <!-- Create Order Dialog -->
    <el-dialog
      v-model="dialogVisible"
      title="新增订单"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="选择客户" prop="customerId">
          <el-select
            v-model="form.customerId"
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
        <el-form-item label="总金额" prop="totalAmount">
          <el-input-number
            v-model="form.totalAmount"
            :min="0"
            :precision="2"
            style="width: 100%"
            placeholder="请输入总金额"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="待付款" value="待付款" />
            <el-option label="已付款" value="已付款" />
            <el-option label="已退款" value="已退款" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>

        <!-- Order Items -->
        <el-form-item label="订单明细">
          <div style="width: 100%">
            <div v-for="(item, index) in form.items" :key="index" style="display: flex; gap: 8px; margin-bottom: 8px; align-items: center">
              <el-input v-model="item.productName" placeholder="商品名称" style="width: 200px" />
              <el-input-number v-model="item.qty" :min="1" placeholder="数量" style="width: 120px" />
              <el-input-number v-model="item.price" :min="0" :precision="2" placeholder="单价" style="width: 150px" />
              <el-button type="danger" :icon="Delete" circle size="small" @click="removeItem(index)" />
            </div>
            <el-button type="primary" link :icon="Plus" @click="addItem">添加商品</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>

    <!-- Detail Dialog -->
    <el-dialog
      v-model="detailVisible"
      title="订单详情"
      width="700px"
    >
      <template v-if="detailData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单ID" width="120">{{ detailData.id }}</el-descriptions-item>
          <el-descriptions-item label="客户名称">{{ detailData.customerName }}</el-descriptions-item>
          <el-descriptions-item label="总金额">
            <span style="color: #f56c6c; font-weight: bold">¥{{ detailData.totalAmount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detailData.status)" size="small" effect="dark">
              {{ detailData.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ formatDate(detailData.createdAt) }}</el-descriptions-item>
        </el-descriptions>
        <el-divider />
        <h4 style="margin: 0 0 12px 0">商品明细</h4>
        <el-table :data="detailData.items || []" border stripe size="small">
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column prop="qty" label="数量" width="80" align="center" />
          <el-table-column prop="price" label="单价" width="120" align="center">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column label="小计" width="120" align="center">
            <template #default="{ row }">¥{{ (row.price * row.qty).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </template>
      <div v-else v-loading="detailLoading" style="height: 200px" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, View, Delete } from '@element-plus/icons-vue'
import { getOrders, getOrder, createOrder } from '../../api/sales'
import { getCustomers } from '../../api/customer'

// Search
const searchForm = reactive({
  customerName: '',
  status: ''
})

// Table
const orderList = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

// Dialog
const dialogVisible = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const customerLoading = ref(false)
const customerOptions = ref([])

const form = reactive({
  customerId: null,
  totalAmount: 0,
  status: '待付款',
  remark: '',
  items: []
})

const formRules = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  totalAmount: [{ required: true, message: '请输入总金额', trigger: 'blur' }]
}

// Detail
const detailVisible = ref(false)
const detailData = ref(null)
const detailLoading = ref(false)

function statusTagType(status) {
  const map = {
    '待付款': 'warning',
    '已付款': 'success',
    '已退款': 'danger'
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
    const params = { page: page.value, size: pageSize.value }
    if (searchForm.customerName) params.customerName = searchForm.customerName
    if (searchForm.status) params.status = searchForm.status

    const res = await getOrders(params)
    orderList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    orderList.value = []
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
  searchForm.customerName = ''
  searchForm.status = ''
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

function addItem() {
  form.items.push({ productName: '', qty: 1, price: 0 })
}

function removeItem(index) {
  form.items.splice(index, 1)
}

function openCreateDialog() {
  form.customerId = null
  form.totalAmount = 0
  form.status = '待付款'
  form.remark = ''
  form.items = [{ productName: '', qty: 1, price: 0 }]
  customerOptions.value = []
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    await createOrder({ ...form })
    ElMessage.success('订单创建成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    // Handled by interceptor
  } finally {
    submitLoading.value = false
  }
}

async function viewDetail(row) {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    const res = await getOrder(row.id)
    detailData.value = res.data
  } catch (e) {
    detailData.value = null
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.order-list :deep(.el-form--inline .el-form-item) {
  margin-right: 0;
  width: 100%;
}
</style>
