<template>
  <view class="detail-container">
    <view v-if="loading" class="loading">
      <text>加载中...</text>
    </view>

    <view v-else-if="order" class="order-content">
      <view class="status-header" :class="'status-' + order.status">
        <text class="status-text">{{ order.statusName }}</text>
      </view>

      <view class="info-card">
        <view class="card-title">工单信息</view>
        <view class="info-row">
          <text class="label">工单编号</text>
          <text class="value">{{ order.orderNo }}</text>
        </view>
        <view class="info-row">
          <text class="label">设施名称</text>
          <text class="value">{{ order.facilityName }}</text>
        </view>
        <view class="info-row">
          <text class="label">故障描述</text>
          <text class="value">{{ order.faultDescription }}</text>
        </view>
        <view class="info-row">
          <text class="label">上报人</text>
          <text class="value">{{ order.reporterName }}</text>
        </view>
        <view class="info-row">
          <text class="label">上报时间</text>
          <text class="value">{{ order.reportTime }}</text>
        </view>
      </view>

      <view class="info-card" v-if="order.status >= 2">
        <view class="card-title">处理信息</view>
        <view class="info-row" v-if="order.acceptTime">
          <text class="label">接单时间</text>
          <text class="value">{{ order.acceptTime }}</text>
        </view>
        <view class="info-row" v-if="order.processDescription">
          <text class="label">处理说明</text>
          <text class="value">{{ order.processDescription }}</text>
        </view>
        <view class="info-row" v-if="order.submitTime">
          <text class="label">提交时间</text>
          <text class="value">{{ order.submitTime }}</text>
        </view>
        <view v-if="processImages.length > 0" class="images-section">
          <text class="label">处理图片</text>
          <view class="image-list">
            <image v-for="(img, idx) in processImages" :key="idx" :src="getImageUrl(img)" 
              mode="aspectFill" class="process-img" @click="previewImage(img)" />
          </view>
        </view>
      </view>

      <view class="action-section">
        <button v-if="order.status === 1 && isAssignee" type="primary" @click="handleAccept" :loading="actionLoading">
          接单
        </button>

        <button v-if="order.status === 2 && isAssignee" type="primary" @click="goToProcess">
          填写处理记录
        </button>

        <button v-if="order.status === 2 && isAssignee" type="default" @click="scanConfirm">
          到场扫码确认
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import { getOrderById, acceptOrder } from '../../api/workorder.js'

export default {
  data() {
    return {
      orderId: null,
      order: null,
      loading: true,
      actionLoading: false,
      currentUserId: null
    }
  },
  computed: {
    isAssignee() {
      return this.order && this.order.assigneeId === this.currentUserId
    },
    processImages() {
      if (!this.order || !this.order.processImages) return []
      try {
        return JSON.parse(this.order.processImages)
      } catch {
        return []
      }
    }
  },
  onLoad(options) {
    this.orderId = options.id
    const userId = uni.getStorageSync('userId')
    this.currentUserId = userId ? Number(userId) : null
    this.loadOrder()
  },
  onShow() {
    if (this.orderId) {
      this.loadOrder()
    }
  },
  methods: {
    async loadOrder() {
      this.loading = true
      try {
        const res = await getOrderById(this.orderId)
        this.order = res.data
        console.log('工单详情:', this.order)
        console.log('assigneeId:', this.order.assigneeId, 'type:', typeof this.order.assigneeId)
        console.log('currentUserId:', this.currentUserId, 'type:', typeof this.currentUserId)
        console.log('isAssignee:', this.isAssignee)
      } catch (err) {
        uni.showToast({ title: err.message || '加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    async handleAccept() {
      this.actionLoading = true
      try {
        await acceptOrder(this.orderId)
        uni.showToast({ title: '接单成功', icon: 'success' })
        this.loadOrder()
      } catch (err) {
        uni.showToast({ title: err.message || '接单失败', icon: 'none' })
      } finally {
        this.actionLoading = false
      }
    },
    scanConfirm() {
      uni.scanCode({
        onlyFromCamera: false,
        scanType: ['qrCode'],
        success: (res) => {
          if (res.result === this.order.facilityCode) {
            uni.showToast({ title: '设施确认成功', icon: 'success' })
          } else {
            uni.showToast({ title: '设施编码不匹配', icon: 'none' })
          }
        }
      })
    },
    goToProcess() {
      uni.navigateTo({
        url: `/pages/workorder/process?id=${this.orderId}`
      })
    },
    getImageUrl(url) {
      if (url.startsWith('http')) return url
      return 'http://localhost:8080' + url
    },
    previewImage(url) {
      uni.previewImage({
        current: this.getImageUrl(url),
        urls: this.processImages.map(img => this.getImageUrl(img))
      })
    }
  }
}
</script>

<style scoped>
.detail-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.loading {
  padding: 200rpx;
  text-align: center;
  color: #999;
}

.status-header {
  padding: 40rpx;
  text-align: center;
  color: #fff;
}

.status-0 { background: #909399; }
.status-1 { background: #e6a23c; }
.status-2 { background: #409eff; }
.status-3 { background: #e6a23c; }
.status-4 { background: #67c23a; }
.status-5 { background: #909399; }

.status-text {
  font-size: 36rpx;
  font-weight: bold;
}

.info-card {
  background: #fff;
  margin: 20rpx;
  border-radius: 12rpx;
  padding: 24rpx;
}

.card-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  padding-bottom: 16rpx;
  border-bottom: 1px solid #f0f0f0;
}

.info-row {
  display: flex;
  padding: 16rpx 0;
}

.info-row .label {
  width: 160rpx;
  color: #999;
  font-size: 28rpx;
  flex-shrink: 0;
}

.info-row .value {
  flex: 1;
  color: #333;
  font-size: 28rpx;
}

.images-section {
  margin-top: 20rpx;
}

.images-section .label {
  display: block;
  color: #999;
  font-size: 28rpx;
  margin-bottom: 16rpx;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
}

.process-img {
  width: 150rpx;
  height: 150rpx;
  border-radius: 8rpx;
  margin-right: 16rpx;
  margin-bottom: 16rpx;
}

.action-section {
  padding: 40rpx 20rpx;
}

.action-section button {
  margin-bottom: 20rpx;
}
</style>
