<template>
  <view class="mine-container">
    <view class="user-info" v-if="userInfo">
      <text class="name">{{ userInfo.realName }}</text>
      <text class="phone">{{ userInfo.phone || '未绑定' }}</text>
    </view>

    <view class="logout-section">
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </view>
  </view>
</template>

<script>
import { getUserInfo } from '../../api/auth.js'

export default {
  data() {
    return {
      userInfo: null
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
</style>
