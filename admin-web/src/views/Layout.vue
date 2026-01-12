<template>
  <el-container class="layout-container">
    <el-aside width="220px">
      <div class="logo">
        <h3>港航维保系统</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        :router="true"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/home">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/facility">
          <el-icon><OfficeBuilding /></el-icon>
          <span>设施管理</span>
        </el-menu-item>
        <el-menu-item index="/workorder">
          <el-icon><Document /></el-icon>
          <span>工单管理</span>
        </el-menu-item>
        <el-menu-item index="/maintenance">
          <el-icon><Calendar /></el-icon>
          <span>保养计划</span>
        </el-menu-item>
        <el-menu-item index="/inventory">
          <el-icon><Box /></el-icon>
          <span>库存管理</span>
        </el-menu-item>
        <el-menu-item index="/stats">
          <el-icon><DataAnalysis /></el-icon>
          <span>统计分析</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header>
        <div class="header-right">
          <span class="username">{{ userInfo?.realName || userInfo?.username }}</span>
          <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const userInfo = ref(null)
const activeMenu = computed(() => route.path)

onMounted(async () => {
  try {
    userInfo.value = await userStore.getUserInfo()
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
})

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.layout-container {
  width: 100%;
  height: 100vh;
}

.el-aside {
  background-color: #304156;
  height: 100vh;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b3a4a;
}

.logo h3 {
  color: #fff;
  margin: 0;
  font-size: 18px;
}

.el-header {
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.username {
  color: #606266;
  font-size: 14px;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
