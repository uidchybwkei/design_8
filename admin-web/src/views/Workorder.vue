<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>工单管理</span>
      </template>

      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="类型">
          <el-select v-model="queryParams.orderType" placeholder="全部" clearable style="width: 100px;">
            <el-option label="维修" :value="1" />
            <el-option label="保养" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px;">
            <el-option label="待派发" :value="0" />
            <el-option label="已派发" :value="1" />
            <el-option label="已接单" :value="2" />
            <el-option label="待验收" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已归档" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="工单号/设施名" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="orderNo" label="工单编号" width="180" />
        <el-table-column prop="orderTypeName" label="类型" width="70">
          <template #default="{ row }">
            <el-tag :type="row.orderType === 1 ? 'danger' : 'warning'" size="small">{{ row.orderTypeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="facilityName" label="设施名称" min-width="120" />
        <el-table-column prop="faultDescription" label="故障描述" min-width="150" show-overflow-tooltip />
        <el-table-column prop="reporterName" label="上报人" width="90" />
        <el-table-column prop="assigneeName" label="执行人" width="90" />
        <el-table-column prop="statusName" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showDetail(row)">详情</el-button>
            <el-button v-if="row.status === 0" type="warning" link @click="showAssign(row)">派单</el-button>
            <el-button v-if="row.status === 1 || row.status === 2" type="warning" link @click="showReassign(row)">转派</el-button>
            <el-button v-if="row.status === 3" type="success" link @click="showVerify(row)">验收</el-button>
            <el-button v-if="row.status === 4" type="info" link @click="handleArchive(row)">归档</el-button>
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

    <el-dialog v-model="detailVisible" title="工单详情" width="700px">
      <el-descriptions :column="2" border v-if="currentOrder">
        <el-descriptions-item label="工单编号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentOrder.status)">{{ currentOrder.statusName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="设施编码">{{ currentOrder.facilityCode }}</el-descriptions-item>
        <el-descriptions-item label="设施名称">{{ currentOrder.facilityName }}</el-descriptions-item>
        <el-descriptions-item label="故障描述" :span="2">{{ currentOrder.faultDescription }}</el-descriptions-item>
        <el-descriptions-item label="上报人">{{ currentOrder.reporterName }}</el-descriptions-item>
        <el-descriptions-item label="上报时间">{{ currentOrder.reportTime }}</el-descriptions-item>
        <el-descriptions-item label="执行人">{{ currentOrder.assigneeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="派单时间">{{ currentOrder.assignTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="接单时间">{{ currentOrder.acceptTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ currentOrder.submitTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理说明" :span="2">{{ currentOrder.processDescription || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理位置" :span="2">
          <span v-if="currentOrder.processLongitude">经度: {{ currentOrder.processLongitude }}, 纬度: {{ currentOrder.processLatitude }}</span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="验收人">{{ currentOrder.verifierName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="验收时间">{{ currentOrder.verifyTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="验收备注" :span="2">{{ currentOrder.verifyRemark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="currentOrder && currentOrder.processImages" style="margin-top: 15px;">
        <div style="font-weight: bold; margin-bottom: 10px;">处理图片:</div>
        <div class="image-list">
          <el-image v-for="(img, idx) in parseImages(currentOrder.processImages)" :key="idx"
            :src="getImageUrl(img)" :preview-src-list="parseImages(currentOrder.processImages).map(getImageUrl)"
            style="width: 100px; height: 100px; margin-right: 10px;" fit="cover" />
        </div>
      </div>
      <div style="margin-top: 20px;">
        <div style="font-weight: bold; margin-bottom: 10px; display: flex; justify-content: space-between; align-items: center;">
          <span>备件消耗:</span>
          <el-button v-if="currentOrder && currentOrder.status === 4" type="primary" size="small" @click="showAddConsumption">关联备件</el-button>
        </div>
        <el-table v-if="consumptionList.length > 0" :data="consumptionList" size="small" border>
          <el-table-column prop="itemCode" label="备件编码" width="120" />
          <el-table-column prop="itemName" label="备件名称" width="150" />
          <el-table-column prop="quantity" label="消耗数量" width="100" />
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column prop="operatorName" label="操作人" width="100" />
          <el-table-column prop="createTime" label="记录时间" />
          <el-table-column label="操作" width="80" v-if="currentOrder && currentOrder.status !== 5">
            <template #default="{ row }">
              <el-button type="danger" link size="small" @click="handleDeleteConsumption(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无备件消耗记录" :image-size="60" />
      </div>
      <div style="margin-top: 20px;">
        <div style="font-weight: bold; margin-bottom: 10px;">操作日志:</div>
        <el-timeline>
          <el-timeline-item v-for="log in orderLogs" :key="log.id" :timestamp="log.createTime" placement="top">
            <strong>{{ log.operatorName }}</strong> {{ log.remark }}
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-dialog>

    <el-dialog v-model="assignVisible" :title="isReassign ? '转派工单' : '派单'" width="400px">
      <el-form label-width="80px">
        <el-form-item label="执行人">
          <el-select v-model="assignForm.assigneeId" placeholder="请选择执行人" style="width: 100%;">
            <el-option v-for="u in assignableUsers" :key="u.id" :label="u.realName" :value="u.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssign">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="verifyVisible" title="验收工单" width="400px">
      <el-form label-width="80px">
        <el-form-item label="验收备注">
          <el-input v-model="verifyForm.remark" type="textarea" :rows="3" placeholder="请输入验收备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="verifyVisible = false">取消</el-button>
        <el-button type="primary" @click="handleVerify">验收通过</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="consumptionVisible" title="关联备件消耗" width="450px">
      <el-form :model="consumptionForm" label-width="100px">
        <el-form-item label="选择备件" required>
          <el-select v-model="consumptionForm.itemId" placeholder="请选择备件" style="width: 100%;">
            <el-option v-for="item in inventoryItems" :key="item.id" :label="`${item.itemName} (库存: ${item.currentStock}${item.unit})`" :value="item.id" :disabled="item.currentStock <= 0" />
          </el-select>
        </el-form-item>
        <el-form-item label="消耗数量" required>
          <el-input-number v-model="consumptionForm.quantity" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="consumptionVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddConsumption">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getWorkOrderPage, getWorkOrderById, getWorkOrderLogs, assignWorkOrder, reassignWorkOrder, verifyWorkOrder, archiveWorkOrder, getAssignableUsers } from '@/api/workorder'
import { getItemList, addConsumption, getConsumptionsByOrderId, deleteConsumption } from '@/api/inventory'

export default {
  name: 'Workorder',
  setup() {
    const loading = ref(false)
    const tableData = ref([])
    const total = ref(0)
    const detailVisible = ref(false)
    const assignVisible = ref(false)
    const verifyVisible = ref(false)
    const isReassign = ref(false)
    const currentOrder = ref(null)
    const orderLogs = ref([])
    const assignableUsers = ref([])

    const queryParams = reactive({ pageNum: 1, pageSize: 10, status: null, orderType: null, keyword: '' })
    const assignForm = reactive({ orderId: null, assigneeId: null })
    const verifyForm = reactive({ orderId: null, remark: '' })
    const consumptionVisible = ref(false)
    const consumptionForm = reactive({ orderId: null, itemId: null, quantity: 1 })
    const consumptionList = ref([])
    const inventoryItems = ref([])

    const loadData = async () => {
      loading.value = true
      try {
        const res = await getWorkOrderPage(queryParams)
        tableData.value = res.data.records
        total.value = res.data.total
      } finally {
        loading.value = false
      }
    }

    const resetQuery = () => {
      queryParams.status = null
      queryParams.orderType = null
      queryParams.keyword = ''
      queryParams.pageNum = 1
      loadData()
    }

    const getStatusType = (status) => {
      const map = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'warning', 4: 'success', 5: '' }
      return map[status] || ''
    }

    const showDetail = async (row) => {
      const res = await getWorkOrderById(row.id)
      currentOrder.value = res.data
      const logsRes = await getWorkOrderLogs(row.id)
      orderLogs.value = logsRes.data
      try {
        const consumRes = await getConsumptionsByOrderId(row.id)
        consumptionList.value = consumRes.data || []
      } catch { consumptionList.value = [] }
      detailVisible.value = true
    }

    const showAssign = async (row) => {
      isReassign.value = false
      assignForm.orderId = row.id
      assignForm.assigneeId = null
      const res = await getAssignableUsers()
      assignableUsers.value = res.data
      assignVisible.value = true
    }

    const showReassign = async (row) => {
      isReassign.value = true
      assignForm.orderId = row.id
      assignForm.assigneeId = null
      const res = await getAssignableUsers()
      assignableUsers.value = res.data
      assignVisible.value = true
    }

    const handleAssign = async () => {
      if (!assignForm.assigneeId) {
        ElMessage.warning('请选择执行人')
        return
      }
      if (isReassign.value) {
        await reassignWorkOrder(assignForm.orderId, assignForm.assigneeId)
        ElMessage.success('转派成功')
      } else {
        await assignWorkOrder(assignForm.orderId, assignForm.assigneeId)
        ElMessage.success('派单成功')
      }
      assignVisible.value = false
      loadData()
    }

    const showVerify = (row) => {
      verifyForm.orderId = row.id
      verifyForm.remark = ''
      verifyVisible.value = true
    }

    const handleVerify = async () => {
      await verifyWorkOrder(verifyForm.orderId, verifyForm.remark)
      ElMessage.success('验收成功')
      verifyVisible.value = false
      loadData()
    }

    const handleArchive = async (row) => {
      await ElMessageBox.confirm('确定要归档该工单吗？归档后不可修改。', '提示', { type: 'warning' })
      await archiveWorkOrder(row.id)
      ElMessage.success('归档成功')
      loadData()
    }

    const parseImages = (str) => {
      if (!str) return []
      try { return JSON.parse(str) } catch { return [] }
    }

    const getImageUrl = (url) => {
      if (url.startsWith('http')) return url
      return 'http://localhost:8080' + url
    }

    const showAddConsumption = async () => {
      consumptionForm.orderId = currentOrder.value.id
      consumptionForm.itemId = null
      consumptionForm.quantity = 1
      try {
        const res = await getItemList()
        inventoryItems.value = res.data.filter(item => item.status === 1)
      } catch { inventoryItems.value = [] }
      consumptionVisible.value = true
    }

    const handleAddConsumption = async () => {
      if (!consumptionForm.itemId) {
        ElMessage.warning('请选择备件')
        return
      }
      if (consumptionForm.quantity <= 0) {
        ElMessage.warning('消耗数量必须大于0')
        return
      }
      try {
        await addConsumption(consumptionForm)
        ElMessage.success('备件消耗关联成功')
        consumptionVisible.value = false
        const consumRes = await getConsumptionsByOrderId(currentOrder.value.id)
        consumptionList.value = consumRes.data || []
      } catch (err) {
        ElMessage.error(err.response?.data?.message || '操作失败')
      }
    }

    const handleDeleteConsumption = async (row) => {
      await ElMessageBox.confirm('删除后将回退库存，确定删除该消耗记录？', '提示', { type: 'warning' })
      try {
        await deleteConsumption(row.id)
        ElMessage.success('删除成功，库存已回退')
        const consumRes = await getConsumptionsByOrderId(currentOrder.value.id)
        consumptionList.value = consumRes.data || []
      } catch (err) {
        ElMessage.error(err.response?.data?.message || '删除失败')
      }
    }

    onMounted(() => { loadData() })

    return {
      loading, tableData, total, queryParams, detailVisible, assignVisible, verifyVisible,
      isReassign, currentOrder, orderLogs, assignableUsers, assignForm, verifyForm,
      consumptionVisible, consumptionForm, consumptionList, inventoryItems,
      loadData, resetQuery, getStatusType, showDetail, showAssign, showReassign,
      handleAssign, showVerify, handleVerify, handleArchive, parseImages, getImageUrl,
      showAddConsumption, handleAddConsumption, handleDeleteConsumption
    }
  }
}
</script>

<style scoped>
.page-container { padding: 20px; }
.search-form { margin-bottom: 20px; }
.image-list { display: flex; flex-wrap: wrap; }
</style>
