<template>
  <div class="report-container">
    <div class="report-header">
      <div class="header-title">
        <el-icon class="icon"><DataAnalysis /></el-icon>
        <h1>宿舍卫生通报</h1>
      </div>
      <div class="header-controls">
        <el-radio-group v-model="timeRange" size="large">
          <el-radio-button label="week">本周</el-radio-button>
          <el-radio-button label="month">本月</el-radio-button>
        </el-radio-group>
        <el-button type="success" @click="goToSubmit" :icon="Plus">新增检查</el-button>
        <el-button type="primary" @click="openExportDialog" :icon="Download">导出报告</el-button>
      </div>
    </div>

    <!-- Export Dialog -->
    <el-dialog v-model="exportDialogVisible" title="导出选项" width="400px">
      <el-form label-position="top">
        <el-form-item label="筛选条件">
          <el-radio-group v-model="exportFilterType">
            <el-radio label="all">所有数据</el-radio>
            <el-radio label="fail">不合格宿舍 (< 60分)</el-radio>
            <el-radio label="notice">整改通报宿舍</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="exportDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmExport">导出</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Summary Cards -->
    <el-row :gutter="20" class="summary-row">
      <el-col :span="8">
        <el-card shadow="hover" class="summary-card blue-card">
          <template #header>
            <div class="card-header">
              <span>检查宿舍数</span>
              <el-tag type="primary" effect="dark">Total</el-tag>
            </div>
          </template>
          <div class="card-value">{{ summary.totalDorms }}</div>
          <div class="card-footer">覆盖率 {{ summary.coverage }}%</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="summary-card green-card">
          <template #header>
            <div class="card-header">
              <span>平均分</span>
              <el-tag type="success" effect="dark">Avg</el-tag>
            </div>
          </template>
          <div class="card-value">{{ summary.avgScore }}</div>
          <div class="card-footer">环比 {{ summary.avgScoreTrend > 0 ? '+' : '' }}{{ summary.avgScoreTrend }}%</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="summary-card orange-card">
          <template #header>
            <div class="card-header">
              <span>及格率</span>
              <el-tag type="warning" effect="dark">Pass</el-tag>
            </div>
          </template>
          <div class="card-value">{{ summary.passRate }}%</div>
          <div class="card-footer">不及格 {{ summary.failCount }} 间</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Red/Black Lists -->
    <el-row :gutter="20" class="list-row">
      <!-- Red List -->
      <el-col :span="12">
        <el-card class="list-card" shadow="never">
          <template #header>
            <div class="list-header red-list-header">
              <span class="title">🏆 文明宿舍 (红榜)</span>
              <span class="subtitle">Top 10 High Scores</span>
            </div>
          </template>
          <el-table :data="redList" style="width: 100%" stripe :show-header="true">
            <el-table-column type="index" label="排名" width="60">
              <template #default="scope">
                <div class="rank-badge" :class="'rank-' + (scope.$index + 1)">{{ scope.$index + 1 }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="dormName" label="宿舍" />
            <el-table-column prop="score" label="分数" width="100">
              <template #default="scope">
                <span class="score-high">{{ scope.row.score }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="inspector" label="检查人" width="100" />
          </el-table>
        </el-card>
      </el-col>

      <!-- Black List -->
      <el-col :span="12">
        <el-card class="list-card" shadow="never">
          <template #header>
            <div class="list-header black-list-header">
              <span class="title">⚠️ 整改宿舍 (黑榜)</span>
              <span class="subtitle">Bottom 10 / Warning</span>
            </div>
          </template>
          <el-table :data="blackList" style="width: 100%" stripe :show-header="true">
            <el-table-column type="index" label="排名" width="60">
              <template #default="scope">
                <div class="rank-badge rank-low">{{ scope.$index + 1 }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="dormName" label="宿舍" />
            <el-table-column prop="score" label="分数" width="80">
              <template #default="scope">
                <span class="score-low">{{ scope.row.score }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="issues" label="主要问题" show-overflow-tooltip>
               <template #default="scope">
                <el-tag type="danger" size="small" v-for="(issue, i) in scope.row.issues" :key="i" class="issue-tag">{{ issue }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- Detailed Table -->
    <el-card class="detail-card" shadow="never">
      <template #header>
        <div class="detail-header">
          <span>详细记录</span>
          <div class="filter-box">
             <el-input v-model="searchQuery" placeholder="搜索宿舍号..." :prefix-icon="Search" style="width: 200px" />
          </div>
        </div>
      </template>
      <el-table :data="allDetails" style="width: 100%" v-loading="loading">
        <el-table-column prop="checkDate" label="日期" width="180" sortable :formatter="formatDate" />
        <el-table-column prop="dormName" label="宿舍" width="180" />
        <el-table-column prop="totalScore" label="分数" width="100" sortable>
          <template #default="scope">
            <span :class="getScoreClass(scope.row.totalScore)">{{ scope.row.totalScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="inspectorName" label="检查人" width="120" />
        <el-table-column prop="issues" label="问题/备注" />
      </el-table>
      <div class="pagination-container">
        <el-pagination background layout="prev, pager, next" :total="100" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { DataAnalysis, Download, Search, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const timeRange = ref('week')
const searchQuery = ref('')
const loading = ref(false)
const exportDialogVisible = ref(false)
const exportFilterType = ref('all')

// Data State
const summary = reactive({
  totalDorms: 0,
  coverage: 0,
  avgScore: 0,
  avgScoreTrend: 0,
  passRate: 0,
  failCount: 0
})

const redList = ref([])
const blackList = ref([])
const allDetails = ref([])

// Fetch Data
const fetchData = async () => {
  loading.value = true
  try {
    const [summaryRes, topRes, bottomRes, detailsRes] = await Promise.all([
      axios.get('/api/report/summary', { params: { timeRange: timeRange.value } }),
      axios.get('/api/report/rank/top', { params: { timeRange: timeRange.value } }),
      axios.get('/api/report/rank/bottom', { params: { timeRange: timeRange.value } }),
      axios.get('/api/report/details', { params: { timeRange: timeRange.value, search: searchQuery.value } })
    ])

    if (summaryRes.data.code === 200) {
      Object.assign(summary, summaryRes.data.data)
    }
    if (topRes.data.code === 200) {
      redList.value = topRes.data.data
    }
    if (bottomRes.data.code === 200) {
      blackList.value = bottomRes.data.data
    }
    if (detailsRes.data.code === 200) {
      allDetails.value = detailsRes.data.data
    }
  } catch (error) {
    console.error('Failed to fetch report data', error)
    ElMessage.error('获取通报数据失败')
  } finally {
    loading.value = false
  }
}

// Watchers
watch(timeRange, () => {
  fetchData()
})

watch(searchQuery, () => {
  // Simple debounce could be added here
  fetchData()
})

onMounted(() => {
  fetchData()
})

const getScoreClass = (score: number) => {
  if (score >= 90) return 'text-success'
  if (score >= 60) return 'text-warning'
  return 'text-danger'
}

const formatDate = (_row: any, _column: any, cellValue: string) => {
  if (!cellValue) return ''
  return new Date(cellValue).toLocaleString()
}

const goToSubmit = () => {
  router.push('/inspector/submit')
}

// const generateMockData = async () => {
//   try {
//     await ElMessageBox.confirm(
//       '确定要生成过去30天的模拟数据吗？这可能会花费几秒钟。',
//       '生成模拟数据',
//       {
//         confirmButtonText: '确定生成',
//         cancelButtonText: '取消',
//         type: 'warning',
//       }
//     )
//     
//     loading.value = true
//     const res = await axios.post('/api/mock/generate')
//     if (res.data.code === 200) {
//       ElMessage.success('模拟数据生成成功')
//       fetchData() // Refresh data
//     } else {
//       ElMessage.error(res.data.message || '生成失败')
//     }
//   } catch (error) {
//     if (error !== 'cancel') {
//       console.error(error)
//       ElMessage.error('生成数据请求失败')
//     }
//   } finally {
//     loading.value = false
//   }
// }

const openExportDialog = () => {
  exportDialogVisible.value = true
  exportFilterType.value = 'all'
}

const confirmExport = async () => {
  exportDialogVisible.value = false
  await exportReport()
}

const exportReport = async () => {
  try {
    ElMessage.info('正在生成报告...')
    const response = await axios.get('/api/report/export', {
      params: {
        timeRange: timeRange.value,
        search: searchQuery.value,
        filterType: exportFilterType.value
      },
      responseType: 'blob'
    })
    
    // Create download link
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    // Append filter type to filename
    let suffix = ''
    if (exportFilterType.value === 'fail') suffix = '_不合格'
    if (exportFilterType.value === 'notice') suffix = '_整改通报'
    
    link.setAttribute('download', `卫生检查报告${suffix}_${new Date().toISOString().slice(0,10)}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('Export failed', error)
    ElMessage.error('导出失败')
  }
}
</script>

<style scoped>
.report-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-title h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.header-title .icon {
  font-size: 28px;
  color: #409eff;
}

.header-controls {
  display: flex;
  gap: 16px;
}

/* Summary Cards */
.summary-row {
  margin-bottom: 24px;
}

.summary-card {
  border: none;
  border-radius: 8px;
}

.blue-card :deep(.el-card__header) { background: linear-gradient(135deg, #e3f2fd, #bbdefb); }
.green-card :deep(.el-card__header) { background: linear-gradient(135deg, #e8f5e9, #c8e6c9); }
.orange-card :deep(.el-card__header) { background: linear-gradient(135deg, #fff3e0, #ffe0b2); }

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #606266;
}

.card-value {
  font-size: 36px;
  font-weight: bold;
  color: #303133;
  margin: 16px 0;
  text-align: center;
}

.card-footer {
  text-align: right;
  font-size: 14px;
  color: #909399;
}

/* Lists */
.list-row {
  margin-bottom: 24px;
}

.list-card {
  height: 100%;
  border-radius: 8px;
}

.list-header {
  display: flex;
  flex-direction: column;
}

.list-header .title {
  font-size: 18px;
  font-weight: bold;
}

.list-header .subtitle {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.red-list-header .title { color: #f56c6c; }
.black-list-header .title { color: #303133; } /* Black list title can be dark */

.rank-badge {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  background-color: #f0f2f5;
  color: #909399;
}

.rank-1 { background-color: #f56c6c; color: #fff; }
.rank-2 { background-color: #e6a23c; color: #fff; }
.rank-3 { background-color: #409eff; color: #fff; }

.rank-low {
  background-color: #303133;
  color: #fff;
}

.score-high { color: #67c23a; font-weight: bold; }
.score-low { color: #f56c6c; font-weight: bold; }

.issue-tag { margin-right: 4px; }

/* Detail Table */
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.text-success { color: #67c23a; }
.text-warning { color: #e6a23c; }
.text-danger { color: #f56c6c; }

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>