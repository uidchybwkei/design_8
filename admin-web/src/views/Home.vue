<template>
  <div class="page-container">
    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="统计时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD HH:mm:ss"
            :default-time="[new Date(0, 0, 0, 0, 0, 0), new Date(0, 0, 0, 23, 59, 59)]"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadAllStats">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ overview.totalCount || 0 }}</div>
          <div class="stat-label">工单总数</div>
          <div class="stat-detail">
            <span class="repair">维修: {{ overview.repairCount || 0 }}</span>
            <span class="maintenance">保养: {{ overview.maintenanceCount || 0 }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card success">
          <div class="stat-value">{{ overview.completedCount || 0 }}</div>
          <div class="stat-label">已完成</div>
          <div class="stat-detail">
            <span class="rate">完成率: {{ overview.completionRate || 0 }}%</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card warning">
          <div class="stat-value">{{ overview.pendingCount || 0 }}</div>
          <div class="stat-label">待处理</div>
          <div class="stat-detail">
            <span>待验收: {{ overview.waitingVerifyCount || 0 }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card info">
          <div class="stat-value">{{ duration.avgDurationDisplay || '-' }}</div>
          <div class="stat-label">平均处理耗时</div>
          <div class="stat-detail">
            <span>样本数: {{ duration.totalSampleCount || 0 }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="detail-section">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>处理耗时统计</span>
            <el-tooltip content="统计口径：从接单时间到提交时间的平均时长" placement="top">
              <el-icon style="margin-left: 8px; cursor: help;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="维修工单平均耗时">
              {{ duration.repairAvgDisplay || '-' }}
              <el-tag size="small" style="margin-left: 8px;">样本: {{ duration.repairSampleCount || 0 }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="保养工单平均耗时">
              {{ duration.maintenanceAvgDisplay || '-' }}
              <el-tag size="small" style="margin-left: 8px;">样本: {{ duration.maintenanceSampleCount || 0 }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="总体平均耗时">
              {{ duration.avgDurationDisplay || '-' }}
              <el-tag size="small" style="margin-left: 8px;">样本: {{ duration.totalSampleCount || 0 }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>故障高发设施 Top 5</span>
            <el-tooltip content="统计口径：按设施分组统计维修工单数量" placement="top">
              <el-icon style="margin-left: 8px; cursor: help;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
          <el-table :data="topFacilities" size="small" v-loading="loading">
            <el-table-column type="index" label="排名" width="60" />
            <el-table-column prop="facilityCode" label="设施编码" width="120" />
            <el-table-column prop="facilityName" label="设施名称" />
            <el-table-column prop="repairCount" label="维修次数" width="100">
              <template #default="{ row }">
                <el-tag type="danger">{{ row.repairCount }} 次</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="topFacilities.length === 0 && !loading" description="暂无维修工单数据" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <el-card class="explain-card">
      <template #header>
        <span>统计口径说明</span>
      </template>
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="工单完成率">已完成 + 已归档工单数 / 总工单数 × 100%</el-descriptions-item>
        <el-descriptions-item label="平均处理耗时">Σ(提交时间 - 接单时间) / 已提交工单数</el-descriptions-item>
        <el-descriptions-item label="故障高发设施">按设施分组统计维修工单(orderType=1)数量，取Top N</el-descriptions-item>
        <el-descriptions-item label="数据范围">仅统计未删除的工单，时间筛选基于工单创建时间</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { QuestionFilled } from '@element-plus/icons-vue'
import { getWorkOrderOverview, getAverageDuration, getTopFaultFacilities } from '@/api/stats'

export default {
  name: 'Home',
  components: { QuestionFilled },
  setup() {
    const loading = ref(false)
    const dateRange = ref(null)
    const overview = reactive({})
    const duration = reactive({})
    const topFacilities = ref([])

    const getParams = () => {
      const params = {}
      if (dateRange.value && dateRange.value.length === 2) {
        params.startTime = dateRange.value[0]
        params.endTime = dateRange.value[1]
      }
      return params
    }

    const loadAllStats = async () => {
      loading.value = true
      try {
        const params = getParams()
        const [overviewRes, durationRes, facilitiesRes] = await Promise.all([
          getWorkOrderOverview(params),
          getAverageDuration(params),
          getTopFaultFacilities({ ...params, topN: 5 })
        ])
        Object.assign(overview, overviewRes.data)
        Object.assign(duration, durationRes.data)
        topFacilities.value = facilitiesRes.data
      } catch (err) {
        console.error('加载统计数据失败', err)
      } finally {
        loading.value = false
      }
    }

    const resetFilter = () => {
      dateRange.value = null
      loadAllStats()
    }

    onMounted(() => {
      loadAllStats()
    })

    return {
      loading, dateRange, overview, duration, topFacilities,
      loadAllStats, resetFilter
    }
  }
}
</script>

<style scoped>
.page-container { padding: 20px; }
.filter-card { margin-bottom: 20px; }
.stats-cards { margin-bottom: 20px; }
.stat-card { text-align: center; padding: 10px 0; }
.stat-value { font-size: 36px; font-weight: bold; color: #409eff; }
.stat-card.success .stat-value { color: #67c23a; }
.stat-card.warning .stat-value { color: #e6a23c; }
.stat-card.info .stat-value { color: #909399; font-size: 28px; }
.stat-label { font-size: 14px; color: #666; margin: 8px 0; }
.stat-detail { font-size: 12px; color: #999; }
.stat-detail .repair { margin-right: 12px; color: #f56c6c; }
.stat-detail .maintenance { color: #e6a23c; }
.stat-detail .rate { color: #67c23a; font-weight: bold; }
.detail-section { margin-bottom: 20px; }
.explain-card { background: #fafafa; }
</style>
