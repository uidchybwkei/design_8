<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>工单管理</span>
      </template>
      <div class="placeholder">
        <el-empty description="工单管理功能将在里程碑3实现" />
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
