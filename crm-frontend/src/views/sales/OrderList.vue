<template>
  <div class="order-list">
    <div class="crm-page-header">
      <h1 class="crm-page-title">销售管理</h1>
      <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增订单</el-button>
    </div>
    <div class="card crm-search-card">
      <el-form :model="searchForm" inline @keyup.enter="handleSearch">
        <el-form-item label="客户"><el-input v-model="searchForm.customerName" placeholder="客户名称" clearable style="width:180px" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width:130px">
            <el-option label="待付款" value="待付款" /><el-option label="已付款" value="已付款" /><el-option label="已退款" value="已退款" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button><el-button :icon="Refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <div class="card">
      <el-table :data="orderList" stripe v-loading="loading" style="width:100%">
        <el-table-column prop="id" label="订单号" width="90" align="center"><template #default="{row}"><span class="crm-mono">#{{ row.id }}</span></template></el-table-column>
        <el-table-column prop="customerName" label="客户" min-width="120" />
        <el-table-column label="金额" width="120" align="center">
          <template #default="{row}"><span class="crm-mono amount">¥{{ Number(row.totalAmount).toLocaleString('zh-CN',{minimumFractionDigits:2}) }}</span></template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{row}"><el-tag :type="statusType(row.status)" size="small" effect="light">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="时间" width="160" align="center"><template #default="{row}">{{ fmt(row.createdAt) }}</template></el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{row}"><el-button type="primary" link size="small" :icon="View" @click="viewDetail(row)">详情</el-button></template>
        </el-table-column>
      </el-table>
      <div class="table-footer"><el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" @size-change="fetchData" @current-change="fetchData" /></div>
    </div>

    <!-- Create Dialog -->
    <el-dialog v-model="dialogVisible" title="新增订单" width="620px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="form.customerId" placeholder="搜索客户" filterable remote :remote-method="searchCustomers" :loading="customerLoading" style="width:100%" clearable>
            <el-option v-for="c in customerOptions" :key="c.id" :label="`${c.name}${c.phone?' ('+c.phone+')':''}`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额" prop="totalAmount"><el-input-number v-model="form.totalAmount" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="状态"><el-select v-model="form.status" style="width:100%"><el-option label="待付款" value="待付款" /><el-option label="已付款" value="已付款" /></el-select></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="商品明细">
          <div style="width:100%">
            <div v-for="(item,i) in form.items" :key="i" class="item-row">
              <el-input v-model="item.productName" placeholder="商品名" style="width:180px" />
              <el-input-number v-model="item.qty" :min="1" style="width:90px" placeholder="数量" />
              <el-input-number v-model="item.price" :min="0" :precision="2" style="width:130px" placeholder="单价" />
              <el-button type="danger" :icon="Delete" circle size="small" @click="form.items.splice(i,1)" :disabled="form.items.length<=1" />
            </div>
            <el-button type="primary" link :icon="Plus" @click="form.items.push({productName:'',qty:1,price:0})">添加商品</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button></template>
    </el-dialog>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="订单详情" width="620px">
      <template v-if="detailData">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="订单号"><span class="crm-mono">#{{ detailData.id }}</span></el-descriptions-item>
          <el-descriptions-item label="客户">{{ detailData.customerName }}</el-descriptions-item>
          <el-descriptions-item label="金额"><span class="crm-mono" style="color:#dc2626;font-weight:600">¥{{ Number(detailData.totalAmount).toLocaleString('zh-CN',{minimumFractionDigits:2}) }}</span></el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag :type="statusType(detailData.status)" size="small" effect="light">{{ detailData.status }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="时间" :span="2">{{ fmt(detailData.createdAt) }}</el-descriptions-item>
        </el-descriptions>
        <el-divider />
        <h4 style="font-size:14px;font-weight:600;margin:0 0 12px">商品明细</h4>
        <el-table :data="detailData.items||[]" border stripe size="small">
          <el-table-column prop="productName" label="商品" />
          <el-table-column prop="qty" label="数量" width="70" align="center" />
          <el-table-column label="单价" width="110" align="center"><template #default="{row}"><span class="crm-mono">¥{{ row.price }}</span></template></el-table-column>
          <el-table-column label="小计" width="110" align="center"><template #default="{row}"><span class="crm-mono">¥{{ (row.price*row.qty).toFixed(2) }}</span></template></el-table-column>
        </el-table>
      </template>
      <div v-else v-loading="detailLoading" style="height:200px" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, View, Delete } from '@element-plus/icons-vue'
import { getOrders, getOrder, createOrder } from '../../api/sales'
import { getCustomers } from '../../api/customer'

const searchForm = reactive({ customerName:'', status:'' })
const orderList = ref([]); const loading = ref(false)
const page = ref(1); const pageSize = ref(10); const total = ref(0)

const dialogVisible = ref(false); const formRef = ref(null); const submitLoading = ref(false)
const customerLoading = ref(false); const customerOptions = ref([])
const form = reactive({ customerId:null, totalAmount:0, status:'待付款', remark:'', items:[{productName:'',qty:1,price:0}] })
const formRules = { customerId:[{required:true,message:'请选择客户',trigger:'change'}], totalAmount:[{required:true,message:'请输入金额',trigger:'blur'}] }

const detailVisible = ref(false); const detailData = ref(null); const detailLoading = ref(false)

function statusType(s) { return {'待付款':'warning','已付款':'success','已退款':'danger'}[s]||'' }
function fmt(d) { if(!d) return '-'; const t=new Date(d); return `${t.getFullYear()}-${String(t.getMonth()+1).padStart(2,'0')}-${String(t.getDate()).padStart(2,'0')} ${String(t.getHours()).padStart(2,'0')}:${String(t.getMinutes()).padStart(2,'0')}` }

async function fetchData() {
  loading.value=true
  try {
    const p = { page:page.value, size:pageSize.value }
    if(searchForm.customerName) p.customerName = searchForm.customerName
    if(searchForm.status) p.status = searchForm.status
    const res = await getOrders(p)
    orderList.value = res.data.records || []; total.value = res.data.total || 0
  } catch { orderList.value=[]; total.value=0 } finally { loading.value=false }
}
function handleSearch() { page.value=1; fetchData() }
function handleReset() { searchForm.customerName=''; searchForm.status=''; page.value=1; fetchData() }
async function searchCustomers(q) { if(!q){customerOptions.value=[];return}; customerLoading.value=true; try { const r=await getCustomers({page:1,size:20,name:q}); customerOptions.value=r.data.records||[] } catch { customerOptions.value=[] } finally { customerLoading.value=false } }
function openCreateDialog() { form.customerId=null; form.totalAmount=0; form.status='待付款'; form.remark=''; form.items=[{productName:'',qty:1,price:0}]; customerOptions.value=[]; dialogVisible.value=true }
async function handleSubmit() { if(!formRef.value) return; try{await formRef.value.validate()}catch{return}; submitLoading.value=true; try{await createOrder({...form}); ElMessage.success('创建成功'); dialogVisible.value=false; fetchData()}catch{}finally{submitLoading.value=false} }
async function viewDetail(row) { detailVisible.value=true; detailLoading.value=true; detailData.value=null; try{const r=await getOrder(row.id); detailData.value=r.data}catch{detailData.value=null}finally{detailLoading.value=false} }
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
.amount { font-weight: 600; color: var(--crm-text); }
.item-row { display: flex; gap: 8px; margin-bottom: 8px; align-items: center; }
</style>
