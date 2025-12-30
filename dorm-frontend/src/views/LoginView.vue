<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>卫生管理系统登录</h2>
        </div>
      </template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" class="login-form">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item label="验证码" prop="captcha">
          <div class="captcha-row">
            <el-input v-model="form.captcha" placeholder="验证码" class="captcha-input" @keyup.enter="handleLogin" />
            <div class="captcha-img" @click="refreshCaptcha" title="点击刷新">
              <img :src="captchaUrl" alt="验证码" />
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin" style="width: 100%;">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  captcha: ''
})

const rules = reactive<FormRules>({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
})

const captchaUrl = ref('')

const refreshCaptcha = () => {
  // Add timestamp to prevent caching
  captchaUrl.value = '/api/auth/captcha?t=' + new Date().getTime()
}

onMounted(() => {
  refreshCaptcha()
})

const handleLogin = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid, fields) => {
    if (fields) {
      console.log('Validation failed:', fields)
    }
    if (valid) {
      loading.value = true
      try {
        const res = await axios.post('/api/auth/login', {
          username: form.username,
          password: form.password,
          captcha: form.captcha
        })
        
        if (res.data.code === 200) {
          const { token, role, username } = res.data.data
          userStore.login(role, username, token)
          ElMessage.success('登录成功')
          
          // Redirect based on role
          if (role === 'student') {
            router.push('/student/home')
          } else if (role === 'inspector') {
            router.push('/inspector/submit')
          } else if (role === 'admin') {
            router.push('/admin/dashboard')
          } else {
            router.push('/')
          }
        } else {
          ElMessage.error(res.data.message || '登录失败')
          refreshCaptcha()
        }
      } catch (error) {
        console.error('Login error:', error)
        ElMessage.error('登录请求失败')
        refreshCaptcha()
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh; /* Allow scrolling if content is taller than screen */
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f2f5;
  background-image: url('https://images.unsplash.com/photo-1555854877-bab0e564b8d5?ixlib=rb-1.2.1&auto=format&fit=crop&w=1920&q=80');
  background-size: cover;
  padding: 20px; /* Add padding for small screens */
  box-sizing: border-box;
}
.login-card {
  width: 100%;
  max-width: 400px; /* Responsive width */
  background-color: rgba(255, 255, 255, 0.95);
}
.card-header {
  text-align: center;
}
.captcha-row {
  display: flex;
  align-items: center;
  gap: 10px; /* Use gap instead of margin */
}
.captcha-input {
  flex: 1; /* Allow input to shrink/grow */
  min-width: 0; /* Prevent overflow */
}
.captcha-img {
  cursor: pointer;
  width: 100px; /* Explicit width */
  height: 32px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0; /* Prevent image from shrinking */
  background-color: #f5f7fa; /* Placeholder color */
}
.captcha-img img {
  height: 100%;
  display: block;
}
.divider {
  display: flex; 
  align-items: center; 
  text-align: center; 
  margin: 15px 0; 
  color: #909399; 
  font-size: 14px;
}
.divider::before, .divider::after {
  content: ''; 
  flex: 1; 
  border-bottom: 1px solid #dcdfe6; 
}
.divider::before {
  margin-right: 10px;
}
.divider::after {
  margin-left: 10px;
}

/* Mobile optimizations */
@media (max-width: 768px) {
  .login-card {
    max-width: 90%;
  }
  .login-card :deep(.el-form-item__label) {
    float: none; /* Stack label and input */
    display: block;
    text-align: left;
    padding: 0 0 8px;
  }
  .login-card :deep(.el-form-item__content) {
    margin-left: 0 !important;
  }
}
</style>
