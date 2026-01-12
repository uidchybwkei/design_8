<template>
  <view class="mine-container">
    <view class="user-info" v-if="userInfo">
      <text class="name">{{ userInfo.realName }}</text>
      <text class="phone">{{ userInfo.phone || '未绑定' }}</text>
    </view>

    <view class="stats-section" v-if="stats">
      <view class="stats-title">我的工作统计</view>
      <view class="stats-cards">
        <view class="stat-card">
          <text class="stat-value">{{ stats.totalCount || 0 }}</text>
          <text class="stat-label">总工单</text>
        </view>
        <view class="stat-card success">
          <text class="stat-value">{{ stats.completedCount || 0 }}</text>
          <text class="stat-label">已完成</text>
        </view>
        <view class="stat-card warning">
          <text class="stat-value">{{ stats.pendingCount || 0 }}</text>
          <text class="stat-label">待处理</text>
        </view>
      </view>
      <view class="avg-duration">
        <text class="duration-label">平均处理耗时：</text>
        <text class="duration-value">{{ stats.avgDurationDisplay || '-' }}</text>
      </view>
    </view>

    <view class="logout-section">
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </view>
  </view>
</template>

<script>
import { getUserInfo } from '../../api/auth.js'
import { getMyStats } from '../../api/stats.js'

export default {
  data() {
    return {
      userInfo: null,
      stats: null
    }
  },
  onLoad() {
    const token = uni.getStorageSync('token')
    if (!token) {
      uni.reLaunch({
        url: '/pages/login/login'
      })
      return
    }
    this.loadUserInfo()
    this.loadStats()
  },
  methods: {
    async loadUserInfo() {
      try {
        const res = await getUserInfo()
        this.userInfo = res.data
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    },
    async loadStats() {
      try {
        const res = await getMyStats()
        this.stats = res.data
      } catch (error) {
        console.error('获取统计数据失败:', error)
      }
    },
    handleLogout() {
      uni.showModal({
        title: '提示',
        content: '确定退出登录？',
        success: (res) => {
          if (res.confirm) {
            uni.removeStorageSync('token')
            uni.removeStorageSync('userInfo')
            uni.reLaunch({
              url: '/pages/login/login'
            })
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.mine-container {
  min-height: 100vh;
  background-color: #fff;
  padding: 40rpx;
}

.user-info {
  padding: 60rpx 20rpx;
  border-bottom: 1px solid #eee;
  margin-bottom: 40rpx;
}

.name {
  font-size: 32rpx;
  color: #333;
  display: block;
  margin-bottom: 12rpx;
}

.phone {
  font-size: 28rpx;
  color: #999;
  display: block;
}

.logout-section {
  padding: 0 20rpx;
}

.logout-btn {
  width: 100%;
  height: 80rpx;
  line-height: 80rpx;
  background-color: #fff;
  color: #f56c6c;
  border: 1px solid #f56c6c;
  font-size: 30rpx;
}

.stats-section {
  background: #f8f9fa;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 40rpx;
}

.stats-title {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 20rpx;
}

.stats-cards {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.stat-card {
  flex: 1;
  text-align: center;
  background: #fff;
  border-radius: 12rpx;
  padding: 20rpx 10rpx;
  margin: 0 8rpx;
}

.stat-card:first-child { margin-left: 0; }
.stat-card:last-child { margin-right: 0; }

.stat-value {
  display: block;
  font-size: 40rpx;
  font-weight: bold;
  color: #409eff;
}

.stat-card.success .stat-value { color: #67c23a; }
.stat-card.warning .stat-value { color: #e6a23c; }

.stat-label {
  display: block;
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
}

.avg-duration {
  background: #fff;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
  display: flex;
  justify-content: space-between;
}

.duration-label {
  font-size: 26rpx;
  color: #666;
}

.duration-value {
  font-size: 26rpx;
  color: #409eff;
  font-weight: bold;
}
</style>
