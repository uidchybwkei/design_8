<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>系统概览</span>
      </template>
      <div class="placeholder">
        <el-empty description="首页功能待后续里程碑实现" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.placeholder {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
