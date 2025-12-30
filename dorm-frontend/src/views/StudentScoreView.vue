<template>
  <div class="container">
    <div class="header-section">
      <h2>宿舍卫生检查查询</h2>
      
      <!-- 宿舍查询表单 -->
      <div class="search-box">
        <el-form :inline="true" class="demo-form-inline">
          <el-form-item label="楼栋">
            <el-select 
              v-model="selectedBuilding" 
              placeholder="选择楼栋" 
              style="width: 180px"
              clearable
            >
              <el-option
                v-for="building in uniqueBuildings"
                :key="building"
                :label="building"
                :value="building"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="宿舍号">
            <el-input 
              v-model="inputRoomNumber" 
              placeholder="例如: 301" 
              style="width: 150px"
              @keyup.enter="handleSearch"
              clearable
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" :icon="Search">查询</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <div class="dorm-info" v-if="currentDorm">
      当前查询: <strong>{{ currentDorm.buildingName }} {{ currentDorm.roomNumber }}</strong>
    </div>
    
    <el-table 
      v-if="currentDorm && records.length > 0"
      v-loading="loading"
      :data="records" 
      style="width: 100%" 
      border
    >
      <el-table-column type="expand">
        <template #default="props">
          <div class="details-container">
            <h3>扣分明细</h3>
            <el-table :data="props.row.details" border size="small">
              <el-table-column prop="itemId" label="检查项ID" width="100" />
              <el-table-column prop="score" label="得分" width="100">
                <template #default="{ row }">
                  <span :class="{'low-score': row.score < 10}">{{ row.score }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="deductionReason" label="扣分说明">
                <template #default="{ row }">
                  {{ row.deductionReason || '无' }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column label="检查照片" width="120">
        <template #default="{ row }">
          <el-image 
            v-if="row.imageUrl" 
            :src="row.imageUrl" 
            :preview-src-list="[row.imageUrl]"
            fit="cover"
            style="width: 80px; height: 60px; border-radius: 4px;"
          />
          <span v-else>无照片</span>
        </template>
      </el-table-column>

      <el-table-column prop="checkDate" label="检查时间" width="180" />
      <el-table-column prop="inspectorName" label="检查人" width="120" />
      <el-table-column prop="totalScore" label="总分" width="120">
        <template #default="{ row }">
          <el-tag :type="getScoreType(row.totalScore)">{{ row.totalScore }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="整改意见/备注" />
      <el-table-column label="整改状态" width="120">
        <template #default="{ row }">
           <el-tag v-if="row.rectificationStatus === 4" type="danger">待整改</el-tag>
           <el-tag v-else-if="row.rectificationStatus === 1" type="warning">审核中</el-tag>
           <el-tag v-else-if="row.rectificationStatus === 2" type="success">整改通过</el-tag>
           <el-tag v-else-if="row.rectificationStatus === 3" type="danger">审核不通过</el-tag>
           <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button 
            v-if="(row.totalScore < 60 || row.isNotice === 1 || row.rectificationStatus === 4 || row.rectificationStatus === 3) && row.rectificationStatus !== 2 && row.rectificationStatus !== 1"
            type="primary" 
            size="small" 
            @click="openRectificationModal(row)"
          >
            {{ row.rectificationStatus === 3 ? '重新整改' : '提交整改' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-else-if="currentDorm && records.length === 0" description="该宿舍暂无检查记录" />
    <el-empty v-else description="请选择楼栋并输入宿舍号进行查询" />

    <el-dialog v-model="rectDialogVisible" title="提交整改反馈" width="500px">
      <el-form :model="rectForm" label-width="80px">
        <el-form-item label="整改说明">
          <el-input v-model="rectForm.description" type="textarea" rows="3" placeholder="请填写整改情况说明" />
        </el-form-item>
        <el-form-item label="整改照片">
          <el-upload
            class="upload-demo"
            action="/api/common/upload"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleRectUploadSuccess"
            :before-upload="beforeUpload"
          >
            <img v-if="rectForm.imageUrl" :src="rectForm.imageUrl" class="uploaded-image" />
            <el-icon v-else class="uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rectDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRectification" :loading="submittingRect">
            提交
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

interface InspectionDetail {
  id: number
  itemId: number
  score: number
  deductionReason: string | null
  imageUrl: string | null
}

interface InspectionRecord {
  id: number
  dormId: number
  inspectorName: string
  totalScore: number
  checkDate: string
  remark: string | null
  details: InspectionDetail[]
  imageUrl: string | null
  isNotice: number
  rectificationStatus: number // 0:None, 1:Pending, 2:Passed, 3:Rejected
  rectificationDesc: string | null
  rectificationImageUrl: string | null
}

interface Dormitory {
  id: number
  buildingName: string
  floor: number
  roomNumber: string
}

const records = ref<InspectionRecord[]>([])
const dorms = ref<Dormitory[]>([])
const loading = ref(false)

// Search fields
const selectedBuilding = ref('')
const inputRoomNumber = ref('')
const currentDorm = ref<Dormitory | null>(null)

// Rectification Modal
const rectDialogVisible = ref(false)
const submittingRect = ref(false)
const rectForm = reactive({
  recordId: 0,
  description: '',
  imageUrl: ''
})

const uniqueBuildings = computed(() => {
  const buildings = new Set(dorms.value.map(d => d.buildingName))
  return Array.from(buildings).sort()
})

const getScoreType = (score: number) => {
  if (score >= 90) return 'success'
  if (score >= 80) return 'warning'
  return 'danger'
}

const fetchDorms = async () => {
  try {
    const res = await axios.get('/api/dormitories')
    if (res.data.code === 200) {
      dorms.value = res.data.data
    }
  } catch (error) {
    console.error('Failed to fetch dorms:', error)
    ElMessage.error('加载楼栋数据失败')
  }
}

const fetchRecords = async (dormId: number) => {
  loading.value = true
  try {
    const response = await axios.get(`/api/records/by-dorm/${dormId}`)
    if (response.data.code === 200) {
      records.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '获取数据失败')
    }
  } catch (error) {
    console.error('Failed to fetch records:', error)
    ElMessage.error('网络请求失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (!selectedBuilding.value || !inputRoomNumber.value) {
    ElMessage.warning('请选择楼栋并输入宿舍号')
    return
  }

  const dorm = dorms.value.find(d => 
    d.buildingName === selectedBuilding.value && 
    d.roomNumber === inputRoomNumber.value
  )

  if (dorm) {
    currentDorm.value = dorm
    fetchRecords(dorm.id)
  } else {
    currentDorm.value = null
    records.value = []
    ElMessage.error('未找到该宿舍，请检查楼栋和宿舍号是否正确')
  }
}

// const fetchCurrentStudent = async () => {
//   // Logic removed as per user request to not auto-fill dorm info
// }

// Rectification Logic
const openRectificationModal = (row: InspectionRecord) => {
  rectForm.recordId = row.id
  rectForm.description = ''
  rectForm.imageUrl = ''
  rectDialogVisible.value = true
}

const handleRectUploadSuccess = (response: any) => {
  if (response.code === 200) {
    rectForm.imageUrl = response.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

const beforeUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isJPG) ElMessage.error('只能上传 JPG/PNG 格式!')
  if (!isLt2M) ElMessage.error('大小不能超过 2MB!')
  return isJPG && isLt2M
}

const submitRectification = async () => {
  if (!rectForm.description) {
    ElMessage.warning('请填写整改说明')
    return
  }
  
  submittingRect.value = true
  try {
    const res = await axios.post('/api/records/rectification/submit', rectForm)
    if (res.data.code === 200) {
      ElMessage.success('提交成功')
      rectDialogVisible.value = false
      if (currentDorm.value) {
        fetchRecords(currentDorm.value.id) // Refresh list
      }
    } else {
      ElMessage.error(res.data.message || '提交失败')
    }
  } catch (error) {
    ElMessage.error('提交失败')
  } finally {
    submittingRect.value = false
  }
}

onMounted(async () => {
  await fetchDorms()
})
</script>

<style scoped>
.container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.student-selector {
  display: flex;
  align-items: center;
}
.student-selector .label {
  margin-right: 10px;
  font-weight: bold;
}
.dorm-info {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #e6f7ff;
  border-radius: 4px;
  border: 1px solid #91d5ff;
  font-size: 16px;
  color: #0050b3;
}
.details-container {
  padding: 20px;
  background-color: #f8f9fa;
}
.low-score {
  color: red;
  font-weight: bold;
}
.uploaded-image {
  width: 100%;
  max-width: 200px;
  border-radius: 4px;
}
.uploader-icon {
  font-size: 28px;
  color: #8c939d;
  border: 1px dashed #d9d9d9;
  padding: 20px;
  border-radius: 6px;
}
.main-photo {
  margin-bottom: 20px;
}
.rect-feedback {
  margin-bottom: 20px;
  padding: 10px;
  background: #fdf6ec;
  border-radius: 4px;
}
</style>
