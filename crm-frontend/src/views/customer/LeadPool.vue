<template>
  <div class="lead-pool">
    <div class="crm-page-header">
      <h1 class="crm-page-title">公海池</h1>
      <span class="header-hint">无人跟进的客户线索，可自由领取</span>
    </div>
    <div class="card crm-search-card">
      <el-form :model="searchForm" inline @keyup.enter="handleSearch">
        <el-form-item label="姓名"><el-input v-model="searchForm.name" placeholder="输入姓名" clearable style="width:160px" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="searchForm.phone" placeholder="输入电话" clearable style="width:160px" /></el-form-item>
        <el-form-item label="等级">
          <el-select v-model="searchForm.level" placeholder="全部" clearable style="width:130px">
            <el-option label="高价值" value="高价值" /><el-option label="普通" value="普通" /><el-option label="沉睡" value="沉睡" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button><el-button :icon="Refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <div class="card">
      <el-table :data="poolList" stripe v-loading="loading" empty-text="公海池暂无客户" style="width:100%">
        <el-table-column prop="name" label="姓名" min-width="110" />
        <el-table-column prop="phone" label="电话" min-width="130" />
        <el-table-column prop="level" label="等级" width="90" align="center">
          <template #default="{row}"><el-tag v-if="row.level" :type="levelType(row.level)" size="small" effect="light">{{ row.level }}</el-tag><span v-else>-</span></template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="100" align="center"><template #default="{row}">{{ row.source || '-' }}</template></el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{row}">
            <el-button type="primary" size="small" :icon="Plus" :loading="claimingId===row.id" @click="handleClaim(row)">领取</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footer"><el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" @size-change="fetchData" @current-change="fetchData" /></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getPoolCustomers, claimCustomer } from '../../api/customer'

const searchForm = reactive({ name:'', phone:'', level:'' })
const poolList = ref([])
const loading = ref(false)
const page = ref(1); const pageSize = ref(10); const total = ref(0)
const claimingId = ref(null)
function levelType(l) { return {'高价值':'danger','普通':'','沉睡':'info'}[l]||'' }

async function fetchData() {
  loading.value=true
  try {
    const p = { page:page.value, size:pageSize.value }
    if(searchForm.name) p.name = searchForm.name
    if(searchForm.phone) p.phone = searchForm.phone
    if(searchForm.level) p.level = searchForm.level
    const res = await getPoolCustomers(p)
    poolList.value = res.data.records || []; total.value = res.data.total || 0
  } catch { poolList.value=[]; total.value=0 }
  finally { loading.value=false }
}
function handleSearch() { page.value=1; fetchData() }
function handleReset() { searchForm.name=''; searchForm.phone=''; searchForm.level=''; page.value=1; fetchData() }
async function handleClaim(row) {
  claimingId.value=row.id
  try { await claimCustomer(row.id); ElMessage.success('领取成功'); fetchData() } catch {}
  finally { claimingId.value=null }
}
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
.header-hint { font-size: 13px; color: var(--crm-text-muted); }
</style>
