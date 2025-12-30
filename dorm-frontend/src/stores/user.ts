import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export type UserRole = 'student' | 'inspector' | 'admin' | null

export const useUserStore = defineStore('user', () => {
  // State
  const role = ref<UserRole>(localStorage.getItem('user_role') as UserRole || null)
  const name = ref<string>(localStorage.getItem('user_name') || '未登录用户')
  const token = ref<string>(localStorage.getItem('user_token') || '')
  
  // Getters
  const isAuthenticated = computed(() => !!role.value && !!token.value)
  const isStudent = computed(() => role.value === 'student')
  const isInspector = computed(() => role.value === 'inspector')
  const isAdmin = computed(() => role.value === 'admin')

  // Actions
  function login(userRole: UserRole, userName: string, userToken: string) {
    role.value = userRole
    name.value = userName
    token.value = userToken
    localStorage.setItem('user_role', userRole || '')
    localStorage.setItem('user_name', userName)
    localStorage.setItem('user_token', userToken)
  }

  function logout() {
    role.value = null
    name.value = '未登录用户'
    token.value = ''
    localStorage.removeItem('user_role')
    localStorage.removeItem('user_name')
    localStorage.removeItem('user_token')
  }

  return {
    role,
    name,
    token,
    isAuthenticated,
    isStudent,
    isInspector,
    isAdmin,
    login,
    logout
  }
})
