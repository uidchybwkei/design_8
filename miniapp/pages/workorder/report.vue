<template>
  <view class="report-container">
    <view class="facility-info" v-if="facility">
      <text class="label">已选设施</text>
      <view class="facility-card">
        <text class="name">{{ facility.facilityName }}</text>
        <text class="code">{{ facility.facilityCode }}</text>
      </view>
    </view>

    <view class="form-section">
      <view class="form-item">
        <text class="label">故障描述 <text class="required">*</text></text>
        <textarea 
          v-model="form.faultDescription" 
          placeholder="请详细描述故障情况..."
          maxlength="500"
          class="textarea"
        />
        <text class="count">{{ form.faultDescription.length }}/500</text>
      </view>

      <view class="form-item">
        <text class="label">故障图片（可选）</text>
        <view class="image-list">
          <view v-for="(img, idx) in imageList" :key="idx" class="image-item">
            <image :src="img.path" mode="aspectFill" class="preview-img" />
            <view class="remove-btn" @click="removeImage(idx)">×</view>
          </view>
          <view v-if="imageList.length < 3" class="add-image" @click="chooseImage">
            <text class="add-icon">+</text>
            <text class="add-text">添加图片</text>
          </view>
        </view>
      </view>
    </view>

    <view class="submit-section">
      <button type="primary" :loading="submitting" @click="handleSubmit" class="submit-btn">
        提交故障上报
      </button>
    </view>
  </view>
</template>

<script>
import { getFacilityByCode } from '../../api/facility.js'
import { reportFault, uploadFile } from '../../api/workorder.js'

export default {
  data() {
    return {
      facilityCode: '',
      facility: null,
      form: {
        faultDescription: ''
      },
      imageList: [],
      submitting: false
    }
  },
  onLoad(options) {
    if (options.facilityCode) {
      this.facilityCode = options.facilityCode
      this.loadFacility()
    } else {
      uni.showToast({ title: '缺少设施信息', icon: 'none' })
      setTimeout(() => uni.navigateBack(), 1500)
    }
  },
  methods: {
    async loadFacility() {
      try {
        const res = await getFacilityByCode(this.facilityCode)
        this.facility = res.data
      } catch (err) {
        uni.showToast({ title: err.message || '获取设施失败', icon: 'none' })
        setTimeout(() => uni.navigateBack(), 1500)
      }
    },
    chooseImage() {
      uni.chooseImage({
        count: 3 - this.imageList.length,
        sizeType: ['compressed'],
        sourceType: ['camera', 'album'],
        success: (res) => {
          res.tempFilePaths.forEach(path => {
            this.imageList.push({ path, uploaded: false, url: '' })
          })
        }
      })
    },
    removeImage(idx) {
      this.imageList.splice(idx, 1)
    },
    async handleSubmit() {
      if (!this.form.faultDescription.trim()) {
        uni.showToast({ title: '请填写故障描述', icon: 'none' })
        return
      }

      this.submitting = true
      try {
        let faultImages = []
        for (let img of this.imageList) {
          if (!img.uploaded) {
            const res = await uploadFile(img.path)
            img.url = res.fileUrl
            img.uploaded = true
          }
          faultImages.push(img.url)
        }

        await reportFault({
          facilityId: this.facility.id,
          faultDescription: this.form.faultDescription,
          faultImages: JSON.stringify(faultImages)
        })

        uni.showToast({ title: '上报成功', icon: 'success' })
        setTimeout(() => {
          uni.switchTab({ url: '/pages/task/task' })
        }, 1500)
      } catch (err) {
        uni.showToast({ title: err.message || '上报失败', icon: 'none' })
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.report-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20rpx;
}

.facility-info {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.facility-info .label {
  font-size: 26rpx;
  color: #999;
  display: block;
  margin-bottom: 16rpx;
}

.facility-card {
  background: #f9f9f9;
  border-radius: 8rpx;
  padding: 20rpx;
}

.facility-card .name {
  display: block;
  font-size: 30rpx;
  color: #333;
  font-weight: bold;
}

.facility-card .code {
  display: block;
  font-size: 24rpx;
  color: #666;
  margin-top: 8rpx;
}

.form-section {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
}

.form-item {
  margin-bottom: 30rpx;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-item .label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 16rpx;
}

.required {
  color: #f56c6c;
}

.textarea {
  width: 100%;
  height: 200rpx;
  background: #f9f9f9;
  border-radius: 8rpx;
  padding: 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.count {
  display: block;
  text-align: right;
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
}

.image-item {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  margin-right: 20rpx;
  margin-bottom: 20rpx;
}

.preview-img {
  width: 100%;
  height: 100%;
  border-radius: 8rpx;
}

.remove-btn {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  width: 40rpx;
  height: 40rpx;
  background: #f56c6c;
  color: #fff;
  border-radius: 50%;
  text-align: center;
  line-height: 36rpx;
  font-size: 28rpx;
}

.add-image {
  width: 160rpx;
  height: 160rpx;
  background: #f9f9f9;
  border: 2rpx dashed #ddd;
  border-radius: 8rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.add-icon {
  font-size: 48rpx;
  color: #999;
}

.add-text {
  font-size: 22rpx;
  color: #999;
  margin-top: 8rpx;
}

.submit-section {
  margin-top: 40rpx;
  padding: 0 20rpx;
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 32rpx;
}
</style>
