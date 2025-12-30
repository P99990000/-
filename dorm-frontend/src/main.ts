import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import './style.css'
import App from './App.vue'
import router from './router'
import axios from 'axios'

const app = createApp(App)
const pinia = createPinia()

// Axios global configuration
axios.defaults.baseURL = '' // Use relative path to let Vite proxy handle it
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('user_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}, error => {
  return Promise.reject(error)
})

axios.interceptors.response.use(response => {
  return response
}, error => {
  if (error.response && error.response.status === 401) {
    // Redirect to login if 401
    // Use window.location because router might not be ready or circular dependency
    if (window.location.pathname !== '/login') {
       localStorage.removeItem('user_token')
       localStorage.removeItem('user_role')
       window.location.href = '/login'
    }
  }
  return Promise.reject(error)
})

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(ElementPlus)
app.use(router)

app.mount('#app')
