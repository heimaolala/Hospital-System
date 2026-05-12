import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { noAuth: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { noAuth: true }
  },
  {
    path: '/',
    component: () => import('@/views/MainLayout.vue'),
    redirect: '/patient/dashboard',
    children: [
      {
        path: 'patient/dashboard',
        name: 'PatientDashboard',
        component: () => import('@/views/patient/PatientDashboard.vue'),
        meta: { role: 0 }
      },
      {
        path: 'patient/book',
        name: 'BookRegistration',
        component: () => import('@/views/patient/BookRegistration.vue'),
        meta: { role: 0 }
      },
      {
        path: 'patient/my-registrations',
        name: 'MyRegistrations',
        component: () => import('@/views/patient/MyRegistrations.vue'),
        meta: { role: 0 }
      },
      {
        path: 'patient/ai-chat',
        name: 'AiChat',
        component: () => import('@/views/patient/AiChat.vue'),
        meta: { role: 0 }
      },
      {
        path: 'doctor/dashboard',
        name: 'DoctorDashboard',
        component: () => import('@/views/doctor/DoctorDashboard.vue'),
        meta: { role: 1 }
      },
      {
        path: 'doctor/my-schedules',
        name: 'MySchedules',
        component: () => import('@/views/doctor/MySchedules.vue'),
        meta: { role: 1 }
      },
      {
        path: 'admin/dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/AdminDashboard.vue'),
        meta: { role: 2 }
      },
      {
        path: 'admin/users',
        name: 'UserManage',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { role: 2 }
      },
      {
        path: 'admin/schedules',
        name: 'ScheduleAudit',
        component: () => import('@/views/admin/ScheduleAudit.vue'),
        meta: { role: 2 }
      },
      {
        path: 'admin/audit-logs',
        name: 'AuditLog',
        component: () => import('@/views/admin/AuditLog.vue'),
        meta: { role: 2 }
      },
      {
        path: 'patient/medical-records',
        name: 'PatientMedicalRecords',
        component: () => import('@/views/patient/MedicalRecords.vue'),
        meta: { role: 0 }
      },
      {
        path: 'doctor/medical-records',
        name: 'DoctorMedicalRecords',
        component: () => import('@/views/doctor/MedicalRecords.vue'),
        meta: { role: 1 }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

const roleRoutes = { 0: '/patient/dashboard', 1: '/doctor/dashboard', 2: '/admin/dashboard' }

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('accessToken')
  const userRole = parseInt(localStorage.getItem('userRole'))
  const mustChangePwd = localStorage.getItem('mustChangePwd')

  if (to.meta.noAuth) {
    if (token && to.path === '/login') {
      next(roleRoutes[userRole] || '/patient/dashboard')
      return
    }
    next()
    return
  }

  if (!token) {
    next('/login')
    return
  }

  if (mustChangePwd === '1' && to.path !== '/patient/dashboard'
      && to.path !== '/doctor/dashboard' && to.path !== '/admin/dashboard') {
    next(roleRoutes[userRole] || '/patient/dashboard')
    return
  }

  const requiredRole = to.meta.role
  if (requiredRole !== undefined && userRole !== requiredRole) {
    next(roleRoutes[userRole] || '/login')
    return
  }

  next()
})

export default router
