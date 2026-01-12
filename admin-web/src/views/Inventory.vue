<template>
  <div class="page-container">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="备件台账" name="items">
        <div class="toolbar">
          <el-button type="primary" @click="showAddItem">新增备件</el-button>
          <el-button type="warning" @click="loadWarningList">查看预警备件</el-button>
        </div>
        <el-table :data="itemList" v-loading="loading" stripe>
          <el-table-column prop="itemCode" label="备件编码" width="120" />
          <el-table-column prop="itemName" label="备件名称" width="150" />
          <el-table-column prop="specification" label="型号/规格" width="120" />
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column prop="currentStock" label="当前库存" width="100">
            <template #default="{ row }">
              <el-tag :type="row.warning ? 'danger' : 'success'">{{ row.currentStock }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="warningThreshold" label="预警阈值" width="100" />
          <el-table-column prop="statusName" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.statusName }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="showStockIn(row)">入库</el-button>
              <el-button size="small" @click="showStockOut(row)">出库</el-button>
              <el-button size="small" type="primary" @click="showEditItem(row)">编辑</el-button>
              <el-button size="small" v-if="row.status === 1" type="warning" @click="handleDisable(row)">停用</el-button>
              <el-button size="small" v-else type="success" @click="handleEnable(row)">启用</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="库存流水" name="records">
        <el-table :data="recentRecords" v-loading="recordLoading" stripe>
          <el-table-column prop="createTime" label="操作时间" width="180" />
          <el-table-column prop="itemCode" label="备件编码" width="120" />
          <el-table-column prop="itemName" label="备件名称" width="150" />
          <el-table-column prop="recordTypeName" label="类型" width="80">
            <template #default="{ row }">
              <el-tag :type="row.recordType === 1 ? 'success' : 'danger'">{{ row.recordTypeName }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="beforeStock" label="操作前库存" width="100" />
          <el-table-column prop="afterStock" label="操作后库存" width="100" />
          <el-table-column prop="reason" label="备注" />
          <el-table-column prop="orderNo" label="关联工单" width="180" />
          <el-table-column prop="operatorName" label="操作人" width="100" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="库存预警" name="warning">
        <el-alert v-if="warningList.length > 0" type="warning" :closable="false" show-icon
          :title="`共有 ${warningList.length} 个备件库存不足，请及时补货`" style="margin-bottom: 16px" />
        <el-table :data="warningList" v-loading="warningLoading" stripe>
          <el-table-column prop="itemCode" label="备件编码" width="120" />
          <el-table-column prop="itemName" label="备件名称" width="150" />
          <el-table-column prop="specification" label="型号/规格" width="120" />
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column prop="currentStock" label="当前库存" width="100">
            <template #default="{ row }">
              <el-tag type="danger">{{ row.currentStock }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="warningThreshold" label="预警阈值" width="100" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="showStockIn(row)">入库</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="itemDialogVisible" :title="isEdit ? '编辑备件' : '新增备件'" width="500px">
      <el-form :model="itemForm" label-width="100px">
        <el-form-item label="备件编码" required>
          <el-input v-model="itemForm.itemCode" placeholder="请输入备件编码" />
        </el-form-item>
        <el-form-item label="备件名称" required>
          <el-input v-model="itemForm.itemName" placeholder="请输入备件名称" />
        </el-form-item>
        <el-form-item label="型号/规格">
          <el-input v-model="itemForm.specification" placeholder="请输入型号/规格" />
        </el-form-item>
        <el-form-item label="单位" required>
          <el-input v-model="itemForm.unit" placeholder="个、件、米、kg等" />
        </el-form-item>
        <el-form-item label="预警阈值">
          <el-input-number v-model="itemForm.warningThreshold" :min="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="itemForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitItem">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="stockDialogVisible" :title="stockType === 'in' ? '入库操作' : '出库操作'" width="400px">
      <el-form :model="stockForm" label-width="100px">
        <el-form-item label="备件">
          <span>{{ stockForm.itemName }} (当前库存: {{ stockForm.currentStock }})</span>
        </el-form-item>
        <el-form-item label="数量" required>
          <el-input-number v-model="stockForm.quantity" :min="1" :max="stockType === 'out' ? stockForm.currentStock : 99999" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="stockForm.reason" placeholder="请输入操作原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStock">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getItemList, createItem, updateItem, enableItem, disableItem, stockIn, stockOut, getWarningList, getRecentRecords } from '@/api/inventory'

export default {
  name: 'Inventory',
  setup() {
    const activeTab = ref('items')
    const loading = ref(false)
    const recordLoading = ref(false)
    const warningLoading = ref(false)
    const itemList = ref([])
    const recentRecords = ref([])
    const warningList = ref([])

    const itemDialogVisible = ref(false)
    const isEdit = ref(false)
    const editId = ref(null)
    const itemForm = reactive({ itemCode: '', itemName: '', specification: '', unit: '', warningThreshold: 10, remark: '' })

    const stockDialogVisible = ref(false)
    const stockType = ref('in')
    const stockForm = reactive({ itemId: null, itemName: '', currentStock: 0, quantity: 1, reason: '' })

    const loadItems = async () => {
      loading.value = true
      try {
        const res = await getItemList()
        itemList.value = res.data
      } catch (err) {
        ElMessage.error('加载备件列表失败')
      } finally {
        loading.value = false
      }
    }

    const loadRecords = async () => {
      recordLoading.value = true
      try {
        const res = await getRecentRecords(100)
        recentRecords.value = res.data
      } catch (err) {
        ElMessage.error('加载流水记录失败')
      } finally {
        recordLoading.value = false
      }
    }

    const loadWarningList = async () => {
      warningLoading.value = true
      activeTab.value = 'warning'
      try {
        const res = await getWarningList()
        warningList.value = res.data
      } catch (err) {
        ElMessage.error('加载预警列表失败')
      } finally {
        warningLoading.value = false
      }
    }

    const resetItemForm = () => {
      itemForm.itemCode = ''
      itemForm.itemName = ''
      itemForm.specification = ''
      itemForm.unit = ''
      itemForm.warningThreshold = 10
      itemForm.remark = ''
    }

    const showAddItem = () => {
      isEdit.value = false
      editId.value = null
      resetItemForm()
      itemDialogVisible.value = true
    }

    const showEditItem = (row) => {
      isEdit.value = true
      editId.value = row.id
      itemForm.itemCode = row.itemCode
      itemForm.itemName = row.itemName
      itemForm.specification = row.specification
      itemForm.unit = row.unit
      itemForm.warningThreshold = row.warningThreshold
      itemForm.remark = row.remark
      itemDialogVisible.value = true
    }

    const handleSubmitItem = async () => {
      if (!itemForm.itemCode || !itemForm.itemName || !itemForm.unit) {
        ElMessage.warning('请填写必填项')
        return
      }
      try {
        if (isEdit.value) {
          await updateItem(editId.value, itemForm)
          ElMessage.success('更新成功')
        } else {
          await createItem(itemForm)
          ElMessage.success('创建成功')
        }
        itemDialogVisible.value = false
        loadItems()
      } catch (err) {
        ElMessage.error(err.response?.data?.message || '操作失败')
      }
    }

    const handleEnable = async (row) => {
      await enableItem(row.id)
      ElMessage.success('启用成功')
      loadItems()
    }

    const handleDisable = async (row) => {
      await disableItem(row.id)
      ElMessage.success('停用成功')
      loadItems()
    }

    const showStockIn = (row) => {
      stockType.value = 'in'
      stockForm.itemId = row.id
      stockForm.itemName = row.itemName
      stockForm.currentStock = row.currentStock
      stockForm.quantity = 1
      stockForm.reason = ''
      stockDialogVisible.value = true
    }

    const showStockOut = (row) => {
      stockType.value = 'out'
      stockForm.itemId = row.id
      stockForm.itemName = row.itemName
      stockForm.currentStock = row.currentStock
      stockForm.quantity = 1
      stockForm.reason = ''
      stockDialogVisible.value = true
    }

    const handleStock = async () => {
      if (stockForm.quantity <= 0) {
        ElMessage.warning('数量必须大于0')
        return
      }
      try {
        if (stockType.value === 'in') {
          await stockIn({ itemId: stockForm.itemId, quantity: stockForm.quantity, reason: stockForm.reason })
          ElMessage.success('入库成功')
        } else {
          await stockOut({ itemId: stockForm.itemId, quantity: stockForm.quantity, reason: stockForm.reason })
          ElMessage.success('出库成功')
        }
        stockDialogVisible.value = false
        loadItems()
        loadRecords()
        loadWarningList()
      } catch (err) {
        ElMessage.error(err.response?.data?.message || '操作失败')
      }
    }

    onMounted(() => {
      loadItems()
      loadRecords()
      loadWarningList()
    })

    return {
      activeTab, loading, recordLoading, warningLoading,
      itemList, recentRecords, warningList,
      itemDialogVisible, isEdit, itemForm,
      stockDialogVisible, stockType, stockForm,
      loadItems, loadRecords, loadWarningList,
      showAddItem, showEditItem, handleSubmitItem,
      handleEnable, handleDisable,
      showStockIn, showStockOut, handleStock
    }
  }
}
</script>

<style scoped>
.page-container { padding: 20px; }
.toolbar { margin-bottom: 16px; }
</style>
