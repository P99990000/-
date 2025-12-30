<template>
  <div class="notification-container">
    <NotificationToast 
      v-if="store.isShowing && store.currentNotification"
      :message="store.currentNotification"
      @close="handleClose"
      @click="handleClick"
    />
  </div>
</template>

<script setup lang="ts">
import { useNotificationStore } from '../stores/notification'
import NotificationToast from './NotificationToast.vue'
import { useRouter } from 'vue-router'

const store = useNotificationStore()
const router = useRouter()

const handleClose = () => {
  store.dismissCurrent()
}

const handleClick = () => {
  const target = store.currentNotification?.targetRoute || '/chat'
  // Dismiss first then navigate
  store.dismissCurrent()
  router.push(target)
}
</script>

<style scoped>
.notification-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 9999;
  pointer-events: none; /* Let clicks pass through if no toast */
}

.notification-container > * {
  pointer-events: auto; /* Re-enable pointer events for toast */
}
</style>
