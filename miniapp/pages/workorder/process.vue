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

      <view class="form-item">
        <text class="label">消耗备件 (可选)</text>
        <view class="spare-parts-section">
          <view v-for="(part, idx) in selectedParts" :key="idx" class="part-item">
            <picker :range="inventoryItems" range-key="itemName" @change="e => onPartChange(e, idx)" class="part-picker">
              <view class="picker-display">
                <text class="part-name">{{ part.itemName || '选择备件' }}</text>
                <text class="picker-arrow">▼</text>
              </view>
            </picker>
            <input type="number" v-model="part.quantity" placeholder="数量" class="quantity-input" />
            <view class="remove-part-btn" @click="removePart(idx)">删除</view>
          </view>
          <view class="add-part-btn" @click="addPart">
            <text class="add-icon">+</text> 添加备件
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
import { getItemList, addConsumption } from '../../api/inventory.js'

export default {
  data() {
    return {
      orderId: null,
      form: {
        processDescription: ''
      },
      imageList: [],
      inventoryItems: [],
      selectedParts: [],
      submitting: false
    }
  },
  onLoad(options) {
    this.orderId = options.id
    this.loadInventoryItems()
  },
  methods: {
    async loadInventoryItems() {
      try {
        const res = await getItemList()
        this.inventoryItems = res.data.filter(item => item.status === 1)
      } catch (err) {
        console.error('加载备件列表失败', err)
      }
    },
    addPart() {
      this.selectedParts.push({ itemId: null, itemName: '', quantity: 1 })
    },
    removePart(idx) {
      this.selectedParts.splice(idx, 1)
    },
    onPartChange(e, idx) {
      const item = this.inventoryItems[e.detail.value]
      this.selectedParts[idx].itemId = item.id
      this.selectedParts[idx].itemName = item.itemName
    },
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

        for (let part of this.selectedParts) {
          if (part.itemId && part.quantity > 0) {
            try {
              await addConsumption({
                orderId: this.orderId,
                itemId: part.itemId,
                quantity: parseInt(part.quantity)
              })
            } catch (err) {
              console.error('关联备件失败:', err)
            }
          }
        }

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

.spare-parts-section {
  background: #f9f9f9;
  border-radius: 8rpx;
  padding: 16rpx;
}

.part-item {
  display: flex;
  align-items: center;
  background: #fff;
  padding: 16rpx;
  margin-bottom: 16rpx;
  border-radius: 8rpx;
}

.part-picker {
  flex: 1;
  margin-right: 16rpx;
}

.picker-display {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 16rpx;
  background: #f5f5f5;
  border-radius: 6rpx;
}

.part-name {
  font-size: 28rpx;
  color: #333;
}

.picker-arrow {
  font-size: 20rpx;
  color: #999;
}

.quantity-input {
  width: 120rpx;
  height: 60rpx;
  background: #f5f5f5;
  border-radius: 6rpx;
  text-align: center;
  font-size: 28rpx;
  margin-right: 16rpx;
}

.remove-part-btn {
  padding: 8rpx 16rpx;
  background: #f56c6c;
  color: #fff;
  font-size: 24rpx;
  border-radius: 6rpx;
}

.add-part-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24rpx;
  background: #fff;
  border: 2rpx dashed #ddd;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #409eff;
}

.add-part-btn .add-icon {
  font-size: 32rpx;
  margin-right: 8rpx;
}
</style>
