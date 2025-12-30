<template>
  <el-menu
    :default-active="activeMenu"
    class="el-menu-vertical-demo"
    :collapse="isCollapse"
    router
    background-color="#304156"
    text-color="#bfcbd9"
    active-text-color="#409EFF"
  >
    <div class="logo-container">
      <img src="../assets/vue.svg" alt="logo" class="logo" />
      <span v-if="!isCollapse" class="title">卫生管理系统</span>
    </div>

    <!-- Student Menus -->
    <template v-if="userStore.isStudent">
      <el-menu-item index="/student/home">
        <el-icon><Search /></el-icon>
        <template #title>查分首页</template>
      </el-menu-item>
    </template>

    <!-- Inspector Menus -->
    <template v-if="userStore.isInspector">
      <el-menu-item index="/inspector/submit">
        <el-icon><EditPen /></el-icon>
        <template #title>检查录入</template>
      </el-menu-item>
      <el-menu-item index="/inspector/records">
        <el-icon><List /></el-icon>
        <template #title>最近检查记录</template>
      </el-menu-item>
      <el-menu-item index="/inspector/rectification-review">
        <el-icon><CircleCheck /></el-icon>
        <template #title>整改反馈</template>
      </el-menu-item>
      <el-menu-item index="/hygiene-report">
        <el-icon><DataAnalysis /></el-icon>
        <template #title>卫生通报</template>
      </el-menu-item>
    </template>

    <!-- Admin Menus -->
    <template v-if="userStore.isAdmin">
      <el-menu-item index="/admin/dashboard">
        <el-icon><Odometer /></el-icon>
        <template #title>仪表盘</template>
      </el-menu-item>
      
      <!-- Integrated Inspector Menus -->
      <el-menu-item index="/inspector/submit">
        <el-icon><EditPen /></el-icon>
        <template #title>检查录入</template>
      </el-menu-item>
      <el-menu-item index="/inspector/records">
        <el-icon><List /></el-icon>
        <template #title>最近检查记录</template>
      </el-menu-item>
      <el-menu-item index="/inspector/rectification-review">
        <el-icon><CircleCheck /></el-icon>
        <template #title>整改反馈</template>
      </el-menu-item>
      
      <el-menu-item index="/hygiene-report">
        <el-icon><DataAnalysis /></el-icon>
        <template #title>卫生通报</template>
      </el-menu-item>
      <!-- Big Screen usually opens in new tab or full screen, but here we route to it. 
           If it needs to be standalone, we might handle it differently. 
           For now, let's keep it in layout or provide a link. -->
      <el-menu-item index="/big-screen">
        <el-icon><Monitor /></el-icon>
        <template #title>数据大屏</template>
      </el-menu-item>
    </template>

  </el-menu>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'

const props = defineProps<{
  isCollapse: boolean
}>()

const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => {
  return route.path
})
</script>

<style scoped>
.el-menu-vertical-demo {
  height: 100%;
  border-right: none;
}
.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 200px;
}
.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b3649;
  overflow: hidden;
}
.logo {
  width: 32px;
  height: 32px;
}
.title {
  margin-left: 12px;
  color: #fff;
  font-weight: 600;
  font-size: 16px;
  white-space: nowrap;
}
</style>
