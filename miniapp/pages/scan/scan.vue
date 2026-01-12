<template>
  <view class="scan-container">
    <view class="placeholder">
      <text class="placeholder-text">扫码识别</text>
      <text class="placeholder-desc">里程碑2实现</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {}
  },
  onLoad() {
    const token = uni.getStorageSync('token')
    if (!token) {
      uni.reLaunch({
        url: '/pages/login/login'
      })
    }
  }
}
</script>

<style scoped>
.scan-container {
  padding: 40rpx;
  background-color: #fff;
  min-height: 100vh;
}

.placeholder {
  text-align: center;
  padding: 120rpx 40rpx;
}

.placeholder-text {
  font-size: 32rpx;
  color: #333;
  display: block;
  margin-bottom: 20rpx;
}

.placeholder-desc {
  font-size: 28rpx;
  color: #999;
  display: block;
}
</style>
