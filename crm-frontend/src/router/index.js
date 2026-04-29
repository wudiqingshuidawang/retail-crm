import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { noAuth: true }
  },
  {
    path: '/',
    component: () => import('../components/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'customers',
        name: 'CustomerList',
        component: () => import('../views/customer/CustomerList.vue'),
        meta: { title: '客户管理' }
      },
      {
        path: 'customers/:id',
        name: 'CustomerDetail',
        component: () => import('../views/customer/CustomerDetail.vue'),
        meta: { title: '客户详情' }
      },
      {
        path: 'pool',
        name: 'LeadPool',
        component: () => import('../views/customer/LeadPool.vue'),
        meta: { title: '公海池' }
      },
      {
        path: 'orders',
        name: 'OrderList',
        component: () => import('../views/sales/OrderList.vue'),
        meta: { title: '销售管理' }
      },
      {
        path: 'follow-ups',
        name: 'FollowUpList',
        component: () => import('../views/sales/FollowUpList.vue'),
        meta: { title: '回访记录' }
      }
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

export default router
