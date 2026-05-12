import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCurrentUser } from '@/api/user'

function loadRole() {
  const r = localStorage.getItem('userRole')
  return r != null ? parseInt(r) : null
}

export const useUserStore = defineStore('user', () => {
  const userInfo = ref({
    name: localStorage.getItem('userName') || '',
    role: loadRole()
  })
  const isLoggedIn = ref(!!localStorage.getItem('accessToken'))
  const role = ref(loadRole())

  function setLogin(response) {
    localStorage.setItem('accessToken', response.accessToken)
    localStorage.setItem('refreshToken', response.refreshToken)
    localStorage.setItem('userRole', response.role)
    localStorage.setItem('userName', response.name)
    isLoggedIn.value = true
    role.value = response.role
    userInfo.value = {
      userId: response.userId,
      name: response.name,
      role: response.role
    }
  }

  function logout() {
    localStorage.clear()
    userInfo.value = { name: '', role: null }
    isLoggedIn.value = false
    role.value = null
  }

  async function fetchUserInfo() {
    try {
      const res = await getCurrentUser()
      Object.assign(userInfo.value, res.data)
      isLoggedIn.value = true
      role.value = res.data.role
      localStorage.setItem('userRole', res.data.role)
      localStorage.setItem('userName', res.data.name)
    } catch (e) {
      if (e.response && e.response.status === 401) {
        logout()
      }
    }
  }

  function initFromStorage() {
    if (isLoggedIn.value) {
      fetchUserInfo()
    }
  }

  return { userInfo, isLoggedIn, role, setLogin, logout, fetchUserInfo, initFromStorage }
})
