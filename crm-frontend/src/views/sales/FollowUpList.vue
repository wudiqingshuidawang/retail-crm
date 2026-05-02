<template>
  <div class="follow-up-list">
    <div class="crm-page-header">
      <h1 class="crm-page-title">回访记录</h1>
      <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增回访</el-button>
    </div>
    <div class="card crm-search-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="回访" name="回访" /><el-tab-pane label="投诉" name="投诉" />
      </el-tabs>
      <el-form :model="searchForm" inline style="margin-top:8px">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width:140px">
            <el-option label="待处理" value="待处理" /><el-option label="处理中" value="处理中" /><el-option label="已完成" value="已完成" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button><el-button :icon="Refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <div class="card">
      <el-table :data="list" stripe v-loading="loading" style="width:100%">
        <el-table-column prop="customerName" label="客户" min-width="110" />
        <el-table-column prop="type" label="类型" width="70" align="center">
          <template #default="{row}"><el-tag :type="row.type==='投诉'?'danger':'primary'" size="small" effect="light">{{ row.type }}</el-tag></template>
        </el-table-column>
        <el-table-column label="计划时间" width="160" align="center"><template #default="{row}">{{ fmt(row.planTime) }}</template></el-table-column>
        <el-table-column label="实际时间" width="160" align="center"><template #default="{row}">{{ fmt(row.actualTime) }}</template></el-table-column>
        <el-table-column prop="content" label="内容" min-width="180" show-overflow-tooltip><template #default="{row}">{{ row.content||'-' }}</template></el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{row}"><el-tag :type="statusType(row.status)" size="small" effect="light">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center" fixed="right">
          <template #default="{row}">
            <el-button v-if="row.status==='待处理'" type="success" link size="small" :icon="Select" @click="openCompleteDialog(row)">完成</el-button>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footer"><el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" @size-change="fetchData" @current-change="fetchData" /></div>
    </div>

    <!-- Create Dialog -->
    <el-dialog v-model="createVisible" title="新增回访" width="540px" :close-on-click-modal="false">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="80px">
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="createForm.customerId" placeholder="搜索客户" filterable remote :remote-method="searchCustomers" :loading="customerLoading" style="width:100%" clearable>
            <el-option v-for="c in customerOptions" :key="c.id" :label="`${c.name}${c.phone?' ('+c.phone+')':''}`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型"><el-radio-group v-model="createForm.type"><el-radio value="回访">回访</el-radio><el-radio value="投诉">投诉</el-radio></el-radio-group></el-form-item>
        <el-form-item label="计划时间" prop="planTime"><el-date-picker v-model="createForm.planTime" type="datetime" placeholder="选择时间" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="createForm.content" type="textarea" :rows="3" placeholder="请输入内容" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="createVisible=false">取消</el-button><el-button type="primary" :loading="submitLoading" @click="handleCreate">确认</el-button></template>
    </el-dialog>

    <!-- Complete Dialog -->
    <el-dialog v-model="completeVisible" title="完成回访" width="480px" :close-on-click-modal="false">
      <el-form ref="completeFormRef" :model="completeForm" :rules="completeRules" label-width="80px">
        <el-form-item label="实际时间" prop="actualTime"><el-date-picker v-model="completeForm.actualTime" type="datetime" placeholder="选择时间" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" /></el-form-item>
        <el-form-item label="完成内容" prop="content"><el-input v-model="completeForm.content" type="textarea" :rows="3" placeholder="请输入完成内容" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="completeVisible=false">取消</el-button><el-button type="primary" :loading="completeLoading" @click="handleComplete">确认</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, Select } from '@element-plus/icons-vue'
import { getFollowUps, createFollowUp, updateFollowUp } from '../../api/sales'
import { getCustomers } from '../../api/customer'

const searchForm = reactive({ status:'' }); const activeTab = ref('回访')
const list = ref([]); const loading = ref(false)
const page = ref(1); const pageSize = ref(10); const total = ref(0)

const createVisible = ref(false); const createFormRef = ref(null); const submitLoading = ref(false)
const customerLoading = ref(false); const customerOptions = ref([])
const createForm = reactive({ customerId:null, type:'回访', planTime:'', content:'' })
const createRules = { customerId:[{required:true,message:'请选择客户',trigger:'change'}], planTime:[{required:true,message:'请选择时间',trigger:'change'}] }

const completeVisible = ref(false); const completeFormRef = ref(null); const completeLoading = ref(false)
const currentFollowUpId = ref(null)
const completeForm = reactive({ actualTime:'', content:'' })
const completeRules = { actualTime:[{required:true,message:'请选择时间',trigger:'change'}], content:[{required:true,message:'请输入内容',trigger:'blur'}] }

function statusType(s) { return {'待处理':'warning','处理中':'primary','已完成':'success'}[s]||'' }
function fmt(d) { if(!d) return '-'; const t=new Date(d); return `${t.getFullYear()}-${String(t.getMonth()+1).padStart(2,'0')}-${String(t.getDate()).padStart(2,'0')} ${String(t.getHours()).padStart(2,'0')}:${String(t.getMinutes()).padStart(2,'0')}` }

async function fetchData() { loading.value=true; try{const p={page:page.value,size:pageSize.value,type:activeTab.value}; if(searchForm.status) p.status=searchForm.status; const r=await getFollowUps(p); list.value=r.data.records||[]; total.value=r.data.total||0 }catch{list.value=[];total.value=0}finally{loading.value=false} }
function handleTabChange() { page.value=1; fetchData() }
function handleSearch() { page.value=1; fetchData() }
function handleReset() { searchForm.status=''; page.value=1; fetchData() }
async function searchCustomers(q) { if(!q){customerOptions.value=[];return}; customerLoading.value=true; try{const r=await getCustomers({page:1,size:20,name:q}); customerOptions.value=r.data.records||[]}catch{customerOptions.value=[]}finally{customerLoading.value=false} }
function openCreateDialog() { createForm.customerId=null; createForm.type=activeTab.value; createForm.planTime=''; createForm.content=''; customerOptions.value=[]; createVisible.value=true }
async function handleCreate() { if(!createFormRef.value)return; try{await createFormRef.value.validate()}catch{return}; submitLoading.value=true; try{await createFollowUp({...createForm}); ElMessage.success('创建成功'); createVisible.value=false; fetchData()}catch{}finally{submitLoading.value=false} }
function openCompleteDialog(row) { currentFollowUpId.value=row.id; completeForm.actualTime=new Date().toISOString().slice(0,19); completeForm.content=row.content||''; completeVisible.value=true }
async function handleComplete() { if(!completeFormRef.value)return; try{await completeFormRef.value.validate()}catch{return}; completeLoading.value=true; try{await updateFollowUp(currentFollowUpId.value,{actualTime:completeForm.actualTime,content:completeForm.content,status:'已完成'}); ElMessage.success('已完成'); completeVisible.value=false; fetchData()}catch{}finally{completeLoading.value=false} }
onMounted(() => fetchData())
</script>

<style scoped>
.table-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--crm-border-subtle);
}
.text-muted { color: var(--crm-text-muted); }
</style>
