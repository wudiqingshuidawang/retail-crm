<template>
  <div class="customer-list">
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
              <el-button type="primary" :icon="Plus" @click="openCreateDialog" style="margin-left: 8px">新增客户</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- Table -->
    <el-card shadow="hover">
      <el-table
        :data="customerList"
        stripe
        border
        highlight-current-row
        v-loading="loading"
        style="width: 100%"
        empty-text="暂无数据"
      >
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="phone" label="电话" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column prop="gender" label="性别" width="70" align="center">
          <template #default="{ row }">
            {{ row.gender || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="70" align="center">
          <template #default="{ row }">
            {{ row.age ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="100" align="center">
          <template #default="{ row }">
            {{ row.source || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="level" label="等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.level" :type="levelTagType(row.level)" size="small" effect="dark">
              {{ row.level }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="ownerId" label="归属员工" width="100" align="center">
          <template #default="{ row }">
            {{ row.ownerId || '未分配' }}
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="创建时间" width="170" align="center" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" :icon="View" @click="viewDetail(row)">
              查看
            </el-button>
            <el-button type="warning" link size="small" :icon="Edit" @click="openEditDialog(row)">
              编辑
            </el-button>
            <el-popconfirm title="确定要删除该客户吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link size="small" :icon="Delete">
                  删除
                </el-button>
              </template>
            </el-popconfirm>
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

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑客户' : '新增客户'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="80px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择" style="width: 100%">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="form.age" :min="0" :max="150" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源" prop="source">
              <el-input v-model="form.source" placeholder="请输入来源" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="等级" prop="level">
              <el-select v-model="form.level" placeholder="请选择" style="width: 100%">
                <el-option label="高价值" value="高价值" />
                <el-option label="普通" value="普通" />
                <el-option label="沉睡" value="沉睡" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, View, Edit, Delete } from '@element-plus/icons-vue'
import { getCustomers, createCustomer, updateCustomer, deleteCustomer } from '../../api/customer'

const router = useRouter()

// Search
const searchForm = reactive({
  name: '',
  phone: '',
  level: ''
})

// Table
const customerList = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

// Dialog
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)
const submitLoading = ref(false)

const form = reactive({
  name: '',
  phone: '',
  email: '',
  gender: '',
  age: null,
  source: '',
  level: '',
  remark: ''
})

const formRules = {
  name: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }]
}

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

    const res = await getCustomers(params)
    customerList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    customerList.value = []
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

function resetForm() {
  form.name = ''
  form.phone = ''
  form.email = ''
  form.gender = ''
  form.age = null
  form.source = ''
  form.level = ''
  form.remark = ''
  editId.value = null
}

function openCreateDialog() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  editId.value = row.id
  form.name = row.name || ''
  form.phone = row.phone || ''
  form.email = row.email || ''
  form.gender = row.gender || ''
  form.age = row.age ?? null
  form.source = row.source || ''
  form.level = row.level || ''
  form.remark = row.remark || ''
  dialogVisible.value = true
}

function viewDetail(row) {
  router.push(`/customers/${row.id}`)
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
    if (isEdit.value && editId.value) {
      await updateCustomer(editId.value, { ...form })
      ElMessage.success('更新成功')
    } else {
      await createCustomer({ ...form })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    // Error handled by interceptor
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(id) {
  try {
    await deleteCustomer(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    // Error handled by interceptor
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.customer-list :deep(.el-form--inline .el-form-item) {
  margin-right: 0;
  width: 100%;
}
</style>
