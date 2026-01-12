<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>保养计划</span>
          <div>
            <el-button type="success" @click="handleTrigger">执行定时任务</el-button>
            <el-button type="primary" @click="showAdd">新增计划</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="planName" label="计划名称" min-width="120" />
        <el-table-column prop="facilityName" label="设施名称" min-width="120" />
        <el-table-column prop="cycleDays" label="周期(天)" width="90" />
        <el-table-column prop="description" label="保养内容" min-width="150" show-overflow-tooltip />
        <el-table-column prop="statusName" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastGenerateDate" label="上次生成" width="110" />
        <el-table-column prop="nextGenerateDate" label="下次生成" width="110" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" type="success" link @click="handleEnable(row)">启用</el-button>
            <el-button v-if="row.status === 1" type="warning" link @click="handleDisable(row)">停用</el-button>
            <el-button type="info" link @click="handleGenerate(row)">立即生成</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑保养计划' : '新增保养计划'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="计划名称" required>
          <el-input v-model="form.planName" placeholder="请输入计划名称" />
        </el-form-item>
        <el-form-item label="关联设施" required>
          <el-select v-model="form.facilityId" placeholder="请选择设施" style="width: 100%;" filterable>
            <el-option v-for="f in facilities" :key="f.id" :label="f.facilityName" :value="f.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="周期(天)" required>
          <el-input-number v-model="form.cycleDays" :min="1" :max="365" />
        </el-form-item>
        <el-form-item label="保养内容">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入保养内容描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMaintenancePlanList, createMaintenancePlan, updateMaintenancePlan, enableMaintenancePlan, disableMaintenancePlan, deleteMaintenancePlan, triggerGenerate, generateNow } from '@/api/maintenance'
import { getFacilityList } from '@/api/facility'

export default {
  name: 'Maintenance',
  setup() {
    const loading = ref(false)
    const tableData = ref([])
    const dialogVisible = ref(false)
    const isEdit = ref(false)
    const editId = ref(null)
    const facilities = ref([])

    const form = reactive({ planName: '', facilityId: null, cycleDays: 30, description: '' })

    const loadData = async () => {
      loading.value = true
      try {
        const res = await getMaintenancePlanList()
        tableData.value = res.data
      } catch (err) {
        console.error('加载保养计划失败:', err)
        ElMessage.error('加载保养计划失败: ' + (err.message || '请检查后端服务'))
      } finally {
        loading.value = false
      }
    }

    const loadFacilities = async () => {
      try {
        const res = await getFacilityList()
        facilities.value = res.data
      } catch (err) {
        console.error('加载设施列表失败:', err)
      }
    }

    const resetForm = () => {
      form.planName = ''
      form.facilityId = null
      form.cycleDays = 30
      form.description = ''
    }

    const showAdd = () => {
      isEdit.value = false
      editId.value = null
      resetForm()
      dialogVisible.value = true
    }

    const showEdit = (row) => {
      isEdit.value = true
      editId.value = row.id
      form.planName = row.planName
      form.facilityId = row.facilityId
      form.cycleDays = row.cycleDays
      form.description = row.description
      dialogVisible.value = true
    }

    const handleSubmit = async () => {
      if (!form.planName) {
        ElMessage.warning('请输入计划名称')
        return
      }
      if (!form.facilityId) {
        ElMessage.warning('请选择设施')
        return
      }

      if (isEdit.value) {
        await updateMaintenancePlan(editId.value, form)
        ElMessage.success('更新成功')
      } else {
        await createMaintenancePlan(form)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadData()
    }

    const handleEnable = async (row) => {
      await enableMaintenancePlan(row.id)
      ElMessage.success('启用成功')
      loadData()
    }

    const handleDisable = async (row) => {
      await disableMaintenancePlan(row.id)
      ElMessage.success('停用成功')
      loadData()
    }

    const handleDelete = async (row) => {
      await ElMessageBox.confirm('确定要删除该保养计划吗？', '提示', { type: 'warning' })
      await deleteMaintenancePlan(row.id)
      ElMessage.success('删除成功')
      loadData()
    }

    const handleTrigger = async () => {
      const res = await triggerGenerate()
      ElMessage.success(`执行完成，生成工单数: ${res.data}`)
      loadData()
    }

    const handleGenerate = async (row) => {
      if (row.status !== 1) {
        ElMessage.warning('只有启用的计划才能生成工单')
        return
      }
      const res = await generateNow(row.id)
      if (res.data) {
        ElMessage.success(`工单生成成功: ${res.data.orderNo}`)
      } else {
        ElMessage.warning('今日已生成过工单（幂等控制）')
      }
      loadData()
    }

    onMounted(() => {
      loadData()
      loadFacilities()
    })

    return {
      loading, tableData, dialogVisible, isEdit, form, facilities,
      loadData, showAdd, showEdit, handleSubmit, handleEnable, handleDisable,
      handleDelete, handleTrigger, handleGenerate
    }
  }
}
</script>

<style scoped>
.page-container { padding: 20px; }
.header-row { display: flex; justify-content: space-between; align-items: center; }
</style>
