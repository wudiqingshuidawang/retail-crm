<template>
  <div class="member-list">
    <div class="crm-page-header">
      <h1 class="crm-page-title">会员管理</h1>
      <el-button type="primary" :icon="Plus" @click="openRegisterDialog">注册会员</el-button>
    </div>
    <div class="card crm-search-card">
      <el-form :model="searchForm" inline @keyup.enter="handleSearch">
        <el-form-item label="客户"><el-input v-model="searchForm.customerName" placeholder="客户姓名" clearable style="width:160px" /></el-form-item>
        <el-form-item label="等级">
          <el-select v-model="searchForm.cardLevel" placeholder="全部" clearable style="width:120px">
            <el-option label="普通" value="普通" /><el-option label="银卡" value="银卡" /><el-option label="金卡" value="金卡" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button><el-button :icon="Refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <div class="card">
      <el-table :data="list" stripe v-loading="loading" style="width:100%">
        <el-table-column prop="customerName" label="客户" min-width="100" />
        <el-table-column prop="customerPhone" label="电话" min-width="120" />
        <el-table-column label="等级" width="90" align="center">
          <template #default="{row}"><el-tag :type="levelType(row.cardLevel)" size="small" effect="light">{{ row.cardLevel }}</el-tag></template>
        </el-table-column>
        <el-table-column label="积分" width="100" align="center">
          <template #default="{row}"><span class="crm-mono">{{ row.points }}</span></template>
        </el-table-column>
        <el-table-column label="注册时间" width="160" align="center"><template #default="{row}">{{ fmt(row.registeredAt) }}</template></el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{row}">
            <el-button link size="small" :icon="View" @click="openProfile(row)">详情</el-button>
            <el-button link size="small" :icon="Edit" @click="openPointsDialog(row)">积分</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footer"><el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" @size-change="fetchData" @current-change="fetchData" /></div>
    </div>

    <!-- Register Dialog -->
    <el-dialog v-model="registerVisible" title="注册会员" width="480px">
      <el-form label-width="80px">
        <el-form-item label="选择客户">
          <el-select v-model="registerCustomerId" placeholder="搜索客户" filterable remote :remote-method="searchCustomers" :loading="customerLoading" style="width:100%" clearable>
            <el-option v-for="c in customerOptions" :key="c.id" :label="`${c.name}${c.phone?' ('+c.phone+')':''}`" :value="c.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="registerVisible=false">取消</el-button><el-button type="primary" :loading="registerLoading" @click="handleRegister">确认注册</el-button></template>
    </el-dialog>

    <!-- Points Dialog -->
    <el-dialog v-model="pointsVisible" title="积分管理" width="480px">
      <div v-if="currentMember" style="margin-bottom:16px">
        <span>会员：</span><strong>{{ currentMember.customerName }}</strong>
        <span style="margin-left:16px">当前积分：</span><strong class="crm-mono">{{ currentMember.points }}</strong>
      </div>
      <el-form :model="pointsForm" label-width="80px">
        <el-form-item label="类型"><el-radio-group v-model="pointsForm.type"><el-radio value="获取">获取</el-radio><el-radio value="消耗">消耗</el-radio></el-radio-group></el-form-item>
        <el-form-item label="积分"><el-input-number v-model="pointsForm.points" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="原因"><el-input v-model="pointsForm.reason" placeholder="如：消费奖励" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="pointsVisible=false">取消</el-button><el-button type="primary" :loading="pointsLoading" @click="handlePoints">确认</el-button></template>
    </el-dialog>

    <!-- Profile Dialog -->
    <el-dialog v-model="profileVisible" title="会员详情" width="560px">
      <template v-if="profile">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="客户">{{ profile.customerName }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ profile.customerPhone }}</el-descriptions-item>
          <el-descriptions-item label="等级"><el-tag :type="levelType(profile.member?.cardLevel)" size="small" effect="light">{{ profile.member?.cardLevel }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="积分"><span class="crm-mono">{{ profile.member?.points }}</span></el-descriptions-item>
          <el-descriptions-item label="注册时间" :span="2">{{ fmt(profile.member?.registeredAt) }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top:12px"><span style="font-size:13px;color:var(--crm-text-secondary)">画像标签：</span>
          <el-tag v-for="t in profile.tags" :key="t" size="small" style="margin:2px">{{ t }}</el-tag>
          <span v-if="!profile.tags?.length" style="color:var(--crm-text-muted);font-size:13px">暂无标签</span>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, View, Edit } from '@element-plus/icons-vue'
import { getMembers, registerMember, updatePoints, getMemberProfile } from '../../api/member'
import { getCustomers } from '../../api/customer'

const searchForm = reactive({ customerName:'', cardLevel:'' })
const list = ref([]); const loading = ref(false)
const page = ref(1); const pageSize = ref(10); const total = ref(0)

const registerVisible = ref(false); const registerCustomerId = ref(null); const registerLoading = ref(false)
const customerLoading = ref(false); const customerOptions = ref([])

const pointsVisible = ref(false); const currentMember = ref(null); const pointsLoading = ref(false)
const pointsForm = reactive({ type:'获取', points:0, reason:'' })

const profileVisible = ref(false); const profile = ref(null)

function levelType(l) { return {'金卡':'danger','银卡':'warning','普通':''}[l]||'' }
function fmt(d) { if(!d) return '-'; const t=new Date(d); return `${t.getFullYear()}-${String(t.getMonth()+1).padStart(2,'0')}-${String(t.getDate()).padStart(2,'0')} ${String(t.getHours()).padStart(2,'0')}:${String(t.getMinutes()).padStart(2,'0')}` }

async function fetchData() { loading.value=true; try{const p={page:page.value,size:pageSize.value}; if(searchForm.customerName)p.customerName=searchForm.customerName; if(searchForm.cardLevel)p.cardLevel=searchForm.cardLevel; const r=await getMembers(p); list.value=r.data.records||[]; total.value=r.data.total||0 }catch{list.value=[];total.value=0}finally{loading.value=false} }
function handleSearch() { page.value=1; fetchData() }
function handleReset() { searchForm.customerName=''; searchForm.cardLevel=''; page.value=1; fetchData() }
async function searchCustomers(q) { if(!q){customerOptions.value=[];return}; customerLoading.value=true; try{const r=await getCustomers({page:1,size:20,name:q}); customerOptions.value=r.data.records||[]}catch{customerOptions.value=[]}finally{customerLoading.value=false} }
function openRegisterDialog() { registerCustomerId.value=null; customerOptions.value=[]; registerVisible.value=true }
async function handleRegister() { if(!registerCustomerId.value){ElMessage.warning('请选择客户');return}; registerLoading.value=true; try{await registerMember({customerId:registerCustomerId.value}); ElMessage.success('注册成功'); registerVisible.value=false; fetchData()}catch{}finally{registerLoading.value=false} }
function openPointsDialog(row) { currentMember.value=row; pointsForm.type='获取'; pointsForm.points=0; pointsForm.reason=''; pointsVisible.value=true }
async function handlePoints() { if(!currentMember.value||pointsForm.points<=0){ElMessage.warning('请输入积分');return}; pointsLoading.value=true; try{await updatePoints(currentMember.value.id,{...pointsForm}); ElMessage.success('操作成功'); pointsVisible.value=false; fetchData()}catch{}finally{pointsLoading.value=false} }
async function openProfile(row) { profileVisible.value=true; profile.value=null; try{const r=await getMemberProfile(row.id); profile.value=r.data}catch{profile.value=null} }
onMounted(()=>fetchData())
</script>

<style scoped>
.card { background:var(--crm-surface); border-radius:var(--crm-radius); border:1px solid var(--crm-border); padding:16px 20px; }
.crm-search-card { margin-bottom:16px; padding:12px 20px 0; }
.table-footer { display:flex; justify-content:flex-end; margin-top:16px; }
</style>
