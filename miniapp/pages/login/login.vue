<template>
  <view style="padding: 100rpx 40rpx; background-color: #fff; min-height: 100vh;">
    <view style="text-align: center; margin-bottom: 120rpx;">
      <text style="display: block; font-size: 32rpx; color: #333; margin-bottom: 20rpx;">港航设施维修保养系统</text>
      <text style="display: block; font-size: 28rpx; color: #999;">微信端</text>
    </view>

    <view style="padding: 0 20rpx;">
      <button 
        type="primary" 
        @click="handleLogin"
        :loading="loading"
        style="width: 100%; height: 80rpx; font-size: 30rpx;"
      >
        微信登录
      </button>

      <view style="margin-top: 30rpx; text-align: center; font-size: 24rpx; color: #999;">
        <text>测试环境使用 mock openid</text>
      </view>
    </view>
  </view>
</template>

<script>
import { wxLogin } from '../../api/auth.js'

export default {
  data() {
    return {
      loading: false
    }
  },
  methods: {
    async handleLogin() {
      this.loading = true
      try {
        const mockCode = 'wx001'
        const res = await wxLogin(mockCode)
        
        uni.setStorageSync('token', res.data.token)
        uni.setStorageSync('userInfo', res.data)
        
        uni.showToast({
          title: '登录成功',
          icon: 'success'
        })
        
        setTimeout(() => {
          uni.switchTab({
            url: '/pages/task/task'
          })
        }, 1000)
      } catch (error) {
        console.error('登录失败:', error)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>
