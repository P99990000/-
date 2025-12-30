<template>
  <div class="header-container">
    <div class="left">
      <el-icon class="fold-btn" @click="$emit('toggle-collapse')">
        <Fold v-if="!isCollapse" />
        <Expand v-else />
      </el-icon>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>{{ currentRouteName }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="right">
      <el-dropdown trigger="click" @command="handleCommand">
        <span class="el-dropdown-link">
          <el-avatar :size="32" icon="UserFilled" />
          <span class="username">{{ userStore.name }}</span>
          <el-tag size="small" effect="plain" class="role-tag">{{ roleName }}</el-tag>
          <el-icon class="el-icon--right"><arrow-down /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

defineProps<{
  isCollapse: boolean
}>()

const emit = defineEmits(['toggle-collapse'])

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const currentRouteName = computed(() => {
  return route.meta.title || route.name
})

const roleName = computed(() => {
  switch (userStore.role) {
    case 'student': return '学生'
    case 'inspector': return '检查员'
    case 'admin': return '管理员'
    default: return '访客'
  }
})

const handleCommand = (command: string) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.header-container {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}
.left {
  display: flex;
  align-items: center;
}
.fold-btn {
  font-size: 20px;
  cursor: pointer;
  margin-right: 20px;
}
.right {
  display: flex;
  align-items: center;
}
.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
}
.username {
  margin-left: 8px;
  margin-right: 8px;
  font-weight: 500;
}
.role-tag {
  margin-right: 4px;
}
</style>
