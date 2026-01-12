<template>
  <view class="task-container">
    <view class="tabs">
      <view 
        v-for="tab in tabs" 
        :key="tab.value" 
        class="tab-item" 
        :class="{ active: currentTab === tab.value }"
        @click="switchTab(tab.value)"
      >
        <text>{{ tab.label }}</text>
        <view v-if="getCount(tab.value) > 0" class="badge">{{ getCount(tab.value) }}</view>
      </view>
    </view>

    <view class="order-list">
      <view v-if="loading" class="loading">
        <text>加载中...</text>
      </view>

      <view v-else-if="orderList.length === 0" class="empty">
        <text>暂无工单</text>
      </view>

      <view v-else>
        <view 
          v-for="order in orderList" 
          :key="order.id" 
          class="order-item"
          @click="goToDetail(order)"
        >
          <view class="order-header">
            <view class="order-info">
              <text class="order-type" :class="order.orderType === 1 ? 'type-repair' : 'type-maintenance'">
                {{ order.orderType === 1 ? '维修' : '保养' }}
              </text>
              <text class="order-no">{{ order.orderNo }}</text>
            </view>
            <text class="status" :class="'status-' + order.status">{{ order.statusName }}</text>
          </view>
          <view class="order-body">
            <text class="facility-name">{{ order.facilityName }}</text>
            <text class="fault-desc">{{ order.faultDescription }}</text>
          </view>
          <view class="order-footer">
            <text class="time">{{ order.createTime }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getMyOrders } from '../../api/workorder.js'

export default {
  data() {
    return {
      currentTab: 'pending',
      tabs: [
        { label: '待接单', value: 'pending', statusList: [1] },
        { label: '处理中', value: 'processing', statusList: [2] },
        { label: '待验收', value: 'submitted', statusList: [3] },
        { label: '已完成', value: 'completed', statusList: [4, 5] }
      ],
      orderList: [],
      allOrders: [],
      loading: false
    }
  },
  onLoad() {
    const token = uni.getStorageSync('token')
    if (!token) {
      uni.reLaunch({ url: '/pages/login/login' })
    }
  },
  onShow() {
    this.loadOrders()
  },
  methods: {
    async loadOrders() {
      this.loading = true
      try {
        const res = await getMyOrders()
        this.allOrders = res.data || []
        this.filterOrders()
      } catch (err) {
        console.error('加载工单失败', err)
      } finally {
        this.loading = false
      }
    },
    switchTab(tabValue) {
      this.currentTab = tabValue
      this.filterOrders()
    },
    filterOrders() {
      const tab = this.tabs.find(t => t.value === this.currentTab)
      if (tab) {
        this.orderList = this.allOrders.filter(o => tab.statusList.includes(o.status))
      }
    },
    getCount(tabValue) {
      const tab = this.tabs.find(t => t.value === tabValue)
      if (tab) {
        return this.allOrders.filter(o => tab.statusList.includes(o.status)).length
      }
      return 0
    },
    goToDetail(order) {
      uni.navigateTo({
        url: `/pages/workorder/detail?id=${order.id}`
      })
    }
  }
}
</script>

<style scoped>
.task-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.tabs {
  display: flex;
  background: #fff;
  padding: 0 20rpx;
  border-bottom: 1px solid #eee;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  font-size: 28rpx;
  color: #666;
  position: relative;
}

.tab-item.active {
  color: #409eff;
  font-weight: bold;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60rpx;
  height: 4rpx;
  background: #409eff;
  border-radius: 2rpx;
}

.badge {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  min-width: 32rpx;
  height: 32rpx;
  background: #f56c6c;
  color: #fff;
  font-size: 20rpx;
  border-radius: 16rpx;
  text-align: center;
  line-height: 32rpx;
  padding: 0 8rpx;
}

.order-list {
  padding: 20rpx;
}

.loading, .empty {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
}

.order-item {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.order-info {
  display: flex;
  align-items: center;
}

.order-type {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
  margin-right: 12rpx;
}

.type-repair {
  background: #fef0f0;
  color: #f56c6c;
}

.type-maintenance {
  background: #fdf6ec;
  color: #e6a23c;
}

.order-no {
  font-size: 26rpx;
  color: #999;
}

.status {
  font-size: 24rpx;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
}

.status-1 { background: #fdf6ec; color: #e6a23c; }
.status-2 { background: #ecf5ff; color: #409eff; }
.status-3 { background: #fdf6ec; color: #e6a23c; }
.status-4 { background: #f0f9eb; color: #67c23a; }
.status-5 { background: #f4f4f5; color: #909399; }

.order-body {
  margin-bottom: 16rpx;
}

.facility-name {
  display: block;
  font-size: 30rpx;
  color: #333;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.fault-desc {
  display: block;
  font-size: 26rpx;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-footer {
  border-top: 1px solid #f0f0f0;
  padding-top: 16rpx;
}

.time {
  font-size: 24rpx;
  color: #999;
}
</style>
