<template>
  <div class="customer-list">
    <div class="crm-page-header">
      <h1 class="crm-page-title">客户管理</h1>
      <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增客户</el-button>
    </div>

    <!-- Search -->
    <div class="card crm-search-card">
      <el-form :model="searchForm" inline @keyup.enter="handleSearch">
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="输入姓名搜索" clearable style="width:180px" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="searchForm.phone" placeholder="输入电话搜索" clearable style="width:180px" />
        </el-form-item>
        <el-form-item label="等级">
          <el-select v-model="searchForm.level" placeholder="全部" clearable style="width:130px">
            <el-option label="高价值" value="高价值" />
            <el-option label="普通" value="普通" />
            <el-option label="沉睡" value="沉睡" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Table -->
    <div class="card">
      <el-table :data="customerList" stripe v-loading="loading" empty-text="暂无客户数据" style="width:100%">
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="phone" label="电话" min-width="130" />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column prop="gender" label="性别" width="60" align="center">
          <template #default="{row}">{{ row.gender || '-' }}</template>
        </el-table-column>
        <el-table-column prop="level" label="等级" width="90" align="center">
          <template #default="{row}">
            <el-tag v-if="row.level" :type="levelType(row.level)" size="small" effect="light">{{ row.level }}</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="90" align="center">
          <template #default="{row}">{{ row.source || '-' }}</template>
        </el-table-column>
        <el-table-column label="归属" width="80" align="center">
          <template #default="{row}">{{ row.ownerId || '未分配' }}</template>
        </el-table-column>
        <el-table-column prop="created_at" label="创建时间" width="160" align="center" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{row}">
            <el-button type="primary" link size="small" :icon="View" @click="viewDetail(row)">查看</el-button>
            <el-button link size="small" :icon="Edit" @click="openEditDialog(row)">编辑</el-button>
            <el-popconfirm title="确认删除？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link size="small" :icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footer">
        <el-pagination
          v-model:current-page="page" v-model:page-size="pageSize" :total="total"
          :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next"
          @size-change="fetchData" @current-change="fetchData"
        />
      </div>
    </div>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑客户':'新增客户'" width="560px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="72px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电话">
              <el-input v-model="form.phone" placeholder="请输入" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="form.gender" style="width:100%">
                <el-option label="男" value="男" /><el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="年龄">
              <el-input-number v-model="form.age" :min="0" :max="150" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源">
              <el-input v-model="form.source" placeholder="如：门店、小程序" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="等级">
              <el-select v-model="form.level" style="width:100%">
                <el-option label="高价值" value="高价值" />
                <el-option label="普通" value="普通" />
                <el-option label="沉睡" value="沉睡" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注">
              <el-input v-model="form.remark" placeholder="可选" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
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
const searchForm = reactive({ name: '', phone: '', level: '' })
const customerList = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)
const submitLoading = ref(false)
const form = reactive({ name:'', phone:'', email:'', gender:'', age:null, source:'', level:'', remark:'' })
const rules = { name: [{ required:true, message:'请输入客户姓名', trigger:'blur' }] }

function levelType(l) { return { '高价值':'danger','普通':'','沉睡':'info' }[l] || '' }

async function fetchData() {
  loading.value = true
  try {
    const p = { page:page.value, size:pageSize.value }
    if(searchForm.name) p.name = searchForm.name
    if(searchForm.phone) p.phone = searchForm.phone
    if(searchForm.level) p.level = searchForm.level
    const res = await getCustomers(p)
    customerList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch { customerList.value = []; total.value = 0 }
  finally { loading.value = false }
}

function handleSearch() { page.value=1; fetchData() }
function handleReset() { searchForm.name=''; searchForm.phone=''; searchForm.level=''; page.value=1; fetchData() }

function resetForm() { Object.assign(form, { name:'',phone:'',email:'',gender:'',age:null,source:'',level:'',remark:'' }); editId.value=null }
function openCreateDialog() { isEdit.value=false; resetForm(); dialogVisible.value=true }
function openEditDialog(row) { isEdit.value=true; editId.value=row.id; Object.assign(form, row); dialogVisible.value=true }
function viewDetail(row) { router.push(`/customers/${row.id}`) }

async function handleSubmit() {
  if(!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  submitLoading.value = true
  try {
    isEdit.value ? await updateCustomer(editId.value, {...form}) : await createCustomer({...form})
    ElMessage.success(isEdit.value?'更新成功':'创建成功')
    dialogVisible.value=false; fetchData()
  } catch {}
  finally { submitLoading.value=false }
}

async function handleDelete(id) {
  try { await deleteCustomer(id); ElMessage.success('删除成功'); fetchData() } catch {}
}

onMounted(() => fetchData())
</script>

<style scoped>
.card { background:var(--crm-surface); border-radius:var(--crm-radius); border:1px solid var(--crm-border); padding:16px 20px; }
.crm-search-card { margin-bottom:16px; padding:12px 20px 0; }
.table-footer { display:flex; justify-content:flex-end; margin-top:16px; }
.text-muted { color:var(--crm-text-muted); }
</style>
