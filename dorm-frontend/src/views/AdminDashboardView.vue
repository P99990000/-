<template>
  <div class="admin-dashboard-view">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h2>全校宿舍卫生数据看板</h2>
          <el-button type="primary" @click="fetchData">刷新数据</el-button>
        </div>
      </template>

      <div v-loading="loading">
        <!-- 图表区域 -->
        <div class="charts-container">
          <!-- 柱状图：各楼栋平均分 -->
          <div class="chart-wrapper">
            <h3>各楼栋平均分对比</h3>
            <div ref="barChartRef" class="chart"></div>
          </div>
          
          <!-- 饼图：全校优秀率 vs 不及格率 -->
          <div class="chart-wrapper">
            <h3>全校卫生状况分布</h3>
            <div ref="pieChartRef" class="chart"></div>
          </div>
        </div>
        
        <!-- 全校所有宿舍卫生情况散点图 -->
        <div class="chart-full-width">
          <h3>全校所有宿舍卫生评分分布</h3>
          <div ref="scatterChartRef" class="chart-large"></div>
        </div>

        <el-divider />

        <!-- 表格展示详细数据 -->
        <h3>详细数据</h3>
        <el-table :data="stats" style="width: 100%" stripe border>
          <el-table-column prop="building" label="楼栋" width="120" />
          <el-table-column prop="avgScore" label="平均分" width="120" sortable>
            <template #default="scope">
              <el-tag :type="getScoreType(scope.row.avgScore)">
                {{ scope.row.avgScore }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="totalChecks" label="总检查次数" width="120" sortable />
          <el-table-column prop="excellentRate" label="优秀率" width="120" sortable>
            <template #default="scope">
              {{ (scope.row.excellentRate * 100).toFixed(1) }}%
            </template>
          </el-table-column>
           <el-table-column prop="excellentCount" label="优秀次数" width="120" />
           <el-table-column prop="failCount" label="不及格次数" width="120" />
        </el-table>

        <!-- AI 分析区域 -->
        <div class="ai-section">
          <el-card class="ai-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="ai-title">📊 AI 智能分析</span>
                <el-button 
                  type="primary" 
                  :loading="aiLoading" 
                  @click="generateAiReport"
                  class="ai-btn"
                >
                  <el-icon class="el-icon--left"><MagicStick /></el-icon>
                  生成分析报告
                </el-button>
              </div>
            </template>
            <div class="ai-content-wrapper">
              <el-input
                v-model="aiReport"
                type="textarea"
                :rows="3"
                resize="none"
                readonly
                placeholder="点击按钮生成基于全校数据的智能分析报告..."
                class="ai-textarea"
              />
            </div>
          </el-card>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick } from '@element-plus/icons-vue'
import axios from 'axios'
import * as echarts from 'echarts'

// 数据接口定义
interface BuildingStats {
  building: string
  avgScore: number
  totalChecks: number
  excellentRate: number
  excellentCount: number
  failCount: number
}

interface DormScore {
  building: string
  room: string
  score: number | null
}

// 状态
const loading = ref(false)
const stats = ref<BuildingStats[]>([])
const dormScores = ref<DormScore[]>([])
const barChartRef = ref<HTMLElement | null>(null)
const pieChartRef = ref<HTMLElement | null>(null)
const scatterChartRef = ref<HTMLElement | null>(null)
let barChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null
let scatterChart: echarts.ECharts | null = null

// AI Report State
const aiLoading = ref(false)
const aiReport = ref('')

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const [statsRes, scoresRes] = await Promise.all([
      axios.get('/api/admin/building-stats'),
      axios.get('/api/admin/all-dorm-scores')
    ])
    
    if (statsRes.data.code === 200) {
      stats.value = statsRes.data.data
    } else {
      ElMessage.error('获取楼栋统计失败: ' + statsRes.data.message)
    }

    if (scoresRes.data.code === 200) {
      dormScores.value = scoresRes.data.data
    } else {
      ElMessage.error('获取宿舍评分失败: ' + scoresRes.data.message)
    }

    await nextTick()
    initCharts()

  } catch (error: any) {
    ElMessage.error('网络错误: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 初始化图表
const initCharts = () => {
  if (stats.value.length === 0 && dormScores.value.length === 0) return

  initBarChart()
  initPieChart()
  initScatterChart()
}

// 散点图：全校宿舍评分分布
const initScatterChart = () => {
  if (!scatterChartRef.value) return
  
  if (scatterChart) scatterChart.dispose()
  scatterChart = echarts.init(scatterChartRef.value)

  // 准备数据
  // X轴: 楼栋 (Categories)
  // Y轴: 分数
  // Series Data: [BuildingIndex, Score, BuildingName, RoomName]

  // 1. 获取所有楼栋列表并排序
  const buildings = Array.from(new Set(dormScores.value.map(d => d.building))).sort()
  
  // 2. 构造 Series Data
  const seriesData = dormScores.value.map(d => {
    const buildingIndex = buildings.indexOf(d.building)
    return [buildingIndex, d.score || 0, d.building, d.room]
  })

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => {
        const data = params.data
        return `${data[2]} ${data[3]}<br/>分数: ${data[1]}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: buildings,
      axisLabel: {
        interval: 0,
        rotate: 30
      },
      splitLine: {
        show: true,
        lineStyle: {
          type: 'dashed'
        }
      }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      splitLine: {
        show: true
      }
    },
    dataZoom: [
      {
        type: 'slider',
        show: true,
        xAxisIndex: [0],
        start: 0,
        end: 100
      },
      {
        type: 'inside',
        xAxisIndex: [0],
        start: 0,
        end: 100
      }
    ],
    series: [
      {
        name: '宿舍评分',
        type: 'scatter',
        symbolSize: 8,
        data: seriesData,
        itemStyle: {
          color: (params: any) => {
            const score = params.data[1]
            if (score >= 90) return '#67C23A' // 优秀
            if (score < 60) return '#F56C6C' // 不及格
            return '#409EFF' // 合格
          },
          opacity: 0.7
        }
      }
    ]
  }

  scatterChart.setOption(option)
}

// 柱状图：各楼栋平均分
const initBarChart = () => {
  if (!barChartRef.value) return
  
  if (barChart) barChart.dispose()
  barChart = echarts.init(barChartRef.value)

  const buildings = stats.value.map(s => s.building)
  const avgScores = stats.value.map(s => s.avgScore)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: [
      {
        type: 'category',
        data: buildings,
        axisTick: { alignWithLabel: true }
      }
    ],
    yAxis: [
      {
        type: 'value',
        max: 100
      }
    ],
    series: [
      {
        name: '平均分',
        type: 'bar',
        barWidth: '60%',
        data: avgScores,
        itemStyle: {
          color: (params: any) => {
            const score = params.data
            if (score >= 90) return '#67C23A' // Success
            if (score < 60) return '#F56C6C' // Danger
            return '#409EFF' // Primary
          }
        }
      }
    ]
  }

  barChart.setOption(option)
}

// 饼图：全校卫生状况分布
const initPieChart = () => {
  if (!pieChartRef.value) return
  
  if (pieChart) pieChart.dispose()
  pieChart = echarts.init(pieChartRef.value)

  // 汇总全校数据
  let totalExcellent = 0
  let totalFail = 0
  let totalChecks = 0

  stats.value.forEach(s => {
    totalExcellent += s.excellentCount
    totalFail += s.failCount
    totalChecks += s.totalChecks
  })

  const totalOther = totalChecks - totalExcellent - totalFail

  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      top: '5%',
      left: 'center'
    },
    series: [
      {
        name: '卫生状况',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: totalExcellent, name: '优秀 (≥90)', itemStyle: { color: '#67C23A' } },
          { value: totalFail, name: '不及格 (<60)', itemStyle: { color: '#F56C6C' } },
          { value: totalOther, name: '合格 (60-89)', itemStyle: { color: '#E6A23C' } }
        ]
      }
    ]
  }

  pieChart.setOption(option)
}

// 辅助函数：分数颜色
const getScoreType = (score: number) => {
  if (score >= 90) return 'success'
  if (score < 60) return 'danger'
  return 'primary'
}

// AI Report Generation
const generateAiReport = async () => {
  if (aiLoading.value) return
  
  // Simple debounce/cache check logic could be here, but backend also has cache.
  // We rely on backend cache mostly, but frontend can also prevent double clicks (handled by loading state).
  
  aiLoading.value = true
  aiReport.value = 'AI 正在深入分析全校卫生数据，请稍候...'
  
  try {
    const res = await axios.post('/api/admin/ai-report')
    if (res.data.code === 200) {
      aiReport.value = res.data.data
      ElMessage.success('AI 分析报告生成成功')
    } else {
      aiReport.value = '' // Clear on error or keep previous? Let's clear or show error msg
      ElMessage.error('生成失败: ' + res.data.message)
    }
  } catch (error: any) {
    aiReport.value = ''
    ElMessage.error('AI 服务连接失败: ' + (error.message || '未知错误'))
  } finally {
    aiLoading.value = false
  }
}

// 响应式调整
const handleResize = () => {
  barChart?.resize()
  pieChart?.resize()
  scatterChart?.resize()
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  barChart?.dispose()
  pieChart?.dispose()
  scatterChart?.dispose()
})
</script>

<style scoped>
.admin-dashboard-view {
  margin: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.charts-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-wrapper {
  flex: 1;
  min-width: 400px;
  height: 400px;
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  padding: 10px;
  background-color: #fff;
}

.chart-full-width {
  width: 100%;
  height: 500px;
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  padding: 10px;
  background-color: #fff;
  margin-bottom: 20px;
}

.chart-full-width h3 {
  text-align: center;
  margin-top: 0;
  color: #606266;
}

.chart-large {
  width: 100%;
  height: 450px;
}

.chart-wrapper h3 {
  text-align: center;
  margin-top: 0;
  color: #606266;
}

.chart {
  width: 100%;
  height: 350px;
}

.ai-section {
  margin-top: 24px;
}

.ai-card {
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s;
}

.ai-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.ai-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.ai-btn {
  font-weight: 500;
}

.ai-content-wrapper {
  background-color: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
}

/* Deep selector for textarea styling */
:deep(.ai-textarea .el-textarea__inner) {
  font-size: 16px;
  line-height: 1.6;
  color: #2c3e50;
  background-color: transparent;
  border: none;
  box-shadow: none;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
</style>
