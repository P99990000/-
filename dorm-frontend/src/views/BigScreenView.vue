

<template>
  <div class="big-screen-container">
    <!-- Header (8vh) -->
    <header class="header">
      <div class="header-left">
        <span class="time">{{ currentTime }}</span>
      </div>
      <div class="header-center">
        <h1>学校宿舍卫生管理数据大屏</h1>
        <div class="title-decoration"></div>
      </div>
      <div class="header-right">
        <!-- Optional status indicators -->
        <div class="status-dot"></div>
        <span>系统运行正常</span>
      </div>
    </header>

    <!-- Main Content (Remaining height) -->
    <div class="main-content">
      
      <!-- Row 1: Metrics Cards (12vh) -->
      <div class="section-cards">
        <div class="data-card">
          <div class="card-icon blue"><el-icon><DataLine /></el-icon></div>
          <div class="card-info">
            <div class="label">总检查次数</div>
            <div class="value counter">{{ summary.totalChecks || 0 }}</div>
          </div>
          <div class="card-decoration"></div>
        </div>
        <div class="data-card">
          <div class="card-icon green"><el-icon><Trophy /></el-icon></div>
          <div class="card-info">
            <div class="label">平均分</div>
            <div class="value counter">{{ summary.avgScore || 0 }}</div>
          </div>
          <div class="card-decoration"></div>
        </div>
        <div class="data-card">
          <div class="card-icon orange"><el-icon><Calendar /></el-icon></div>
          <div class="card-info">
            <div class="label">今日检查数</div>
            <div class="value counter">{{ summary.todayChecks || 0 }}</div>
          </div>
          <div class="card-decoration"></div>
        </div>
        <div class="data-card">
          <div class="card-icon red"><el-icon><Warning /></el-icon></div>
          <div class="card-info">
            <div class="label">最频繁扣分项</div>
            <div class="value text-sm" :title="summary.topIssue">{{ summary.topIssue || '无' }}</div>
          </div>
          <div class="card-decoration"></div>
        </div>
      </div>

      <!-- Row 2: Area & Table (32vh) -->
      <div class="section-middle">
        <!-- Left: Area Compare (30%) -->
        <div class="panel area-panel">
          <div class="panel-header">宿舍查询与对比</div>
          <div class="panel-body query-panel">
            <!-- Filter Bar -->
            <div class="query-filter">
              <select v-model="filter.campus" class="query-select">
                <option value="">选择校区</option>
                <option value="North">北区</option>
                <option value="South">南区</option>
              </select>
              <select v-model="filter.building" class="query-select" :disabled="!filter.campus">
                <option value="">选择楼栋</option>
                <option v-for="b in buildingOptions" :key="b" :value="b">{{ b }}</option>
              </select>
              <select v-model="filter.floor" class="query-select" :disabled="!filter.building">
                <option value="">选择楼层</option>
                <option v-for="f in floorOptions" :key="f" :value="f">{{ f }}层</option>
              </select>
              <select v-model="filter.room" class="query-select" :disabled="!filter.floor">
                <option value="">选择宿舍</option>
                <option v-for="r in roomOptions" :key="r" :value="r">{{ r }}</option>
              </select>
            </div>
            
            <!-- Result Display -->
            <div v-if="filter.campus" class="query-result">
              <div class="result-score">
                <div class="score-label">{{ getResultLabel() }}</div>
                <div class="score-main">
                  <span class="score-value" :class="getScoreClass(queryScore || 0)">{{ formatScore(queryScore) }}</span>
                  <span class="score-unit">分</span>
                </div>
                <div v-if="queryIssues && filter.room" class="score-issues">
                   <span class="issue-label">扣分项:</span>
                   <span class="issue-content">{{ queryIssues }}</span>
                </div>
              </div>
            </div>
            
            <!-- Default Chart (Show only when no filter active) -->
            <div v-else ref="areaChartRef" class="chart-container"></div>
          </div>
          <div class="panel-corner corner-tl"></div>
          <div class="panel-corner corner-tr"></div>
          <div class="panel-corner corner-bl"></div>
          <div class="panel-corner corner-br"></div>
        </div>

        <!-- Right: Latest Records (70%) -->
        <div class="panel table-panel">
          <div class="panel-header">最新检查记录</div>
          <div class="panel-body table-body">
            <div class="table-header-row">
              <span class="col-dorm">宿舍</span>
              <span class="col-score">分数</span>
              <span class="col-inspector">检查人</span>
              <span class="col-header-issue">扣分项</span>
              <span class="col-time">时间</span>
            </div>
            <div class="table-content-wrapper">
              <div class="table-scroll-container" ref="tableScrollRef" @mouseenter="stopScroll" @mouseleave="startTableScroll">
                <div class="table-row" v-for="(item, index) in latestRecords" :key="index" :class="{ 'row-alt': index % 2 === 1 }">
                  <span class="col-dorm">{{ item.dorm }}</span>
                  <span class="col-score" :class="getScoreClass(item.score)">{{ item.score }}</span>
                  <span class="col-inspector">{{ item.inspectorName || '系统' }}</span>
                  <div class="col-issue-wrapper">
                     <span class="col-issue" :title="item.issues">{{ item.issues || '无' }}</span>
                  </div>
                  <span class="col-time">{{ item.time.substring(5) }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="panel-corner corner-tl"></div>
          <div class="panel-corner corner-tr"></div>
          <div class="panel-corner corner-bl"></div>
          <div class="panel-corner corner-br"></div>
        </div>
      </div>

      <!-- Row 3: Bottom Charts (Remaining ~45vh) -->
      <div class="section-bottom">
        <!-- Col 1: Building Rank -->
        <div class="panel">
          <div class="panel-header">各楼栋平均分排行</div>
          <div class="panel-body">
            <div class="query-filter rank-filter">
              <el-select 
                v-model="rankFilter.campus" 
                class="query-select-el" 
                placeholder="校区"
                popper-class="big-screen-select-popper"
                size="small"
                clearable
              >
                <el-option label="北区" value="North" />
                <el-option label="南区" value="South" />
              </el-select>
              
              <el-select 
                v-model="rankFilter.building" 
                class="query-select-el" 
                placeholder="楼栋"
                popper-class="big-screen-select-popper"
                :disabled="!rankFilter.campus"
                size="small"
                clearable
              >
                <el-option v-for="b in rankBuildingOptions" :key="b" :label="b" :value="b" />
              </el-select>
              
              <el-select 
                v-model="rankFilter.floor" 
                class="query-select-el" 
                placeholder="楼层"
                popper-class="big-screen-select-popper"
                :disabled="!rankFilter.building"
                size="small"
                clearable
              >
                <el-option v-for="f in rankFloorOptions" :key="f" :label="f + '层'" :value="f" />
              </el-select>
              
              <el-select 
                v-model="rankFilter.room" 
                class="query-select-el" 
                placeholder="宿舍"
                popper-class="big-screen-select-popper"
                :disabled="!rankFilter.floor"
                size="small"
                clearable
              >
                <el-option v-for="r in rankRoomOptions" :key="r" :label="r" :value="r" />
              </el-select>
            </div>
            <div ref="buildingChartRef" class="chart-container rank-chart"></div>
          </div>
          <div class="panel-corner corner-tl"></div>
          <div class="panel-corner corner-tr"></div>
          <div class="panel-corner corner-bl"></div>
          <div class="panel-corner corner-br"></div>
        </div>

        <!-- Col 2: Issues Top 10 -->
        <div class="panel">
          <div class="panel-header">扣分原因 TOP10</div>
          <div class="panel-body">
            <div ref="issueChartRef" class="chart-container"></div>
          </div>
          <div class="panel-corner corner-tl"></div>
          <div class="panel-corner corner-tr"></div>
          <div class="panel-corner corner-bl"></div>
          <div class="panel-corner corner-br"></div>
        </div>

        <!-- Col 3: Excellent Dorms -->
        <div class="panel">
          <div class="panel-header">优秀宿舍</div>
          <div class="panel-body table-body">
            <div class="table-header-row">
              <span class="col-xl-dorm">宿舍</span>
              <span class="col-xl-score">分数</span>
              <span class="col-xl-time">时间</span>
            </div>
            <div class="table-content-wrapper">
              <div class="table-scroll-container">
                <div class="table-row" v-for="(item, index) in excellentDorms" :key="index" :class="{ 'row-alt': index % 2 === 1 }">
                  <span class="col-xl-dorm">{{ item.dorm }}</span>
                  <span class="col-xl-score text-green">{{ item.score }}</span>
                  <span class="col-xl-time">{{ item.date }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="panel-corner corner-tl"></div>
          <div class="panel-corner corner-tr"></div>
          <div class="panel-corner corner-bl"></div>
          <div class="panel-corner corner-br"></div>
        </div>

        <!-- Col 4: Rectification Dorms -->
        <div class="panel">
          <div class="panel-header">宿舍整改通报</div>
          <div class="panel-body table-body">
            <div class="table-header-row">
              <span class="col-dorm">宿舍</span>
              <span class="col-score">分数</span>
              <span class="col-header-issue">整改原因</span>
            </div>
            <div class="table-content-wrapper">
              <div class="table-scroll-container">
                <div class="table-row" v-for="(item, index) in rectificationDorms" :key="index" :class="{ 'row-alt': index % 2 === 1 }">
                  <span class="col-dorm">{{ item.dorm }}</span>
                  <span class="col-score" :class="getScoreClass(item.score)">{{ item.score }}</span>
                  <div class="col-issue-wrapper">
                     <span class="col-issue" :title="item.issues">{{ item.issues || '无' }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="panel-corner corner-tl"></div>
          <div class="panel-corner corner-tr"></div>
          <div class="panel-corner corner-bl"></div>
          <div class="panel-corner corner-br"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { DataLine, Trophy, Calendar, Warning } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import axios from 'axios'

// --- Interfaces ---
interface SummaryData {
  totalChecks: number
  avgScore: number
  todayChecks: number
  topIssue: string
}

interface LatestRecord {
  dorm: string
  score: number
  issues: string
  time: string
  inspectorName: string
}

interface AreaCompare {
  northAvg: number
  southAvg: number
}

interface RankItem {
  name: string
  value: number
}

interface IssueTop10 {
  issue: string
  count: number
}

interface DailyTrend {
  date: string
  avgScore: number
}

interface ExcellentDorm {
  dorm: string
  score: number
  date: string
}

/*
interface ScoreDistribution {
  excellent: string
  good: string
  pass: string
  fail: string
}
*/

// --- State ---
const currentTime = ref('')
const timer = ref<number | null>(null)
const refreshTimer = ref<number | null>(null)
const scrollTimer = ref<number | null>(null)

const summary = ref<SummaryData>({
  totalChecks: 0,
  avgScore: 0,
  todayChecks: 0,
  topIssue: '无'
})
const latestRecords = ref<LatestRecord[]>([])
const areaData = ref<AreaCompare>({ northAvg: 0, southAvg: 0 })
const buildingRank = ref<RankItem[]>([])
const issueTop10 = ref<IssueTop10[]>([])
const trend30d = ref<DailyTrend[]>([])
const excellentDorms = ref<ExcellentDorm[]>([])
const rectificationDorms = ref<LatestRecord[]>([])
// const scoreDist = ref<ScoreDistribution>({ excellent: '0%', good: '0%', pass: '0%', fail: '0%' })

// --- Filter State ---
const filter = ref({
  campus: '',
  building: '',
  floor: '',
  room: ''
})
const allDorms = ref<string[]>([]) // "North 5,3,301"
const buildingOptions = ref<string[]>([])
const floorOptions = ref<number[]>([])
const roomOptions = ref<string[]>([])
const queryScore = ref<number | null>(null)
const queryIssues = ref<string>('')

// --- Rank Filter State ---
const rankFilter = ref({
  campus: '',
  building: '',
  floor: '',
  room: ''
})
const rankBuildingOptions = ref<string[]>([])
const rankFloorOptions = ref<number[]>([])
const rankRoomOptions = ref<string[]>([])

// --- Refs for ECharts ---
const areaChartRef = ref<HTMLElement | null>(null)
const buildingChartRef = ref<HTMLElement | null>(null)
const issueChartRef = ref<HTMLElement | null>(null)
// const trendChartRef = ref<HTMLElement | null>(null)
// const distChartRef = ref<HTMLElement | null>(null)
const tableScrollRef = ref<HTMLElement | null>(null)

let areaChart: echarts.ECharts | null = null
let buildingChart: echarts.ECharts | null = null
let issueChart: echarts.ECharts | null = null
// let trendChart: echarts.ECharts | null = null
// let distChart: echarts.ECharts | null = null

// --- Methods ---

// Time Update
const updateTime = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const week = weekDays[now.getDay()]
  currentTime.value = `${year}-${month}-${day} ${hours}:${minutes}:${seconds} ${week}`
}

// Data Fetching
const fetchData = async () => {
  try {
    const [sumRes, areaRes, recRes, issRes, trendRes, rectRes, excellentRes, structRes] = await Promise.all([
      axios.get('/api/big-screen/summary'),
      axios.get('/api/big-screen/area-compare'),
      axios.get('/api/big-screen/latest-records'),
      axios.get('/api/big-screen/issue-top10'),
      axios.get('/api/big-screen/trend-30d'),
      axios.get('/api/big-screen/rectification-dorms'),
      axios.get('/api/big-screen/excellent-dorms'),
      axios.get('/api/big-screen/structure')
    ])

    summary.value = sumRes.data
    areaData.value = areaRes.data
    latestRecords.value = recRes.data
    issueTop10.value = issRes.data
    trend30d.value = trendRes.data
    rectificationDorms.value = rectRes.data
    excellentDorms.value = excellentRes.data
    allDorms.value = structRes.data
    
    // Initial fetch for rank data
    fetchRankData()

    updateCharts()
  } catch (error) {
    console.error('Failed to fetch big screen data:', error)
  }
}

// --- Filter Logic ---






























































































































































































































































































// Watchers for cascading filters
watch(() => filter.value.campus, () => {
  filter.value.building = ''
  filter.value.floor = ''
  filter.value.room = ''
  updateBuildingOptions()
  fetchQueryScore()
})

watch(() => filter.value.building, (newVal) => {
  filter.value.floor = ''
  filter.value.room = ''
  if (newVal) {
    updateFloorOptions()
  } else {
    floorOptions.value = []
  }
  fetchQueryScore()
})

watch(() => filter.value.floor, (newVal) => {
  filter.value.room = ''
  if (newVal) {
    updateRoomOptions()
  } else {
    roomOptions.value = []
  }
  fetchQueryScore()
})

watch(() => filter.value.room, () => {
  fetchQueryScore()
})

const updateBuildingOptions = () => {
  if (!filter.value.campus) {
    buildingOptions.value = []
    return
  }
  // Filter buildings starting with North/South based on campus selection
  // Assuming 'North' -> '北%' or 'A%', 'South' -> '南%' or 'B%'
  // Or simply parse allDorms: "BuildingName,Floor,Room"
  const buildings = new Set<string>()
  allDorms.value.forEach(s => {
    const parts = s.split(',')
    const b = (parts[0] || '').trim()
    if (filter.value.campus === 'North' && (b.startsWith('北') || b.startsWith('A'))) buildings.add(b)
    else if (filter.value.campus === 'South' && (b.startsWith('南') || b.startsWith('B'))) buildings.add(b)
  })
  buildingOptions.value = Array.from(buildings).sort()
}

const updateFloorOptions = () => {
  if (!filter.value.building) {
    floorOptions.value = []
    return
  }
  const floors = new Set<number>()
  allDorms.value.forEach(s => {
    const parts = s.split(',')
    if (parts.length >= 2) {
      const b = (parts[0] || '').trim()
      const f = (parts[1] || '').trim()
      if (b === filter.value.building && f) {
        const floorNum = parseInt(f)
        if (!isNaN(floorNum)) floors.add(floorNum)
      }
    }
  })
  floorOptions.value = Array.from(floors).sort((a, b) => a - b)
}

const updateRoomOptions = () => {
  if (!filter.value.floor) {
    roomOptions.value = []
    return
  }
  const rooms = new Set<string>()
  allDorms.value.forEach(s => {
    const parts = s.split(',')
    if (parts.length >= 3) {
      const b = (parts[0] || '').trim()
      const f = (parts[1] || '').trim()
      const r = (parts[2] || '').trim()
      if (b === filter.value.building && parseInt(f) === parseInt(filter.value.floor as any)) {
         rooms.add(r)
      }
    }
  })
  roomOptions.value = Array.from(rooms).sort()
}

const fetchQueryScore = async () => {
  if (!filter.value.campus) {
    queryScore.value = null
    queryIssues.value = ''
    nextTick(() => {
        if (areaChartRef.value && !areaChart) {
             areaChart = echarts.init(areaChartRef.value)
             updateCharts()
        } else if (areaChart) {
            updateCharts()
        }
    })
    return
  }
  
  try {
    const res = await axios.get('/api/big-screen/query-score', {
      params: {
        building: filter.value.building || undefined,
        floor: filter.value.floor || undefined,
        room: filter.value.room || undefined
      }
    })
    
    if (typeof res.data === 'object' && res.data !== null && 'score' in res.data) {
       // Detailed result with issues
       queryScore.value = res.data.score
       queryIssues.value = res.data.issues || ''
    } else {
       // Just average score
       const val = Number(res.data)
       queryScore.value = isNaN(val) ? null : val
       queryIssues.value = ''
    }
  } catch (error) {
    console.error('Failed to query score', error)
    queryScore.value = null
    queryIssues.value = ''
  }
}

const getResultLabel = () => {
  if (filter.value.room) return `${filter.value.building}-${filter.value.room}`
  if (filter.value.floor) return `${filter.value.building} ${filter.value.floor}层平均分`
  if (filter.value.building) return `${filter.value.building} 平均分`
  return `${filter.value.campus === 'North' ? '北区' : '南区'} 平均分`
}

const formatScore = (val: number | null | undefined) => {
  if (val === null || val === undefined || isNaN(Number(val))) return '--'
  return Number(val).toFixed(1)
}

// --- Rank Filter Logic ---
watch(() => rankFilter.value.campus, () => {
  rankFilter.value.building = ''
  rankFilter.value.floor = ''
  rankFilter.value.room = ''
  updateRankBuildingOptions()
  fetchRankData()
})

watch(() => rankFilter.value.building, (newVal) => {
  rankFilter.value.floor = ''
  rankFilter.value.room = ''
  if (newVal) {
    updateRankFloorOptions()
  } else {
    rankFloorOptions.value = []
  }
  fetchRankData()
})

watch(() => rankFilter.value.floor, (newVal) => {
  rankFilter.value.room = ''
  if (newVal) {
    updateRankRoomOptions()
  } else {
    rankRoomOptions.value = []
  }
  fetchRankData()
})

watch(() => rankFilter.value.room, () => {
  fetchRankData()
})

const updateRankBuildingOptions = () => {
  if (!rankFilter.value.campus) {
    rankBuildingOptions.value = []
    return
  }
  const buildings = new Set<string>()
  allDorms.value.forEach(s => {
    const parts = s.split(',')
    const b = (parts[0] || '').trim()
    if (rankFilter.value.campus === 'North' && (b.startsWith('北') || b.startsWith('A'))) buildings.add(b)
    else if (rankFilter.value.campus === 'South' && (b.startsWith('南') || b.startsWith('B'))) buildings.add(b)
  })
  rankBuildingOptions.value = Array.from(buildings).sort()
}

const updateRankFloorOptions = () => {
  if (!rankFilter.value.building) {
    rankFloorOptions.value = []
    return
  }
  const floors = new Set<number>()
  allDorms.value.forEach(s => {
    const parts = s.split(',')
    if (parts.length >= 2) {
      const b = (parts[0] || '').trim()
      const f = (parts[1] || '').trim()
      if (b === rankFilter.value.building && f) {
        const floorNum = parseInt(f)
        if (!isNaN(floorNum)) floors.add(floorNum)
      }
    }
  })
  rankFloorOptions.value = Array.from(floors).sort((a, b) => a - b)
}

const updateRankRoomOptions = () => {
  if (!rankFilter.value.floor) {
    rankRoomOptions.value = []
    return
  }
  const rooms = new Set<string>()
  allDorms.value.forEach(s => {
    const parts = s.split(',')
    if (parts.length >= 3) {
      const b = (parts[0] || '').trim()
      const f = (parts[1] || '').trim()
      const r = (parts[2] || '').trim()
      if (b === rankFilter.value.building && parseInt(f) === parseInt(rankFilter.value.floor as any)) {
         rooms.add(r)
      }
    }
  })
  rankRoomOptions.value = Array.from(rooms).sort()
}

const fetchRankData = async () => {
  try {
    const res = await axios.get('/api/big-screen/rank-data', {
      params: {
        campus: rankFilter.value.campus || undefined,
        building: rankFilter.value.building || undefined,
        floor: rankFilter.value.floor || undefined,
        room: rankFilter.value.room || undefined
      }
    })
    buildingRank.value = res.data.data
    
    nextTick(() => {
        if (buildingChartRef.value && !buildingChart) {
             buildingChart = echarts.init(buildingChartRef.value)
             updateCharts()
        } else if (buildingChart) {
            updateCharts()
        }
    })
  } catch (error) {
    console.error('Failed to fetch rank data', error)
  }
}

// Chart Options & Update
const commonChartConfig = {
  textStyle: { fontFamily: 'Microsoft YaHei' },
  backgroundColor: 'transparent',
  animation: true,
  animationDuration: 1500,
  animationEasing: 'cubicOut' as any
}

const updateCharts = () => {
  if (!areaChart || !buildingChart || !issueChart) return

  // 1. Area Compare (Gauge/Pie)
  areaChart.setOption({
    ...commonChartConfig,
    title: { show: false },
    tooltip: { trigger: 'item' },
    legend: { bottom: '0%', textStyle: { color: '#fff' } },
    series: [
      {
        name: '区域平均分',
        type: 'pie',
        radius: ['50%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        label: { show: false, position: 'center' },
        emphasis: {
          label: { show: true, fontSize: '20', fontWeight: 'bold', color: '#fff' }
        },
        labelLine: { show: false },
        data: [
          { value: areaData.value.northAvg, name: '北区' },
          { value: areaData.value.southAvg, name: '南区' }
        ],
        itemStyle: {
          borderRadius: 5,
          borderColor: '#0a1128',
          borderWidth: 2
        },
        color: ['#00e5ff', '#ffeb3b']
      }
    ]
  })

  // 2. Building Rank (Bar)
  buildingChart.setOption({
    ...commonChartConfig,
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { top: '25%', left: '3%', right: '4%', bottom: '5%', containLabel: true },
    xAxis: {
      type: 'category',
      data: buildingRank.value.map(item => item.name),
      axisLine: { lineStyle: { color: '#4c9bfd' } },
      axisLabel: { color: '#fff', rotate: 30, interval: 0 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#fff' },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } }
    },
    series: [{
      name: '平均分',
      type: 'bar',
      barWidth: '40%',
      data: buildingRank.value.map(item => item.value),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#00e5ff' },
          { offset: 1, color: '#005bea' }
        ]),
        borderRadius: [4, 4, 0, 0]
      }
    }]
  })

  // 3. Issue Top 10 (Horizontal Bar)
  issueChart.setOption({
    ...commonChartConfig,
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { top: '5%', left: '3%', right: '10%', bottom: '5%', containLabel: true },
    xAxis: {
      type: 'value',
      show: false
    },
    yAxis: {
      type: 'category',
      data: issueTop10.value.map(item => item.issue).reverse(), // Top items at top
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#fff', width: 90, overflow: 'truncate' }
    },
    series: [{
      name: '次数',
      type: 'bar',
      barWidth: '50%',
      data: issueTop10.value.map(item => item.count).reverse(),
      label: { show: true, position: 'right', color: '#fff' },
      itemStyle: {
        color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
          { offset: 0, color: '#ffeb3b' },
          { offset: 1, color: '#f44336' }
        ]),
        borderRadius: [0, 4, 4, 0]
      }
    }]
  })

  // 4. Trend 30d (Line) - Removed
  // trendChart.setOption({...})
}

const getScoreClass = (score: number) => {
  if (score >= 90) return 'text-green'
  if (score >= 80) return 'text-blue'
  if (score >= 60) return 'text-yellow'
  return 'text-red'
}

// Auto Scroll Table
const startTableScroll = () => {
  if (!tableScrollRef.value) return
  const el = tableScrollRef.value
  
  // Simple auto-scroll logic
  const step = 1
  const interval = 50
  
  scrollTimer.value = window.setInterval(() => {
    if (!el) return
    // Check if we are at the bottom
    // Use a small buffer (e.g. 1px) to handle fractional pixels
    if (el.scrollTop + el.clientHeight >= el.scrollHeight - 1) {
       el.scrollTop = 0
    } else {
       el.scrollTop += step
    }
  }, interval)
}

const stopScroll = () => {
  if (scrollTimer.value) {
    clearInterval(scrollTimer.value)
    scrollTimer.value = null
  }
}

// Lifecycle
onMounted(() => {
  updateTime()
  timer.value = window.setInterval(updateTime, 1000)
  
  fetchData()
  refreshTimer.value = window.setInterval(fetchData, 30 * 1000) // 30 sec refresh
  
  startTableScroll()

  nextTick(() => {
    // Init Charts
    if (areaChartRef.value) areaChart = echarts.init(areaChartRef.value)
    if (buildingChartRef.value) buildingChart = echarts.init(buildingChartRef.value)
    if (issueChartRef.value) issueChart = echarts.init(issueChartRef.value)
    // if (distChartRef.value) distChart = echarts.init(distChartRef.value)
    
    window.addEventListener('resize', handleResize)
  })
})

const handleResize = () => {
  areaChart?.resize()
  buildingChart?.resize()
  issueChart?.resize()
  // distChart?.resize()
}

onUnmounted(() => {
  if (timer.value) clearInterval(timer.value)
  if (refreshTimer.value) clearInterval(refreshTimer.value)
  if (scrollTimer.value) clearInterval(scrollTimer.value)
  window.removeEventListener('resize', handleResize)
  
  areaChart?.dispose()
  buildingChart?.dispose()
  issueChart?.dispose()
  // distChart?.dispose()
})
</script>

<style scoped>
/* Query Panel Styles */
.query-panel {
  display: flex;
  flex-direction: column;
}

.query-filter {
  padding: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  z-index: 10;
}

.query-select {
  background: rgba(0, 229, 255, 0.1);
  border: 1px solid rgba(0, 229, 255, 0.3);
  color: #00e5ff;
  padding: 2px 5px;
  border-radius: 4px;
  font-size: 1.4vh;
  outline: none;
  flex: 1;
  min-width: 0;
}

.query-select:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.query-select option {
  background: #0a1128;
  color: #fff;
}

.query-result {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.result-score {
  text-align: center;
}

.score-label {
  font-size: 2vh;
  color: #aaa;
  margin-bottom: 1vh;
}

.score-main {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 5px;
  margin-bottom: 1vh;
}

.score-value {
  font-size: 6vh;
  font-weight: bold;
  text-shadow: 0 0 20px currentColor;
  line-height: 1;
}

.score-unit {
  font-size: 2vh;
  color: #aaa;
}

.score-issues {
  margin-top: 0.5vh;
  text-align: center;
  background: rgba(255, 235, 59, 0.1);
  padding: 4px 10px;
  border-radius: 15px;
  border: 1px solid rgba(255, 235, 59, 0.3);
  display: inline-block;
  max-width: 90%;
}

.issue-label {
  font-size: 1.4vh;
  color: #ffeb3b;
  margin-right: 5px;
  font-weight: bold;
}

.issue-content {
  font-size: 1.4vh;
  color: #fff;
}

/* Base Layout */
.big-screen-container {
  width: 100vw;
  height: 100vh;
  background-color: #0a1128;
  background-image: radial-gradient(circle at 50% 50%, #131d3d 0%, #0a1128 100%);
  overflow: hidden;
  color: #fff;
  display: flex;
  flex-direction: column;
  font-family: 'Microsoft YaHei', sans-serif;
}

/* Header */
.header {
  height: 8vh;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2vw;
  background: linear-gradient(to bottom, rgba(10, 17, 40, 0.9), rgba(10, 17, 40, 0));
  border-bottom: 1px solid rgba(0, 229, 255, 0.2);
  position: relative;
}

.header-left, .header-right {
  width: 25%;
  display: flex;
  align-items: center;
}

.header-right {
  justify-content: flex-end;
  gap: 10px;
  font-size: 1.5vh;
  color: #00e5ff;
}

.status-dot {
  width: 8px;
  height: 8px;
  background-color: #76ff03;
  border-radius: 50%;
  box-shadow: 0 0 5px #76ff03;
}

.time {
  font-size: 2.2vh;
  color: #00e5ff;
  font-weight: bold;
  text-shadow: 0 0 5px rgba(0, 229, 255, 0.5);
}

.header-center {
  flex: 1;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.header-center h1 {
  margin: 0;
  font-size: 3.5vh;
  letter-spacing: 0.2em;
  background: linear-gradient(to right, #00e5ff, #ffffff, #00e5ff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  font-weight: bold;
}

.title-decoration {
  width: 40%;
  height: 2px;
  background: linear-gradient(to right, transparent, #00e5ff, transparent);
  margin-top: 5px;
}

/* Main Content */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 1vh 1vw;
  gap: 1.5vh; /* Gap between rows */
  box-sizing: border-box;
}

/* Common Panel Styles */
.panel {
  background: rgba(19, 29, 61, 0.4);
  border: 1px solid rgba(0, 229, 255, 0.1);
  position: relative;
  display: flex;
  flex-direction: column;
  box-shadow: inset 0 0 20px rgba(0, 229, 255, 0.05);
  height: 100%; /* Ensure panel fits grid cell */
  overflow: hidden; /* Prevent content overflow */
  min-height: 0; /* Prevent flex/grid item expansion */
}

.panel-header {
  height: 4vh;
  line-height: 4vh;
  padding-left: 1vw;
  font-size: 1.8vh;
  color: #fff;
  font-weight: bold;
  background: linear-gradient(90deg, rgba(0, 229, 255, 0.1) 0%, transparent 100%);
  border-left: 3px solid #00e5ff;
}

.panel-body {
  flex: 1;
  position: relative;
  overflow: hidden;
}

/* Corner Decorations */
.panel-corner {
  position: absolute;
  width: 10px;
  height: 10px;
  border: 2px solid #00e5ff;
  transition: all 0.3s;
}
.corner-tl { top: -1px; left: -1px; border-right: none; border-bottom: none; }
.corner-tr { top: -1px; right: -1px; border-left: none; border-bottom: none; }
.corner-bl { bottom: -1px; left: -1px; border-right: none; border-top: none; }
.corner-br { bottom: -1px; right: -1px; border-left: none; border-top: none; }

/* Section Cards (Row 1) */
.section-cards {
  height: 12vh;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1vw;
}

.data-card {
  background: rgba(19, 29, 61, 0.6);
  display: flex;
  align-items: center;
  padding: 0 1.5vw;
  position: relative;
  border: 1px solid rgba(0, 229, 255, 0.2);
}

.card-icon {
  width: 6vh;
  height: 6vh;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 3vh;
  margin-right: 1vw;
  background: rgba(255, 255, 255, 0.1);
}
.card-icon.blue { color: #00e5ff; box-shadow: 0 0 10px rgba(0, 229, 255, 0.3); }
.card-icon.green { color: #76ff03; box-shadow: 0 0 10px rgba(118, 255, 3, 0.3); }
.card-icon.orange { color: #ffeb3b; box-shadow: 0 0 10px rgba(255, 235, 59, 0.3); }
.card-icon.red { color: #f44336; box-shadow: 0 0 10px rgba(244, 67, 54, 0.3); }

.card-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.card-info .label {
  font-size: 1.6vh;
  color: #aaa;
  margin-bottom: 0.5vh;
}

.card-info .value {
  font-size: 3.5vh;
  font-weight: bold;
  font-family: 'Impact', sans-serif;
  color: #fff;
  line-height: 1;
}

.card-info .value.text-sm {
  font-size: 2vh;
  font-family: 'Microsoft YaHei', sans-serif;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 12vw;
}

.card-decoration {
  position: absolute;
  right: 0;
  top: 0;
  width: 20px;
  height: 20px;
  background: linear-gradient(135deg, transparent 50%, rgba(0, 229, 255, 0.5) 50%);
}

/* Section Middle (Row 2) */
.section-middle {
  height: 32vh;
  display: grid;
  grid-template-columns: 3fr 7fr;
  gap: 1vw;
  z-index: 5; /* Prevent overlap with bottom section */
  position: relative;
}

/* Section Bottom (Row 3) */
.section-bottom {
  flex: 1; /* Fills remaining space */
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr;
  gap: 1vw;
  min-height: 0; /* Important for flex child to not overflow */
}

/* Chart Containers */
.chart-container {
  width: 100%;
  height: 100%;
}

/* Custom Table Styles */
.table-body {
  padding: 1vh;
  padding-right: 0.5vh; /* Adjust right padding to balance with scrollbar */
  display: flex;
  flex-direction: column;
  overflow: hidden; /* Ensure content doesn't spill out */
  min-height: 0; /* CRITICAL: Allows flex child to shrink below content size */
}

.table-header-row {
  display: flex;
  background: rgba(0, 50, 150, 0.4); /* Darker blue background like image */
  padding: 1vh 0;
  padding-right: 11px; /* Compensate for scrollbar (6px) + padding (5px) */
  font-weight: bold;
  color: #00e5ff;
  font-size: max(1.6vh, 12px); 
  flex-shrink: 0; 
}

.table-content-wrapper {
  flex: 1;
  overflow: hidden; 
  position: relative;
  margin-top: 0; /* Remove gap to look more integrated */
  min-height: 0; /* CRITICAL: Allows flex child to shrink below content size */
}

.table-scroll-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 5px; /* Prevent content from being covered by scrollbar */
}

/* Custom Scrollbar Styles */
.table-scroll-container::-webkit-scrollbar {
  width: 6px;
  display: block;
}

.table-scroll-container::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 3px;
}

.table-scroll-container::-webkit-scrollbar-thumb {
  background: rgba(0, 229, 255, 0.3);
  border-radius: 3px;
  transition: background 0.3s;
}

.table-scroll-container::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 229, 255, 0.6);
}

/* Firefox Scrollbar */
.table-scroll-container {
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 229, 255, 0.3) rgba(255, 255, 255, 0.05);
}

.table-row {
  display: flex;
  padding: 1vh 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  font-size: max(1.5vh, 12px);
  color: #ddd;
  transition: background 0.3s;
  align-items: center; /* Vertically center content */
}

.table-row:hover {
  background: rgba(0, 229, 255, 0.1);
}

.row-alt {
  background: rgba(255, 255, 255, 0.02);
}

.col-dorm { flex: 1.5; text-align: center; }
.col-score { flex: 1; text-align: center; font-weight: bold; }
.col-inspector { flex: 1.2; text-align: center; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.col-header-issue { flex: 2.5; text-align: center; }
.col-issue-wrapper { 
  flex: 2.5; 
  padding: 0 10px;
  overflow: hidden;
}
.col-issue { 
  display: -webkit-box;
  -webkit-line-clamp: 2; /* Limit to 2 lines */
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: normal; /* Allow wrapping */
  line-height: 1.2;
}
.col-time { flex: 1.8; text-align: center; color: #aaa; font-size: 0.9em; }

.text-green { color: #76ff03; }
.text-blue { color: #00e5ff; }
.text-yellow { color: #ffeb3b; }
.text-red { color: #f44336; }

/* Responsive Adjustments */
@media screen and (max-height: 800px) {
  .header h1 { font-size: 3vh; }
  .card-info .value { font-size: 3vh; }
}

.col-xl-dorm { flex: 2; text-align: center; }
.col-xl-score { flex: 1; text-align: center; font-weight: bold; }
.col-xl-time { flex: 2; text-align: center; color: #aaa; font-size: 0.9em; }

/* Rank Filter Styles */
.rank-filter {
  display: flex;
  gap: 2px;
  margin-bottom: 5px;
  justify-content: space-between;
  padding: 0 5px;
  z-index: 10;
  position: relative;
}

.rank-filter .query-select-el {
  width: 24%;
}

.rank-filter .query-select-el :deep(.el-select__wrapper) {
  background-color: rgba(10, 17, 40, 0.8) !important;
  box-shadow: 0 0 0 1px rgba(0, 229, 255, 0.3) inset !important;
  padding: 2px 8px;
  min-height: 24px;
  height: 24px;
  font-size: 1.2vh;
}

.rank-filter .query-select-el :deep(.el-select__placeholder),
.rank-filter .query-select-el :deep(.el-select__selected-item) {
  color: #fff !important;
  font-size: 1.2vh;
  line-height: 24px;
}

.rank-filter .query-select-el :deep(.el-select__suffix) {
  color: #00e5ff;
}

.rank-chart {
  flex: 1;
  min-height: 0;
}
</style>

<style>
/* Global styles for the popper since it's teleported */
.big-screen-select-popper.el-popper {
  background: rgba(10, 17, 40, 0.95) !important;
  border: 1px solid rgba(0, 229, 255, 0.3) !important;
}

.big-screen-select-popper .el-select-dropdown__item {
  color: #fff;
}

.big-screen-select-popper .el-select-dropdown__item.is-hovering,
.big-screen-select-popper .el-select-dropdown__item:hover {
  background-color: rgba(0, 229, 255, 0.2);
}

.big-screen-select-popper .el-select-dropdown__item.is-selected {
  color: #00e5ff;
  font-weight: bold;
  background-color: rgba(0, 229, 255, 0.1);
}
</style>
