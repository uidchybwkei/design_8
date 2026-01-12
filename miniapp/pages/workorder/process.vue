<template>
  <view class="process-container">
    <view class="form-section">
      <view class="form-item">
        <text class="label">处理说明 <text class="required">*</text></text>
        <textarea 
          v-model="form.processDescription" 
          placeholder="请详细描述处理过程和结果..."
          maxlength="1000"
          class="textarea"
        />
      </view>

      <view class="form-item">
        <text class="label">处理图片 <text class="required">*</text> (至少1张)</text>
        <view class="image-list">
          <view v-for="(img, idx) in imageList" :key="idx" class="image-item">
            <image :src="img.path" mode="aspectFill" class="preview-img" />
            <view class="remove-btn" @click="removeImage(idx)">×</view>
          </view>
          <view v-if="imageList.length < 9" class="add-image" @click="chooseImage">
            <text class="add-icon">+</text>
            <text class="add-text">添加图片</text>
          </view>
        </view>
      </view>

    </view>

    <view class="submit-section">
      <button type="primary" :loading="submitting" @click="handleSubmit" class="submit-btn">
        提交处理结果
      </button>
    </view>
  </view>
</template>

<script>
import { submitOrder, uploadFile } from '../../api/workorder.js'

export default {
  data() {
    return {
      orderId: null,
      form: {
        processDescription: ''
      },
      imageList: [],
      submitting: false
    }
  },
  onLoad(options) {
    this.orderId = options.id
  },
  methods: {
    chooseImage() {
      uni.chooseImage({
        count: 9 - this.imageList.length,
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
      if (!this.form.processDescription.trim()) {
        uni.showToast({ title: '请填写处理说明', icon: 'none' })
        return
      }
      if (this.imageList.length === 0) {
        uni.showToast({ title: '请至少上传1张处理图片', icon: 'none' })
        return
      }

      this.submitting = true
      try {
        uni.showLoading({ title: '上传图片中...' })
        let processImages = []
        for (let img of this.imageList) {
          if (!img.uploaded) {
            const res = await uploadFile(img.path)
            img.url = res.fileUrl
            img.uploaded = true
          }
          processImages.push(img.url)
        }
        uni.hideLoading()

        await submitOrder(this.orderId, {
          processDescription: this.form.processDescription,
          processImages: JSON.stringify(processImages)
        })

        uni.showToast({ title: '提交成功', icon: 'success' })
        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
      } catch (err) {
        uni.hideLoading()
        uni.showToast({ title: err.message || '提交失败', icon: 'none' })
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.process-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20rpx;
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
  height: 240rpx;
  background: #f9f9f9;
  border-radius: 8rpx;
  padding: 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
}

.image-item {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  margin-right: 16rpx;
  margin-bottom: 16rpx;
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
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 32rpx;
}
</style>
