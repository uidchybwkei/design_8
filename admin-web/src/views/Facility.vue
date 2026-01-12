<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>设施管理</span>
      </template>
      <div class="placeholder">
        <el-empty description="设施管理功能将在里程碑2实现" />
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
