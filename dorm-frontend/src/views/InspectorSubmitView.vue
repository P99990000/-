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
                <div class="item-label">
                  <span class="item-name">{{ entry.item.itemName }}</span>
                  <span class="item-desc">（{{ entry.item.maxScore }}分项）</span>
                </div>
                <div class="item-input">
                  <el-input 
                    v-model="entry.form.remark" 
                    placeholder="无问题则留空，有问题请在此填写具体原因..." 
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
            <div class="score-wrapper">
              <span class="score-label">最终得分</span>
              <el-input-number 
                v-model="totalScore" 
                :min="0" 
                :max="100" 
                size="large"
                class="score-input"
              />
            </div>
            
            <div class="action-buttons">
              <div class="notice-wrapper">
                <span class="notice-label">⚠️ 通报批评</span>
                <el-switch 
                  v-model="isNotice" 
                  inline-prompt 
                  active-text="是" 
                  inactive-text="否"
                  style="--el-switch-on-color: #ff4949;"
                />
              </div>
              <div class="rectify-wrapper">
                <span class="rectify-label">🔧 需要整改</span>
                <el-switch 
                  v-model="isNeedRectification" 
                  inline-prompt 
                  active-text="是" 
                  inactive-text="否"
                  style="--el-switch-on-color: #e6a23c;"
                />
              </div>
              <el-button type="primary" size="large" class="submit-btn" @click="submitRecord" :loading="submitting">
                提交记录
              </el-button>
              <el-button size="large" class="reset-btn" @click="resetForm">重置</el-button>
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
import { ref, computed, onMounted } from 'vue'
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
  score: number
  remark: string
}

interface ItemWithForm {
  item: InspectionItem
  form: ItemDetailForm
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
const totalScore = ref(100)
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
      score: 0, // No longer used for calculation
      remark: ''
    }
  }))
}

// 重置表单
const resetItemScores = () => {
  itemsWithForm.value.forEach(entry => {
    entry.form.remark = ''
  })
  totalScore.value = 100
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
  
  try {
    const res = await axios.get(`/api/dormitories/by-building/${encodeURIComponent(selectedBuilding.value)}`)
    if (res.data.code === 200) {
      currentBuildingDorms.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('加载该楼栋宿舍失败')
  }
}

const handleFloorChange = () => {
  selectedRoom.value = ''
  selectedDormId.value = undefined
}

const handleRoomChange = () => {
  if (selectedDormId.value) {
    // 可以在这里重新加载检查项，或者重置分数
    resetItemScores()
  }
}

const resetForm = () => {
  resetItemScores()
  ElMessage.info('表单已重置')
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

const submitRecord = async () => {
  if (!selectedDormId.value) {
    ElMessage.warning('请选择宿舍')
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
      score: entry.form.score,
      remark: entry.form.remark
    }))

    const payload = {
      dormId: selectedDormId.value,
      inspectorName: inspectorName.value.trim(),
      isNotice: isNotice.value,
      isNeedRectification: isNeedRectification.value,
      totalScore: totalScore.value,
      imageUrl: imageUrl.value,
      remark: '日常检查',
      details: details.map(d => ({
        itemId: d.itemId,
        score: d.score,
        deductionReason: d.remark // Map remark to deductionReason
      }))
    }

    const res = await axios.post('/api/records/submit', payload)
    if (res.data.code === 200) {
      ElMessage.success('提交成功！')
      selectedDormId.value = undefined
      isNotice.value = false
      resetItemScores()
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
  padding: 20px;
  display: flex;
  justify-content: center;
}

.content-container {
  width: 100%;
  max-width: 1000px;
}

/* Header */
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

/* Main Card */
.main-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06) !important;
}

.filter-section {
  padding: 0 10px;
}

.filter-item {
  margin-bottom: 16px;
  width: 100%;
}

.full-width {
  width: 100%;
}

.custom-divider {
  margin: 24px 0;
}

/* Upload Section */
.upload-section {
  text-align: center;
  margin-bottom: 24px;
  border: 1px dashed #dcdfe6;
  padding: 20px;
  border-radius: 8px;
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

/* Items List */
.inspection-items-container {
  margin-bottom: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 16px;
  padding-left: 8px;
  border-left: 4px solid #409eff;
}

.items-list {
  background-color: #f9fafb;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e5e7eb;
}

.item-row {
  display: flex;
  align-items: flex-start;
  padding: 12px 0;
  border-bottom: 1px solid #e5e7eb;
}

.item-row:last-child {
  border-bottom: none;
}

.item-label {
  width: 140px;
  flex-shrink: 0;
  padding-top: 8px;
}

.item-name {
  font-weight: 500;
  color: #374151;
  display: block;
}

.item-desc {
  font-size: 12px;
  color: #9ca3af;
}

.item-input {
  flex-grow: 1;
}

/* Form Actions */
.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background-color: #fff;
  border-top: 1px solid #f3f4f6;
  border-radius: 0 0 12px 12px;
}

.sticky-bottom {
  position: sticky;
  bottom: 0;
  z-index: 10;
  box-shadow: 0 -4px 6px -1px rgba(0, 0, 0, 0.05);
}

.score-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.score-label {
  font-size: 16px;
  font-weight: 600;
  color: #374151;
}

.score-input {
  width: 140px;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.notice-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background-color: #fef2f2;
  border-radius: 6px;
  position: relative;
  z-index: 1;
}

.rectify-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background-color: #fdf6ec;
  border-radius: 6px;
  position: relative;
  z-index: 1;
}

.notice-label {
  font-size: 14px;
  color: #ef4444;
  font-weight: 500;
}

.rectify-label {
  font-size: 14px;
  color: #e6a23c;
  font-weight: 500;
}

.submit-btn {
  min-width: 120px;
  font-weight: 600;
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
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Responsive */
@media (max-width: 768px) {
  .item-row {
    flex-direction: column;
  }
  
  .item-label {
    width: 100%;
    margin-bottom: 8px;
    padding-top: 0;
  }
  
  .form-actions {
    flex-direction: column;
    gap: 16px;
  }
  
  .score-wrapper {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
