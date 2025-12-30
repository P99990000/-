<template>
  <div 
    class="notification-toast"
    :class="{ 'is-closing': isClosing }"
    @click="handleClick"
    @touchstart="handleTouchStart"
    @touchmove="handleTouchMove"
    @touchend="handleTouchEnd"
    :style="toastStyle"
  >
    <div class="notification-content">
      <div class="avatar">
        <img :src="message.avatar || defaultAvatar" alt="avatar" />
      </div>
      <div class="text-content">
        <div class="header">
          <span class="nickname">{{ message.nickname }}</span>
          <span class="time">现在</span>
        </div>
        <div class="message-body">{{ message.content }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import type { NotificationMessage } from '../stores/notification'

const props = defineProps<{
  message: NotificationMessage
}>()

const emit = defineEmits(['close', 'click'])

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const isClosing = ref(false)
const startY = ref(0)
const currentY = ref(0)
const isDragging = ref(false)

// Auto close timer
let timer: ReturnType<typeof setTimeout>

const toastStyle = computed(() => {
  if (isDragging.value) {
    const translateY = Math.min(0, currentY.value - startY.value)
    return {
      transform: `translateY(${translateY}px)`,
      transition: 'none'
    }
  }
  return {}
})

const startTimer = () => {
  timer = setTimeout(() => {
    close()
  }, 4000) // 4 seconds
}

const close = () => {
  isClosing.value = true
  emit('close')
}

const handleClick = () => {
  emit('click')
}

const handleTouchStart = (e: TouchEvent) => {
  if (e.touches && e.touches[0]) {
    startY.value = e.touches[0].clientY
    isDragging.value = true
    clearTimeout(timer) // Pause timer on interaction
  }
}

const handleTouchMove = (e: TouchEvent) => {
  if (!isDragging.value) return
  if (e.touches && e.touches[0]) {
    currentY.value = e.touches[0].clientY
  }
}

const handleTouchEnd = () => {
  isDragging.value = false
  const diff = currentY.value - startY.value
  if (diff < -30) {
    // Swipe up detected
    close()
  } else {
    // Resume timer
    startTimer()
  }
}

onMounted(() => {
  startTimer()
})

onUnmounted(() => {
  clearTimeout(timer)
})
</script>

<style scoped>
.notification-toast {
  position: fixed;
  top: 10px;
  left: 10px;
  right: 10px;
  max-width: 500px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 12px;
  z-index: 9999;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  animation: slideDown 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  user-select: none;
}

.notification-toast.is-closing {
  animation: slideUp 0.3s cubic-bezier(0.25, 0.8, 0.25, 1) forwards;
}

.notification-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.avatar img {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  object-fit: cover;
}

.text-content {
  flex: 1;
  min-width: 0; /* Enable truncation */
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.nickname {
  font-weight: 600;
  font-size: 15px;
  color: #333;
}

.time {
  font-size: 12px;
  color: #999;
}

.message-body {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-100%);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideUp {
  from {
    opacity: 1;
    transform: translateY(0);
  }
  to {
    opacity: 0;
    transform: translateY(-100%);
  }
}
</style>
