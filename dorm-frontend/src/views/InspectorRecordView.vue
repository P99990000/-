<template>
  <div class="inspector-record-view">
    <div class="content-container">
      <div class="page-header">
        <div class="header-content">
          <div class="header-icon">📋</div>
          <h2>最近检查记录</h2>
        </div>
        <p class="header-subtitle">查看最近提交的卫生检查记录及整改状态</p>
      </div>

      <el-card class="main-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>检查记录列表 (实时更新)</span>
            <el-button type="primary" link @click="fetchRecentRecords">刷新</el-button>
          </div>
        </template>
        <el-table :data="recentRecords" style="width: 100%" stripe v-loading="loading">
          <el-table-column prop="checkDate" label="检查日期" width="120" />
          <el-table-column label="宿舍" width="150">
            <template #default="{ row }">
              {{ row.buildingName }} {{ row.roomNumber }}
            </template>
          </el-table-column>
          <el-table-column prop="inspectorName" label="检查人" width="100" />
          <el-table-column prop="totalScore" label="得分" width="80">
             <template #default="{ row }">
               <span :class="{'score-low': row.totalScore < 60, 'score-high': row.totalScore >= 90}">{{ row.totalScore }}</span>
             </template>
          </el-table-column>
          <el-table-column label="整改状态" width="120">
            <template #default="{ row }">
              <el-tag v-if="row.rectificationStatus === 4" type="danger">待整改</el-tag>
              <el-tag v-else-if="row.rectificationStatus === 1" type="warning">审核中</el-tag>
              <el-tag v-else-if="row.rectificationStatus === 2" type="success">整改通过</el-tag>
              <el-tag v-else-if="row.rectificationStatus === 3" type="danger">已驳回</el-tag>
              <el-tag v-else type="info">无</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="rectificationDesc" label="整改反馈/说明" show-overflow-tooltip />
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

interface InspectionRecord {
  id: number
  checkDate: string
  buildingName: string
  roomNumber: string
  inspectorName: string
  totalScore: number
  rectificationStatus: number
  rectificationDesc: string
}

const recentRecords = ref<InspectionRecord[]>([])
const loading = ref(false)

const fetchRecentRecords = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/records/recent')
    if (res.data.code === 200) {
      recentRecords.value = res.data.data
    } else {
      ElMessage.error(res.data.message || '获取记录失败')
    }
  } catch (error) {
    console.error('Failed to fetch recent records:', error)
    ElMessage.error('获取记录失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchRecentRecords()
})
</script>

<style scoped>
.inspector-record-view {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.score-low {
  color: #f56c6c;
  font-weight: bold;
}

.score-high {
  color: #67c23a;
  font-weight: bold;
}
</style>
