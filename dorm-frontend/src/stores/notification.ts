import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface NotificationMessage {
  id: string
  avatar?: string
  nickname: string
  content: string
  targetRoute?: string
  timestamp: number
}

export const useNotificationStore = defineStore('notification', () => {
  const queue = ref<NotificationMessage[]>([])
  const currentNotification = ref<NotificationMessage | null>(null)
  const isShowing = ref(false)

  const addNotification = (msg: Omit<NotificationMessage, 'id' | 'timestamp'>) => {
    const newMsg: NotificationMessage = {
      ...msg,
      id: Date.now().toString() + Math.random().toString(36).substr(2, 9),
      timestamp: Date.now()
    }
    
    // If nothing is showing, show immediately (handled by watcher in component or explicit call)
    // Here we just add to queue. The component will pick it up.
    queue.value.push(newMsg)
    processQueue()
  }

  const processQueue = () => {
    if (isShowing.value || queue.value.length === 0) return
    
    const nextMsg = queue.value.shift()
    if (nextMsg) {
      currentNotification.value = nextMsg
      isShowing.value = true
    }
  }

  const dismissCurrent = () => {
    isShowing.value = false
    // Wait for animation to finish before processing next
    setTimeout(() => {
      currentNotification.value = null
      processQueue()
    }, 300) // Match animation duration
  }

  return {
    queue,
    currentNotification,
    isShowing,
    addNotification,
    dismissCurrent
  }
})
