<script setup lang="ts">
</script>

<template>
  <div class="page-container">
    <el-card>
      <div class="empty-state">
        <div class="empty-state-icon">❌</div>
        <div class="empty-state-text">页面不存在</div>
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-state-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state-text {
  font-size: 16px;
  color: var(--text-secondary);
  margin-bottom: 24px;
}
</style>
