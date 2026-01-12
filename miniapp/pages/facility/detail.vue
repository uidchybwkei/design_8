<template>
  <view class="detail-container">
    <view v-if="loading" class="loading">
      <text>加载中...</text>
    </view>

    <view v-else-if="error" class="error">
      <text class="error-text">{{ errorMsg }}</text>
      <button type="default" size="mini" @click="goBack">返回</button>
    </view>

    <view v-else class="facility-info">
      <view class="info-header">
        <text class="facility-name">{{ facility.facilityName }}</text>
        <view class="status-tag" :class="facility.status === 1 ? 'active' : 'inactive'">
          {{ facility.status === 1 ? '启用' : '停用' }}
        </view>
      </view>

      <view class="info-section">
        <view class="info-item">
          <text class="label">设施编码</text>
          <text class="value">{{ facility.facilityCode }}</text>
        </view>
        <view class="info-item">
          <text class="label">设施分类</text>
          <text class="value">{{ facility.categoryName || '-' }}</text>
        </view>
        <view class="info-item">
          <text class="label">安装位置</text>
          <text class="value">{{ facility.location || '-' }}</text>
        </view>
        <view class="info-item">
          <text class="label">规格型号</text>
          <text class="value">{{ facility.specification || '-' }}</text>
        </view>
        <view class="info-item">
          <text class="label">生产厂家</text>
          <text class="value">{{ facility.manufacturer || '-' }}</text>
        </view>
        <view class="info-item">
          <text class="label">安装日期</text>
          <text class="value">{{ facility.installDate || '-' }}</text>
        </view>
        <view class="info-item">
          <text class="label">质保到期</text>
          <text class="value">{{ facility.warrantyDate || '-' }}</text>
        </view>
        <view v-if="facility.remark" class="info-item">
          <text class="label">备注</text>
          <text class="value">{{ facility.remark }}</text>
        </view>
      </view>

      <view class="action-section">
        <view class="action-title">操作入口</view>
        <view class="action-list">
          <view class="action-item" @click="reportFault" v-if="facility.status === 1">
            <text>上报故障</text>
            <text class="action-arrow">></text>
          </view>
          <view class="action-item" @click="viewHistory">
            <text>维修记录 ({{ historyCount }})</text>
            <text class="action-arrow">></text>
          </view>
        </view>
      </view>

      <view class="history-section" v-if="showHistory && historyList.length > 0">
        <view class="section-title">维修记录</view>
        <view class="history-list">
          <view v-for="item in historyList" :key="item.id" class="history-item">
            <view class="history-header">
              <text class="history-no">{{ item.orderNo }}</text>
              <text class="history-status">{{ item.statusName }}</text>
            </view>
            <view class="history-body">
              <text>{{ item.faultDescription }}</text>
            </view>
            <view class="history-footer">
              <text>{{ item.createTime }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getFacilityByCode } from '../../api/facility.js'

export default {
  data() {
    return {
      loading: true,
      error: false,
      errorMsg: '',
      facility: {},
      historyList: [],
      historyCount: 0,
      showHistory: false
    }
  },
  onLoad(options) {
    if (options.code) {
      this.loadFacility(options.code)
    } else {
      this.error = true
      this.errorMsg = '缺少设施编码参数'
      this.loading = false
    }
  },
  methods: {
    async loadFacility(code) {
      this.loading = true
      this.error = false
      try {
        const res = await getFacilityByCode(code)
        this.facility = res.data
        this.loadHistory()
      } catch (err) {
        this.error = true
        this.errorMsg = err.message || '获取设施信息失败'
      } finally {
        this.loading = false
      }
    },
    async loadHistory() {
      try {
        const token = uni.getStorageSync('token')
        const res = await uni.request({
          url: `http://localhost:8080/workorder/facility/${this.facility.id}/history`,
          method: 'GET',
          header: { 'Authorization': 'Bearer ' + token }
        })
        if (res.data.code === 200) {
          this.historyList = res.data.data || []
          this.historyCount = this.historyList.length
        }
      } catch (err) {
        console.error('加载历史失败', err)
      }
    },
    goBack() {
      uni.navigateBack()
    },
    viewHistory() {
      this.showHistory = !this.showHistory
    },
    reportFault() {
      if (this.facility.status !== 1) {
        uni.showToast({ title: '该设施已停用', icon: 'none' })
        return
      }
      uni.navigateTo({
        url: `/pages/workorder/report?facilityCode=${this.facility.facilityCode}`
      })
    }
  }
}
</script>

<style scoped>
.detail-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 40rpx;
}

.loading, .error {
  padding: 200rpx 40rpx;
  text-align: center;
}

.error-text {
  display: block;
  color: #f56c6c;
  font-size: 28rpx;
  margin-bottom: 30rpx;
}

.facility-info {
  padding: 20rpx;
}

.info-header {
  background-color: #fff;
  padding: 30rpx;
  border-radius: 12rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.facility-name {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

.status-tag {
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
}

.status-tag.active {
  background-color: #e1f3d8;
  color: #67c23a;
}

.status-tag.inactive {
  background-color: #fde2e2;
  color: #f56c6c;
}

.info-section {
  background-color: #fff;
  border-radius: 12rpx;
  padding: 10rpx 30rpx;
  margin-bottom: 20rpx;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 24rpx 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.label {
  color: #666;
  font-size: 28rpx;
}

.value {
  color: #333;
  font-size: 28rpx;
  text-align: right;
  max-width: 60%;
}

.action-section {
  background-color: #fff;
  border-radius: 12rpx;
  padding: 30rpx;
}

.action-title {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 20rpx;
}

.action-list {
  display: flex;
  flex-direction: column;
}

.action-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1px solid #f0f0f0;
}

.action-item:last-child {
  border-bottom: none;
}

.action-item text:first-child {
  font-size: 30rpx;
  color: #333;
}

.action-arrow {
  font-size: 28rpx;
  color: #999;
}

.history-section {
  background-color: #fff;
  border-radius: 12rpx;
  padding: 30rpx;
  margin-top: 20rpx;
}

.section-title {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
  margin-bottom: 20rpx;
}

.history-item {
  background: #f9f9f9;
  border-radius: 8rpx;
  padding: 20rpx;
  margin-bottom: 16rpx;
}

.history-item:last-child {
  margin-bottom: 0;
}

.history-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.history-no {
  font-size: 24rpx;
  color: #999;
}

.history-status {
  font-size: 24rpx;
  color: #67c23a;
}

.history-body {
  margin-bottom: 12rpx;
}

.history-body text {
  font-size: 26rpx;
  color: #333;
}

.history-footer text {
  font-size: 22rpx;
  color: #999;
}
</style>
