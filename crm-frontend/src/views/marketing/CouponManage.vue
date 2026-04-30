<template>
  <div class="coupon-manage">
    <div class="crm-page-header"><h1 class="crm-page-title">优惠券管理</h1></div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="全部发放记录" name="all">
        <div class="card">
          <el-form inline style="margin-bottom:12px">
            <el-form-item label="状态">
              <el-select v-model="filterStatus" clearable placeholder="全部" style="width:120px" @change="fetchAll">
                <el-option label="未使用" value="未使用" /><el-option label="已使用" value="已使用" /><el-option label="已过期" value="已过期" />
              </el-select>
            </el-form-item>
            <el-form-item><el-button :icon="Refresh" @click="fetchAll">刷新</el-button></el-form-item>
          </el-form>
          <el-table :data="allRecords" stripe v-loading="aLoading" style="width:100%">
            <el-table-column prop="couponName" label="券名" min-width="110" />
            <el-table-column prop="customerName" label="客户" min-width="100" />
            <el-table-column label="状态" width="90" align="center">
              <template #default="{row}"><el-tag :type="sType(row.status)" size="small" effect="light">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column label="发放时间" width="160" align="center"><template #default="{row}">{{ fmt(row.sendTime) }}</template></el-table-column>
            <el-table-column label="使用时间" width="160" align="center"><template #default="{row}">{{ fmt(row.useTime) }}</template></el-table-column>
            <el-table-column label="操作" width="90" align="center" fixed="right">
              <template #default="{row}">
                <el-button v-if="row.status==='未使用'" type="success" size="small" :icon="Select" :loading="usingId===row.recordId" @click="handleUse(row)">核销</el-button>
                <span v-else class="text-muted">-</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="按客户查询" name="customer">
        <div class="card">
          <el-form inline style="margin-bottom:12px">
            <el-form-item label="选择客户">
              <el-select v-model="selectedCustomer" placeholder="搜索客户" filterable remote :remote-method="searchCustomers" :loading="custLoading" clearable @change="fetchCustCoupons" style="width:260px">
                <el-option v-for="c in custOptions" :key="c.id" :label="`${c.name}${c.phone?' ('+c.phone+')':''}`" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-form>
          <el-table :data="custRecords" stripe v-loading="cLoading" empty-text="请选择客户" style="width:100%">
            <el-table-column prop="couponName" label="券名" min-width="100" />
            <el-table-column label="面额" width="80" align="center"><template #default="{row}">{{ row.discountValue ? '¥'+row.discountValue : '-' }}</template></el-table-column>
            <el-table-column label="门槛" width="80" align="center"><template #default="{row}">{{ row.minAmount ? '¥'+row.minAmount : '-' }}</template></el-table-column>
            <el-table-column label="状态" width="90" align="center">
              <template #default="{row}"><el-tag :type="sType(row.status)" size="small" effect="light">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column label="发放时间" width="160" align="center"><template #default="{row}">{{ fmt(row.sendTime) }}</template></el-table-column>
            <el-table-column label="操作" width="90" align="center" fixed="right">
              <template #default="{row}">
                <el-button v-if="row.status==='未使用'" type="success" size="small" :icon="Select" :loading="usingId===row.recordId" @click="handleUse(row)">核销</el-button>
                <span v-else class="text-muted">-</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Select } from '@element-plus/icons-vue'
import { useCoupon, getCustomerCoupons } from '../../api/marketing'
import { getCustomers } from '../../api/customer'
import request from '../../utils/request'

const activeTab = ref('all'); const filterStatus = ref(''); const usingId = ref(null)

// All records
const allRecords = ref([]); const aLoading = ref(false)
function sType(s) { return {'未使用':'warning','已使用':'success','已过期':'info'}[s]||'' }
function fmt(d) { if(!d) return '-'; const t=new Date(d); return `${t.getFullYear()}-${String(t.getMonth()+1).padStart(2,'0')}-${String(t.getDate()).padStart(2,'0')} ${String(t.getHours()).padStart(2,'0')}:${String(t.getMinutes()).padStart(2,'0')}` }

async function fetchAll() { aLoading.value=true; try{const p={}; if(filterStatus.value) p.status=filterStatus.value; const r=await request.get('/coupon-records',{params:p}); allRecords.value=r.data||[] }catch{allRecords.value=[]}finally{aLoading.value=false} }

// Customer coupons
const selectedCustomer = ref(null); const custOptions = ref([]); const custLoading = ref(false)
const custRecords = ref([]); const cLoading = ref(false)

async function searchCustomers(q) { if(!q){custOptions.value=[];return}; custLoading.value=true; try{const r=await getCustomers({page:1,size:20,name:q}); custOptions.value=r.data.records||[]}catch{custOptions.value=[]}finally{custLoading.value=false} }
async function fetchCustCoupons() { if(!selectedCustomer.value){custRecords.value=[];return}; cLoading.value=true; try{const r=await getCustomerCoupons(selectedCustomer.value); custRecords.value=r.data||[]}catch{custRecords.value=[]}finally{cLoading.value=false} }

async function handleUse(row) { usingId.value=row.recordId; try{await useCoupon(row.recordId); ElMessage.success('核销成功'); activeTab.value==='all'?fetchAll():fetchCustCoupons()}catch{}finally{usingId.value=null} }

onMounted(()=>fetchAll())
</script>

<style scoped>
.card { background:var(--crm-surface); border-radius:var(--crm-radius); border:1px solid var(--crm-border); padding:16px 20px; }
.text-muted { color:var(--crm-text-muted); font-size:12px; }
</style>
