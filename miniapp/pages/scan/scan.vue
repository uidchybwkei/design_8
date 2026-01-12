<template>
  <view class="scan-container">
    <view class="scan-header">
      <text class="title">扫码识别设施</text>
      <text class="desc">扫描设施二维码查看详情</text>
    </view>

    <view class="scan-btn-area">
      <button type="primary" class="scan-btn" @click="handleScan">
        扫描二维码
      </button>
    </view>

    <view class="manual-input">
      <text class="input-label">或手动输入设施编码</text>
      <view class="input-row">
        <input 
          type="text" 
          v-model="manualCode" 
          placeholder="请输入设施编码" 
          class="code-input"
        />
        <button type="default" size="mini" @click="handleManualQuery">查询</button>
      </view>
    </view>

    <view class="recent-scan" v-if="recentList.length > 0">
      <text class="recent-title">最近扫码</text>
      <view class="recent-list">
        <view 
          v-for="item in recentList" 
          :key="item.code" 
          class="recent-item"
          @click="goToDetail(item.code)"
        >
          <text class="recent-name">{{ item.name }}</text>
          <text class="recent-code">{{ item.code }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      manualCode: '',
      recentList: []
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
    this.loadRecentList()
  },
  onShow() {
    this.loadRecentList()
  },
  methods: {
    loadRecentList() {
      const list = uni.getStorageSync('recentScanList') || []
      this.recentList = list.slice(0, 5)
    },
    handleScan() {
      uni.scanCode({
        onlyFromCamera: false,
        scanType: ['qrCode'],
        success: (res) => {
          const code = res.result
          if (code) {
            this.goToDetail(code)
          }
        },
        fail: (err) => {
          if (err.errMsg && err.errMsg.indexOf('cancel') === -1) {
            uni.showToast({ title: '扫码失败', icon: 'none' })
          }
        }
      })
    },
    handleManualQuery() {
      const code = this.manualCode.trim()
      if (!code) {
        uni.showToast({ title: '请输入设施编码', icon: 'none' })
        return
      }
      this.goToDetail(code)
    },
    goToDetail(code) {
      uni.navigateTo({
        url: `/pages/facility/detail?code=${encodeURIComponent(code)}`
      })
    },
    saveToRecent(code, name) {
      let list = uni.getStorageSync('recentScanList') || []
      list = list.filter(item => item.code !== code)
      list.unshift({ code, name, time: Date.now() })
      list = list.slice(0, 10)
      uni.setStorageSync('recentScanList', list)
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

.scan-header {
  text-align: center;
  padding: 60rpx 0;
}

.title {
  display: block;
  font-size: 36rpx;
  color: #333;
  margin-bottom: 16rpx;
}

.desc {
  display: block;
  font-size: 26rpx;
  color: #999;
}

.scan-btn-area {
  padding: 40rpx 20rpx;
}

.scan-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 32rpx;
}

.manual-input {
  padding: 40rpx 20rpx;
  border-top: 1px solid #f0f0f0;
}

.input-label {
  display: block;
  font-size: 26rpx;
  color: #999;
  margin-bottom: 20rpx;
}

.input-row {
  display: flex;
  align-items: center;
}

.code-input {
  flex: 1;
  height: 72rpx;
  border: 1px solid #ddd;
  border-radius: 8rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  margin-right: 20rpx;
}

.recent-scan {
  padding: 40rpx 20rpx;
  border-top: 1px solid #f0f0f0;
}

.recent-title {
  display: block;
  font-size: 26rpx;
  color: #999;
  margin-bottom: 20rpx;
}

.recent-list {
  background-color: #f9f9f9;
  border-radius: 8rpx;
}

.recent-item {
  display: flex;
  justify-content: space-between;
  padding: 24rpx 20rpx;
  border-bottom: 1px solid #eee;
}

.recent-item:last-child {
  border-bottom: none;
}

.recent-name {
  font-size: 28rpx;
  color: #333;
}

.recent-code {
  font-size: 24rpx;
  color: #999;
}
</style>
