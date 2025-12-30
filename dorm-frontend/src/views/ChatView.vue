<template>
  <div class="chat-view">
    <div class="chat-header">
      <el-button :icon="ArrowLeft" @click="$router.back()" circle text />
      <h2>消息中心</h2>
      <div style="width: 32px"></div>
    </div>
    
    <div class="chat-list">
      <el-empty description="暂无历史消息" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ArrowLeft } from '@element-plus/icons-vue'
</script>

<style scoped>
.chat-view {
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.chat-header {
  height: 50px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 10px;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  z-index: 10;
}

.chat-header h2 {
  font-size: 16px;
  margin: 0;
  font-weight: 600;
}

.chat-list {
  flex: 1;
  padding: 20px;
}
</style>
