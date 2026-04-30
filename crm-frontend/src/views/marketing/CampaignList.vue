<template>
  <div class="campaign-list">
    <div class="crm-page-header">
      <h1 class="crm-page-title">营销管理</h1>
      <el-button type="primary" :icon="Plus" @click="openCreateDialog">创建活动</el-button>
    </div>
    <div class="card crm-search-card">
      <el-form :model="searchForm" inline @keyup.enter="handleSearch">
        <el-form-item label="名称"><el-input v-model="searchForm.name" placeholder="活动名称" clearable style="width:180px" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width:120px">
            <el-option label="进行中" value="进行中" /><el-option label="已结束" value="已结束" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button><el-button :icon="Refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <div class="card">
      <el-table :data="list" stripe v-loading="loading" style="width:100%">
        <el-table-column prop="name" label="活动名称" min-width="140" />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{row}"><el-tag size="small" effect="light">{{ row.type||'促销' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="目标客群" width="100" align="center"><template #default="{row}">{{ row.targetGroup||'全部' }}</template></el-table-column>
        <el-table-column label="时间" min-width="200" align="center">
          <template #default="{row}">{{ fmt(row.startTime) }} ~ {{ fmt(row.endTime) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{row}"><el-tag :type="row.status==='进行中'?'success':'info'" size="small" effect="light">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template #default="{row}">
            <el-button link size="small" :icon="Edit" @click="openEditDialog(row)">编辑</el-button>
            <el-button link size="small" :icon="Present" @click="openCouponDialog(row)">优惠券</el-button>
            <el-button type="success" link size="small" :icon="Promotion" @click="handleDistribute(row)">发放</el-button>
            <el-popconfirm title="确认删除？" @confirm="handleDelete(row.id)">
              <template #reference><el-button type="danger" link size="small" :icon="Delete">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footer"><el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" @size-change="fetchData" @current-change="fetchData" /></div>
    </div>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑活动':'创建活动'" width="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" placeholder="请输入" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="form.type" style="width:100%"><el-option label="促销" value="促销" /><el-option label="满减" value="满减" /><el-option label="新品" value="新品" /></el-select></el-form-item>
        <el-form-item label="目标客群"><el-select v-model="form.targetGroup" style="width:100%"><el-option label="全部" value="全部" /><el-option label="高价值" value="高价值" /><el-option label="普通" value="普通" /><el-option label="沉睡" value="沉睡" /></el-select></el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="form.startTime" type="datetime" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="form.endTime" type="datetime" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button></template>
    </el-dialog>

    <!-- Coupon Dialog -->
    <el-dialog v-model="couponVisible" title="优惠券管理" width="560px">
      <template v-if="currentCampaign">
        <p style="font-size:13px;color:var(--crm-text-secondary);margin-bottom:12px">活动：{{ currentCampaign.name }}</p>
        <el-form inline>
          <el-form-item label="券名"><el-input v-model="couponForm.name" placeholder="如：满100减20" style="width:200px" /></el-form-item>
          <el-form-item label="面额"><el-input-number v-model="couponForm.discountValue" :min="0" :precision="2" style="width:120px" /></el-form-item>
          <el-form-item label="门槛"><el-input-number v-model="couponForm.minAmount" :min="0" :precision="2" style="width:120px" /></el-form-item>
          <el-form-item label="数量"><el-input-number v-model="couponForm.totalQty" :min="1" style="width:100px" /></el-form-item>
          <el-form-item><el-button type="primary" :icon="Plus" @click="handleCreateCoupon">添加</el-button></el-form-item>
        </el-form>
        <el-table :data="coupons" stripe size="small" style="margin-top:12px">
          <el-table-column prop="name" label="券名" />
          <el-table-column label="面额" width="80"><template #default="{r}">¥{{ r.discountValue }}</template></el-table-column>
          <el-table-column label="门槛" width="80"><template #default="{r}">¥{{ r.minAmount }}</template></el-table-column>
          <el-table-column label="总量" width="70" align="center"><template #default="{r}">{{ r.totalQty }}</template></el-table-column>
          <el-table-column label="已发" width="60" align="center"><template #default="{r}">{{ r.sentQty }}</template></el-table-column>
          <el-table-column label="已核销" width="70" align="center">
            <template #default="{r}"><span style="color:var(--crm-primary);font-weight:600">{{ r.usedQty }}</span></template>
          </el-table-column>
        </el-table>
        <div style="margin-top:12px;font-size:13px" v-if="couponStats">
          <span>已发：<strong>{{ couponStats.totalSent }}</strong></span>
          <span style="margin-left:16px">已核销：<strong>{{ couponStats.totalUsed }}</strong></span>
          <span style="margin-left:16px">核销率：<strong style="color:var(--crm-primary)">{{ couponStats.usageRate }}</strong></span>
        </div>
      </template>
      <template #footer><el-button @click="couponVisible=false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, Present, Promotion } from '@element-plus/icons-vue'
import { getCampaigns, createCampaign, updateCampaign, deleteCampaign, distributeCoupons, getCoupons, createCoupon, getCouponStats } from '../../api/marketing'

const searchForm = reactive({ name:'', status:'' })
const list = ref([]); const loading = ref(false)
const page = ref(1); const pageSize = ref(10); const total = ref(0)

const dialogVisible = ref(false); const isEdit = ref(false); const editId = ref(null)
const formRef = ref(null); const submitLoading = ref(false)
const form = reactive({ name:'', type:'促销', targetGroup:'全部', startTime:'', endTime:'', description:'' })
const rules = { name:[{required:true,message:'请输入活动名称',trigger:'blur'}] }

const couponVisible = ref(false); const currentCampaign = ref(null)
const coupons = ref([]); const couponStats = ref(null)
const couponForm = reactive({ name:'', discountValue:0, minAmount:0, totalQty:100 })

function fmt(d) { if(!d) return '-'; const t=new Date(d); return `${t.getFullYear()}-${String(t.getMonth()+1).padStart(2,'0')}-${String(t.getDate()).padStart(2,'0')}` }

async function fetchData() { loading.value=true; try{const p={page:page.value,size:pageSize.value}; if(searchForm.name)p.name=searchForm.name; if(searchForm.status)p.status=searchForm.status; const r=await getCampaigns(p); list.value=r.data.records||[]; total.value=r.data.total||0 }catch{list.value=[];total.value=0}finally{loading.value=false} }
function handleSearch() { page.value=1; fetchData() }
function handleReset() { searchForm.name=''; searchForm.status=''; page.value=1; fetchData() }

function openCreateDialog() { isEdit.value=false; editId.value=null; form.name=''; form.type='促销'; form.targetGroup='全部'; form.startTime=''; form.endTime=''; form.description=''; dialogVisible.value=true }
function openEditDialog(row) { isEdit.value=true; editId.value=row.id; Object.assign(form, row); dialogVisible.value=true }
async function handleSubmit() { if(!formRef.value)return; try{await formRef.value.validate()}catch{return}; submitLoading.value=true; try{isEdit.value?await updateCampaign(editId.value,{...form}):await createCampaign({...form}); ElMessage.success(isEdit.value?'更新成功':'创建成功'); dialogVisible.value=false; fetchData()}catch{}finally{submitLoading.value=false} }
async function handleDelete(id) { try{await deleteCampaign(id); ElMessage.success('删除成功'); fetchData()}catch{} }
async function handleDistribute(row) { try{const r=await distributeCoupons(row.id); ElMessage.success(r.data.message||'发放完成')}catch{} }

async function openCouponDialog(row) { currentCampaign.value=row; couponVisible.value=true; couponForm.name=''; couponForm.discountValue=0; couponForm.minAmount=0; couponForm.totalQty=100;
  try{const r1=await getCoupons(row.id); coupons.value=r1.data||[]; const r2=await getCouponStats(row.id); couponStats.value=r2.data}catch{coupons.value=[];couponStats.value=null} }
async function handleCreateCoupon() { if(!couponForm.name){ElMessage.warning('请输入券名');return}; try{await createCoupon(currentCampaign.value.id,{...couponForm}); ElMessage.success('添加成功');
  couponForm.name=''; couponForm.discountValue=0; couponForm.minAmount=0; couponForm.totalQty=100;
  const r1=await getCoupons(currentCampaign.value.id); coupons.value=r1.data||[]; const r2=await getCouponStats(currentCampaign.value.id); couponStats.value=r2.data }catch{} }

onMounted(()=>fetchData())
</script>

<style scoped>
.card { background:var(--crm-surface); border-radius:var(--crm-radius); border:1px solid var(--crm-border); padding:16px 20px; }
.crm-search-card { margin-bottom:16px; padding:12px 20px 0; }
.table-footer { display:flex; justify-content:flex-end; margin-top:16px; }
</style>
