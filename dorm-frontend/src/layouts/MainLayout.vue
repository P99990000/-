<template>
  <el-container class="layout-container">
    <el-aside width="auto">
      <SidebarMenu :is-collapse="isCollapse" />
    </el-aside>
    <el-container>
      <el-header height="60px" style="padding: 0;">
        <TopHeader :is-collapse="isCollapse" @toggle-collapse="toggleCollapse" />
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import SidebarMenu from '../components/SidebarMenu.vue'
import TopHeader from '../components/TopHeader.vue'

const isCollapse = ref(false)

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.el-header {
  background-color: #fff;
  color: #333;
}
.el-aside {
  background-color: #304156;
  transition: width 0.3s;
}
.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
