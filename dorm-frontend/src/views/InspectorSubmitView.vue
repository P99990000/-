<template>
  <div class="inspector-submit-view">
    <div class="content-container">
      <!-- Header Section -->
      <div class="page-header">
        <div class="header-content">
          <div class="header-icon">📝</div>
          <h2>宿舍卫生检查录入</h2>
        </div>
        <p class="header-subtitle">请选择宿舍并进行评分</p>
      </div>

      <el-card class="main-card" shadow="never">
        <!-- 筛选区域 -->
        <div class="filter-section">
          <el-form :inline="true" class="filter-form">
            <el-row :gutter="10">
              <el-col :xs="12" :sm="6" :md="4">
                <el-form-item label="院区" class="filter-item">
                  <el-select v-model="selectedCampus" placeholder="选择院区" @change="handleCampusChange" class="full-width">
                    <el-option v-for="c in campusOptions" :key="c" :label="c" :value="c" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="12" :sm="6" :md="4">
                <el-form-item label="楼栋" class="filter-item">
                  <el-select 
                    v-model="selectedBuilding" 
                    placeholder="选择楼栋" 
                    :disabled="!selectedCampus"
                    @change="handleBuildingChange" 
                    class="full-width"
                  >
                    <el-option v-for="b in buildingOptions" :key="b" :label="b" :value="b" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="12" :sm="6" :md="4">
                <el-form-item label="楼层" class="filter-item">
                  <el-select 
                    v-model="selectedFloor" 
                    placeholder="选择楼层" 
                    :disabled="!selectedBuilding" 
                    @change="handleFloorChange"
                    class="full-width"
                  >
                    <el-option v-for="f in floorOptions" :key="f" :label="f + '楼'" :value="f" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="12" :sm="6" :md="4">
                <el-form-item label="房间" class="filter-item">
                  <el-select 
                    v-model="selectedDormId" 
                    placeholder="选择房间" 
                    :disabled="!selectedFloor" 
                    class="full-width"
                    @change="handleRoomChange"
                  >
                    <el-option 
                      v-for="d in roomOptions" 
                      :key="d.id" 
                      :label="d.roomNumber" 
                      :value="d.id" 
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="检查人" class="filter-item">
                  <el-input 
                    v-model="inspectorName" 
                    placeholder="您的姓名" 
                    class="full-width"
                  >
                    <template #prefix>👤</template>
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>

        <el-divider class="custom-divider" />

        <!-- 打分区域 -->
        <div v-if="selectedDormId" class="inspection-form animate-fade-in">
          <!-- 照片上传 -->
          <div class="upload-section">
              <h3 class="section-title">现场照片</h3>
              <el-upload
                class="upload-demo"
                action="/api/common/upload"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="handleUploadSuccess"
                :before-upload="beforeUpload"
              >
                <img v-if="imageUrl" :src="imageUrl" class="uploaded-image" />
                <el-icon v-else class="uploader-icon"><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">点击上传现场照片 (可选)</div>
          </div>

          <div class="inspection-items-container">
            <h3 class="section-title">检查明细</h3>
            <div class="items-list">
              <div v-for="entry in itemsWithForm" :key="entry.item.id" class="item-row">
                <div class="item-header-mobile">
                    <span class="item-name">{{ entry.item.itemName }}</span>
                    <span class="item-desc">（{{ entry.item.maxScore }}分项）</span>
                </div>
                
                <div class="item-content">
                    <div class="quick-deduct-buttons">
                        <button 
                          class="deduct-btn" 
                          :class="{ active: entry.form.deduction === 0 }"
                          @click="handleDeduct(entry, 0)"
                        >
                          满分
                        </button>
                        <button 
                          class="deduct-btn" 
                          :class="{ active: entry.form.deduction === 2 }"
                          @click="handleDeduct(entry, 2)"
                        >
                          -2
                        </button>
                        <button 
                          class="deduct-btn" 
                          :class="{ active: entry.form.deduction === 5 }"
                          @click="handleDeduct(entry, 5)"
                        >
                          -5
                        </button>
                        <button 
                          class="deduct-btn" 
                          :class="{ active: entry.form.deduction === 10 }"
                          @click="handleDeduct(entry, 10)"
                        >
                          -10
                        </button>
                    </div>

                    <el-input 
                        v-model="entry.form.remark" 
                        placeholder="备注原因..." 
                        type="textarea"
                        :rows="1"
                        resize="none"
                        class="custom-textarea"
                    />
                </div>
              </div>
            </div>
          </div>

          <div class="form-actions sticky-bottom">
            <div class="score-display">
              <span class="score-label">当前总分</span>
              <span class="score-value" :class="{ 'low-score': totalScore < 60 }">{{ totalScore }}</span>
            </div>
            
            <div class="action-buttons">
              <div class="notice-wrapper">
                <span class="notice-label">⚠️ 通报</span>
                <el-switch 
                  v-model="isNotice" 
                  inline-prompt 
                  active-text="是" 
                  inactive-text="否"
                  style="--el-switch-on-color: #ff4949;"
                />
              </div>
              <div class="rectify-wrapper">
                <span class="rectify-label">🔧 整改</span>
                <el-switch 
                  v-model="isNeedRectification" 
                  inline-prompt 
                  active-text="是" 
                  inactive-text="否"
                  style="--el-switch-on-color: #e6a23c;"
                />
              </div>
              <el-button type="primary" size="large" class="submit-btn" @click="submitRecord" :loading="submitting">
                提交并下一间
              </el-button>
            </div>
          </div>
        </div>
        
        <div v-else class="empty-state">
          <div class="empty-icon">🏠</div>
          <p>请先完整选择上方的宿舍信息以开始评分</p>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import axios from 'axios'

const userStore = useUserStore()
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

// 接口定义
interface Dormitory {
  id: number
  buildingName: string
  floor: number
  roomNumber: string
}

interface InspectionItem {
  id: number
  itemName: string
  maxScore: number
}

interface ItemDetailForm {
  deduction: number
  remark: string
}

interface ItemWithForm {
  item: InspectionItem
  form: ItemDetailForm
}

// 常量
const STORAGE_KEYS = {
  CAMPUS: 'inspector_campus',
  BUILDING: 'inspector_building',
  FLOOR: 'inspector_floor',
  INSPECTOR: 'inspector_name'
}

// 状态
const buildingsGrouped = ref<Record<string, string[]>>({})
const currentBuildingDorms = ref<Dormitory[]>([])
const inspectionItems = ref<InspectionItem[]>([])
const itemsWithForm = ref<ItemWithForm[]>([])

const selectedCampus = ref('')
const selectedBuilding = ref('')
const selectedFloor = ref<number | undefined>(undefined)
const selectedRoom = ref('')
const selectedDormId = ref<number | undefined>(undefined)
const inspectorName = ref('')
const isNotice = ref(false)
const isNeedRectification = ref(false)
const imageUrl = ref('')
const submitting = ref(false)

// 计算属性：院区列表
const campusOptions = computed(() => {
  return Object.keys(buildingsGrouped.value)
})

// 计算属性：楼栋列表
const buildingOptions = computed(() => {
  if (!selectedCampus.value) return []
  return buildingsGrouped.value[selectedCampus.value] || []
})

// 计算属性：楼层列表
const floorOptions = computed(() => {
  if (currentBuildingDorms.value.length === 0) return []
  const floors = new Set(currentBuildingDorms.value.map(d => d.floor))
  return Array.from(floors).sort((a, b) => a - b)
})

// 计算属性：房间列表
const roomOptions = computed(() => {
  if (!selectedFloor.value) return []
  return currentBuildingDorms.value
    .filter(d => d.floor === selectedFloor.value)
    .sort((a, b) => a.roomNumber.localeCompare(b.roomNumber))
})

// 计算属性：总分
const totalScore = computed(() => {
  const totalDeduction = itemsWithForm.value.reduce((sum, entry) => sum + entry.form.deduction, 0)
  return Math.max(0, 100 - totalDeduction)
})

// 持久化监听
watch(selectedCampus, (val) => localStorage.setItem(STORAGE_KEYS.CAMPUS, val || ''))
watch(selectedBuilding, (val) => localStorage.setItem(STORAGE_KEYS.BUILDING, val || ''))
watch(selectedFloor, (val) => {
  if (val) localStorage.setItem(STORAGE_KEYS.FLOOR, val.toString())
  else localStorage.removeItem(STORAGE_KEYS.FLOOR)
})
watch(inspectorName, (val) => localStorage.setItem(STORAGE_KEYS.INSPECTOR, val || ''))

// 恢复状态
const restoreState = async () => {
  const savedCampus = localStorage.getItem(STORAGE_KEYS.CAMPUS)
  const savedBuilding = localStorage.getItem(STORAGE_KEYS.BUILDING)
  const savedFloor = localStorage.getItem(STORAGE_KEYS.FLOOR)
  const savedInspector = localStorage.getItem(STORAGE_KEYS.INSPECTOR)

  if (savedInspector) inspectorName.value = savedInspector
  
  if (savedCampus && buildingsGrouped.value[savedCampus]) {
    selectedCampus.value = savedCampus
    
    if (savedBuilding) {
      selectedBuilding.value = savedBuilding
      // 加载楼栋数据
      await loadBuildingDorms(savedBuilding)
      
      if (savedFloor) {
        const floorNum = parseInt(savedFloor)
        if (floorOptions.value.includes(floorNum)) {
          selectedFloor.value = floorNum
        }
      }
    }
  }
}

// 加载楼栋宿舍数据
const loadBuildingDorms = async (building: string) => {
  try {
    const res = await axios.get(`/api/dormitories/by-building/${encodeURIComponent(building)}`)
    if (res.data.code === 200) {
      currentBuildingDorms.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('加载该楼栋宿舍失败')
  }
}

// 初始化数据
const fetchData = async () => {
  try {
    const [buildingsRes, itemsRes] = await Promise.all([
      axios.get('/api/dormitories/buildings'),
      axios.get('/api/inspection-items')
    ])
    
    if (buildingsRes.data.code === 200) {
      buildingsGrouped.value = buildingsRes.data.data
    }
    
    if (itemsRes.data.code === 200) {
      inspectionItems.value = itemsRes.data.data
      initFormItems()
    }

    // 恢复上次选择的状态
    await restoreState()

  } catch (error) {
    ElMessage.error('加载基础数据失败')
    console.error(error)
  }
}

// 初始化表单详情
const initFormItems = () => {
  itemsWithForm.value = inspectionItems.value.map(item => ({
    item: item,
    form: {
      deduction: 0,
      remark: ''
    }
  }))
}

// 重置表单
const resetItemScores = () => {
  itemsWithForm.value.forEach(entry => {
    entry.form.deduction = 0
    entry.form.remark = ''
  })
  imageUrl.value = ''
  isNotice.value = false
  isNeedRectification.value = false
}

// 事件处理
const handleCampusChange = () => {
  selectedBuilding.value = ''
  selectedFloor.value = undefined
  selectedRoom.value = ''
  selectedDormId.value = undefined
  currentBuildingDorms.value = []
}

const handleBuildingChange = async () => {
  selectedFloor.value = undefined
  selectedRoom.value = ''
  selectedDormId.value = undefined
  
  if (!selectedBuilding.value) return
  await loadBuildingDorms(selectedBuilding.value)
}

const handleFloorChange = () => {
  selectedRoom.value = ''
  selectedDormId.value = undefined
}

const handleRoomChange = () => {
  if (selectedDormId.value) {
    resetItemScores()
  }
}

const handleDeduct = (entry: ItemWithForm, amount: number) => {
  entry.form.deduction = amount
}

// 上传处理
const handleUploadSuccess = (response: any) => {
  if (response.code === 200) {
    imageUrl.value = response.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

const beforeUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('上传图片只能是 JPG/PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!')
  }
  return isJPG && isLt2M
}

// 跳转到下一间
const jumpToNextRoom = () => {
  if (!currentBuildingDorms.value || currentBuildingDorms.value.length === 0 || !selectedDormId.value) {
    return false
  }

  const currentIndex = currentBuildingDorms.value.findIndex(d => d.id === selectedDormId.value)
  if (currentIndex === -1 || currentIndex === currentBuildingDorms.value.length - 1) {
    return false
  }

  const nextDorm = currentBuildingDorms.value[currentIndex + 1]
  if (nextDorm) {
    // 自动选中下一间
    selectedFloor.value = nextDorm.floor
    selectedRoom.value = nextDorm.roomNumber
    selectedDormId.value = nextDorm.id
    
    // 滚动到顶部
    window.scrollTo({ top: 0, behavior: 'smooth' })
    
    ElMessage.success(`已切换到下一间：${nextDorm.roomNumber}`)
    return true
  }
  return false
}

const submitRecord = async () => {
  if (!selectedDormId.value) {
    ElMessage.warning('请先选择宿舍')
    return
  }

  if (!inspectorName.value.trim()) {
    ElMessage.warning('请输入检查人姓名')
    return
  }

  submitting.value = true
  
  try {
    // 组装数据
    const details = itemsWithForm.value.map(entry => ({
      itemId: entry.item.id,
      score: entry.form.deduction, // 传递扣分值
      remark: entry.form.remark
    }))

    const payload = {
      dormId: selectedDormId.value,
      inspectorName: inspectorName.value.trim(),
      isNotice: isNotice.value,
      isNeedRectification: isNeedRectification.value,
      totalScore: totalScore.value, // This is already a number from computed property
      imageUrl: imageUrl.value,
      remark: '日常检查',
      details: details.map(d => ({
        itemId: d.itemId,
        score: d.score, // This is number
        deductionReason: d.remark
      }))
    }

    const res = await axios.post('/api/records/submit', payload)
    if (res.data.code === 200) {
      ElMessage.success('提交成功！')
      
      const hasNext = jumpToNextRoom()
      if (!hasNext) {
        ElMessage.info('本层检查结束')
        selectedDormId.value = undefined
        resetItemScores()
      }
    } else {
      ElMessage.error('提交失败：' + res.data.message)
    }
  } catch (error: any) {
    console.error('Submit Error:', error)
    ElMessage.error('提交失败：' + (error.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.inspector-submit-view {
  min-height: 100vh;
  background-color: #f0f2f5;
  padding: 10px;
  display: flex;
  justify-content: center;
}

.content-container {
  width: 100%;
  max-width: 800px;
}

/* Header */
.page-header {
  margin-bottom: 16px;
  text-align: center;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 4px;
}

.header-icon {
  font-size: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #1f2937;
  font-weight: 600;
}

.header-subtitle {
  color: #6b7280;
  margin: 0;
  font-size: 12px;
}

/* Items List */
.inspection-items-container {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 12px;
  padding-left: 8px;
  border-left: 4px solid #409eff;
}

.items-list {
  background-color: #f9fafb;
  border-radius: 8px;
  padding: 10px;
  border: 1px solid #e5e7eb;
}

.item-row {
  display: flex;
  flex-direction: column;
  padding: 12px 0;
  border-bottom: 1px solid #e5e7eb;
}

.item-row:last-child {
  border-bottom: none;
}

.item-header-mobile {
  margin-bottom: 8px;
}

.item-name {
  font-weight: 600;
  color: #374151;
  font-size: 16px;
}

.item-desc {
  font-size: 12px;
  color: #9ca3af;
}

.item-content {
  width: 100%;
}

.quick-deduct-buttons {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  overflow-x: auto;
  padding-bottom: 4px; 
}

.deduct-btn {
  flex: 1;
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 6px;
  padding: 10px 0;
  font-size: 14px;
  color: #4b5563;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 60px;
  font-weight: 500;
}

.deduct-btn:active {
  transform: scale(0.96);
  background: #f3f4f6;
}

.deduct-btn.active {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.3);
}

.sticky-bottom {
  position: sticky;
  bottom: 0;
  background: white;
  padding: 12px 16px;
  box-shadow: 0 -4px 10px rgba(0, 0, 0, 0.08);
  margin: 0 -20px -20px -20px; 
  display: flex;
  flex-direction: column;
  gap: 12px;
  z-index: 100;
  border-top: 1px solid #f0f0f0;
}

.score-display {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 4px;
}

.score-label {
  font-size: 16px;
  font-weight: 600;
  color: #374151;
}

.score-value {
  font-size: 28px;
  font-weight: 700;
  color: #10b981;
}

.score-value.low-score {
  color: #ef4444;
}

.action-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

.submit-btn {
  flex: 2;
  font-weight: 600;
  height: 48px;
  font-size: 16px;
}

.notice-wrapper, .rectify-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  gap: 2px;
  min-width: 50px;
}

.notice-label, .rectify-label {
  font-size: 12px;
  font-weight: 500;
}

.filter-item {
  margin-bottom: 12px;
  width: 100%;
}

.full-width {
  width: 100%;
}

.custom-divider {
  margin: 16px 0;
}

/* Upload Section */
.upload-section {
  text-align: center;
  margin-bottom: 24px;
  border: 1px dashed #dcdfe6;
  padding: 16px;
  border-radius: 8px;
  background: #fafafa;
}

.uploaded-image {
  width: 100%;
  max-width: 300px;
  border-radius: 4px;
}

.uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #9ca3af;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

/* Animations */
.animate-fade-in {
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (min-width: 768px) {
    .inspector-submit-view {
        padding: 20px;
    }

    .item-row {
        flex-direction: row;
        align-items: flex-start;
    }

    .item-header-mobile {
        width: 150px;
        flex-shrink: 0;
        margin-bottom: 0;
        padding-top: 10px;
    }

    .form-actions {
        flex-direction: row;
        justify-content: space-between;
    }

    .score-display {
        gap: 16px;
    }

    .sticky-bottom {
        flex-direction: row;
        align-items: center;
    }
}
</style>
