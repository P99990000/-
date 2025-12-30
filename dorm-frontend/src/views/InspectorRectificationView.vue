<template>
  <div class="inspector-rectification-view">
    <div class="content-container">
      <div class="page-header">
        <div class="header-content">
          <div class="header-icon">🔧</div>
          <h2>不合格宿舍整改反馈</h2>
          <el-button 
            type="info" 
            circle 
            plain 
            :icon="Bell" 
            @click="testNotification" 
            style="margin-left: 10px;" 
            title="测试消息提醒"
          />
        </div>
        <p class="header-subtitle">审核学生提交的整改申请</p>
      </div>

      <el-card class="main-card" shadow="never">
        <div class="rectification-list">
          <el-empty v-if="pendingRectifications.length === 0" description="暂无待审核的整改申请" />
          <div v-else class="rectification-cards">
            <el-card v-for="item in pendingRectifications" :key="item.id" class="rectification-card">
              <div class="rect-header">
                <span class="dorm-name">{{ item.buildingName || '未知楼栋' }} {{ item.roomNumber || '未知房间' }}</span>
                <span class="rect-time">{{ item.updatedAt }}</span>
              </div>
              <div class="rect-content">
                <div class="rect-reason">
                  <strong>不合格原因/备注:</strong> {{ item.remark }}
                </div>
                <div class="rect-desc">
                  <strong>整改说明:</strong> {{ item.rectificationDesc }}
                </div>
                <div class="rect-img" v-if="item.rectificationImageUrl">
                  <el-image 
                    :src="item.rectificationImageUrl" 
                    :preview-src-list="[item.rectificationImageUrl]"
                    fit="cover"
                    style="width: 100px; height: 100px; border-radius: 4px;"
                  />
                </div>
              </div>
              <div class="rect-actions">
                <el-button type="success" size="small" @click="reviewRectification(item, true)">整改通过</el-button>
                <el-button type="danger" size="small" @click="reviewRectification(item, false)">驳回</el-button>
              </div>
            </el-card>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { useNotificationStore } from '../stores/notification'
import { Bell } from '@element-plus/icons-vue'

const notificationStore = useNotificationStore()

const testNotification = () => {
  const messages = [
    '收到一条新的整改申请，请及时处理。',
    '302宿舍提交了新的卫生照片。',
    '管理员发布了新的卫生检查通知。',
    '系统将于今晚22:00进行维护。'
  ]
  const randomMsg = messages[Math.floor(Math.random() * messages.length)] || ''
  
  notificationStore.addNotification({
    nickname: '系统通知',
    content: randomMsg,
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    targetRoute: '/inspector/rectification-review'
  })
}

interface InspectionRecord {
  id: number
  buildingName?: string
  roomNumber?: string
  dormId: number
  remark: string
  rectificationDesc: string
  rectificationImageUrl: string
  updatedAt: string
}

const pendingRectifications = ref<InspectionRecord[]>([])

const fetchPendingRectifications = async () => {
  try {
    const res = await axios.get('/api/records/rectification/pending')
    if (res.data.code === 200) {
      pendingRectifications.value = res.data.data
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取整改列表失败')
  }
}

const reviewRectification = async (item: InspectionRecord, pass: boolean) => {
  let rejectReason = ''
  if (!pass) {
    try {
      const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回整改', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /\S/,
        inputErrorMessage: '驳回原因不能为空'
      })
      rejectReason = value
    } catch {
      return
    }
  }

  try {
    const res = await axios.post('/api/records/rectification/review', {
      recordId: item.id,
      pass: pass,
      rejectReason: rejectReason
    })
    
    if (res.data.code === 200) {
      ElMessage.success(pass ? '已通过' : '已驳回')
      fetchPendingRectifications()
    } else {
      ElMessage.error(res.data.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  fetchPendingRectifications()
})
</script>

<style scoped>
.inspector-rectification-view {
  min-height: 100vh;
  background-color: #f0f2f5;
  padding: 20px;
  display: flex;
  justify-content: center;
}

.content-container {
  width: 100%;
  max-width: 1000px;
}

.page-header {
  margin-bottom: 24px;
  text-align: center;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 8px;
}

.header-icon {
  font-size: 32px;
}

.page-header h2 {
  margin: 0;
  font-size: 28px;
  color: #1f2937;
  font-weight: 600;
}

.header-subtitle {
  color: #6b7280;
  margin: 0;
  font-size: 14px;
}

.main-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06) !important;
}

/* Rectification Cards */
.rectification-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.rectification-card {
  margin-bottom: 10px;
}

.rect-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-weight: bold;
  color: #333;
}

.rect-time {
  color: #999;
  font-size: 0.9em;
}

.rect-content {
  margin-bottom: 15px;
}

.rect-reason, .rect-desc {
  margin-bottom: 8px;
  color: #666;
}

.rect-img {
  margin-top: 10px;
}

.rect-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
