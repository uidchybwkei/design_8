<script>
export default {
  onLaunch: function() {
    console.log('App Launch')
    const token = uni.getStorageSync('token')
    if (!token) {
      uni.reLaunch({
        url: '/pages/login/login'
      })
    }
  },
  onShow: function() {
    console.log('App Show')
  },
  onHide: function() {
    console.log('App Hide')
  }
}
</script>

<style>
@import './static/css/common.css';
</style>
