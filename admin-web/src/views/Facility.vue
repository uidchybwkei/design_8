<template>
  <div class="facility-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>设施管理</span>
          <el-button type="primary" @click="handleAdd">新增设施</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="分类">
          <el-select v-model="queryParams.categoryId" placeholder="全部" clearable style="width: 150px;">
            <el-option v-for="item in categories" :key="item.id" :label="item.categoryName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px;">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="名称/编码" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border style="width: 100%;">
        <el-table-column prop="facilityCode" label="设施编码" width="160" />
        <el-table-column prop="facilityName" label="设施名称" min-width="150" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="location" label="位置" min-width="150" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleQrCode(row)">二维码</el-button>
            <el-button v-if="row.status === 1" type="warning" link @click="handleStatus(row, 0)">停用</el-button>
            <el-button v-else type="success" link @click="handleStatus(row, 1)">启用</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="设施名称" prop="facilityName">
          <el-input v-model="formData.facilityName" placeholder="请输入设施名称" />
        </el-form-item>
        <el-form-item label="设施分类" prop="categoryId">
          <el-select v-model="formData.categoryId" placeholder="请选择分类" style="width: 100%;">
            <el-option v-for="item in categories" :key="item.id" :label="item.categoryName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="安装位置" prop="location">
          <el-input v-model="formData.location" placeholder="请输入安装位置" />
        </el-form-item>
        <el-form-item label="规格型号">
          <el-input v-model="formData.specification" placeholder="请输入规格型号" />
        </el-form-item>
        <el-form-item label="生产厂家">
          <el-input v-model="formData.manufacturer" placeholder="请输入生产厂家" />
        </el-form-item>
        <el-form-item label="安装日期">
          <el-date-picker v-model="formData.installDate" type="date" placeholder="选择安装日期" style="width: 100%;" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="质保到期">
          <el-date-picker v-model="formData.warrantyDate" type="date" placeholder="选择质保到期日" style="width: 100%;" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="qrDialogVisible" title="设施二维码" width="400px">
      <div class="qr-container">
        <div ref="qrCodeRef" class="qr-code"></div>
        <p class="qr-info">{{ currentFacility?.facilityName }}</p>
        <p class="qr-code-text">{{ currentFacility?.facilityCode }}</p>
      </div>
      <template #footer>
        <el-button @click="downloadQrCode">下载二维码</el-button>
        <el-button type="primary" @click="qrDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFacilityPage, createFacility, updateFacility, updateFacilityStatus, deleteFacility, getCategories } from '@/api/facility'
import QRCode from 'qrcode'

export default {
  name: 'Facility',
  setup() {
    const loading = ref(false)
    const tableData = ref([])
    const total = ref(0)
    const categories = ref([])
    const dialogVisible = ref(false)
    const dialogTitle = ref('新增设施')
    const qrDialogVisible = ref(false)
    const currentFacility = ref(null)
    const qrCodeRef = ref(null)
    const formRef = ref(null)

    const queryParams = reactive({
      pageNum: 1,
      pageSize: 10,
      categoryId: null,
      status: null,
      keyword: ''
    })

    const formData = reactive({
      id: null,
      facilityName: '',
      categoryId: null,
      location: '',
      specification: '',
      manufacturer: '',
      installDate: null,
      warrantyDate: null,
      remark: ''
    })

    const formRules = {
      facilityName: [{ required: true, message: '请输入设施名称', trigger: 'blur' }]
    }

    const loadData = async () => {
      loading.value = true
      try {
        const res = await getFacilityPage(queryParams)
        tableData.value = res.data.records
        total.value = res.data.total
      } finally {
        loading.value = false
      }
    }

    const loadCategories = async () => {
      const res = await getCategories()
      categories.value = res.data
    }

    const resetQuery = () => {
      queryParams.categoryId = null
      queryParams.status = null
      queryParams.keyword = ''
      queryParams.pageNum = 1
      loadData()
    }

    const handleAdd = () => {
      dialogTitle.value = '新增设施'
      Object.assign(formData, {
        id: null,
        facilityName: '',
        categoryId: null,
        location: '',
        specification: '',
        manufacturer: '',
        installDate: null,
        warrantyDate: null,
        remark: ''
      })
      dialogVisible.value = true
    }

    const handleEdit = (row) => {
      dialogTitle.value = '编辑设施'
      Object.assign(formData, {
        id: row.id,
        facilityName: row.facilityName,
        categoryId: row.categoryId,
        location: row.location,
        specification: row.specification,
        manufacturer: row.manufacturer,
        installDate: row.installDate,
        warrantyDate: row.warrantyDate,
        remark: row.remark
      })
      dialogVisible.value = true
    }

    const handleSubmit = async () => {
      await formRef.value.validate()
      if (formData.id) {
        await updateFacility(formData)
        ElMessage.success('更新成功')
      } else {
        await createFacility(formData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadData()
    }

    const handleStatus = async (row, status) => {
      const action = status === 1 ? '启用' : '停用'
      await ElMessageBox.confirm(`确定要${action}该设施吗？`, '提示', { type: 'warning' })
      await updateFacilityStatus(row.id, status)
      ElMessage.success(`${action}成功`)
      loadData()
    }

    const handleDelete = async (row) => {
      await ElMessageBox.confirm('确定要删除该设施吗？', '提示', { type: 'warning' })
      await deleteFacility(row.id)
      ElMessage.success('删除成功')
      loadData()
    }

    const handleQrCode = async (row) => {
      currentFacility.value = row
      qrDialogVisible.value = true
      await nextTick()
      generateQrCode(row.facilityCode)
    }

    const generateQrCode = (code) => {
      if (qrCodeRef.value) {
        qrCodeRef.value.innerHTML = ''
        const canvas = document.createElement('canvas')
        qrCodeRef.value.appendChild(canvas)
        QRCode.toCanvas(canvas, code, { width: 200, margin: 2 })
      }
    }

    const downloadQrCode = () => {
      const canvas = qrCodeRef.value?.querySelector('canvas')
      if (canvas) {
        const link = document.createElement('a')
        link.download = `${currentFacility.value.facilityCode}.png`
        link.href = canvas.toDataURL('image/png')
        link.click()
      }
    }

    onMounted(() => {
      loadCategories()
      loadData()
    })

    return {
      loading,
      tableData,
      total,
      categories,
      queryParams,
      dialogVisible,
      dialogTitle,
      formData,
      formRules,
      formRef,
      qrDialogVisible,
      currentFacility,
      qrCodeRef,
      loadData,
      resetQuery,
      handleAdd,
      handleEdit,
      handleSubmit,
      handleStatus,
      handleDelete,
      handleQrCode,
      downloadQrCode
    }
  }
}
</script>

<style scoped>
.facility-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-form {
  margin-bottom: 20px;
}
.qr-container {
  text-align: center;
  padding: 20px;
}
.qr-code {
  display: inline-block;
  padding: 10px;
  background: #fff;
  border: 1px solid #eee;
}
.qr-info {
  margin-top: 15px;
  font-size: 16px;
  color: #333;
}
.qr-code-text {
  margin-top: 5px;
  font-size: 14px;
  color: #666;
}
</style>
