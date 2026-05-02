<template>
  <div class="todo-list">
    <div class="crm-page-header"><h1 class="crm-page-title">待办提醒</h1></div>
    <div class="card crm-search-card">
      <el-form inline>
        <el-form-item label="状态">
          <el-select v-model="filterStatus" placeholder="全部" clearable style="width:130px" @change="fetchData">
            <el-option label="待办" value="待办" /><el-option label="已完成" value="已完成" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button :icon="Refresh" @click="fetchData">刷新</el-button></el-form-item>
      </el-form>
    </div>
    <div class="card">
      <el-table :data="list" stripe v-loading="loading" style="width:100%">
        <el-table-column label="类型" width="80" align="center">
          <template #default="{row}"><el-tag :type="row.type==='投诉'?'danger':row.type==='回访'?'primary':'warning'" size="small" effect="light">{{ row.type }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="240" show-overflow-tooltip />
        <el-table-column label="截止时间" width="160" align="center">
          <template #default="{row}">{{ fmt(row.deadline) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{row}"><el-tag :type="row.status==='已完成'?'success':'warning'" size="small" effect="light">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center" fixed="right">
          <template #default="{row}">
            <el-button v-if="row.status!=='已完成'" type="success" link size="small" :icon="Select" :loading="completingId===row.id" @click="handleComplete(row)">完成</el-button>
            <span v-else style="color:var(--crm-text-muted);font-size:12px">已办</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footer"><el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" @size-change="fetchData" @current-change="fetchData" /></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Select } from '@element-plus/icons-vue'
import { getTodos, completeTodo } from '../../api/employee'

const filterStatus = ref('')
const list = ref([]); const loading = ref(false)
const page = ref(1); const pageSize = ref(10); const total = ref(0)
const completingId = ref(null)

function fmt(d) { if(!d) return '-'; const t=new Date(d); return `${t.getFullYear()}-${String(t.getMonth()+1).padStart(2,'0')}-${String(t.getDate()).padStart(2,'0')} ${String(t.getHours()).padStart(2,'0')}:${String(t.getMinutes()).padStart(2,'0')}` }

async function fetchData() { loading.value=true; try{const p={page:page.value,size:pageSize.value}; if(filterStatus.value)p.status=filterStatus.value; const r=await getTodos(p); list.value=r.data.records||[]; total.value=r.data.total||0 }catch{list.value=[];total.value=0}finally{loading.value=false} }
async function handleComplete(row) { completingId.value=row.id; try{await completeTodo(row.id); ElMessage.success('已完成'); fetchData()}catch{}finally{completingId.value=null} }
onMounted(()=>fetchData())
</script>

<style scoped>
.table-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--crm-border-subtle);
}
</style>
