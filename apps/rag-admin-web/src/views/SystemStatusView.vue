<template>
  <section class="section-grid">
    <div class="section-header">
      <div>
        <h2>系统状态</h2>
        <p>检查管理系统登记的 LightRAG endpoint 健康状态。</p>
      </div>
    </div>

    <p v-if="errorMessage" class="error-line">{{ errorMessage }}</p>

    <div class="endpoint-grid">
      <article v-for="endpoint in endpoints" :key="endpoint.id" class="endpoint-card">
        <h3>{{ endpoint.name }}</h3>
        <p>{{ endpoint.baseUrl }}</p>
        <button type="button" @click="checkHealth(endpoint.id)">健康检查</button>
      </article>
    </div>
    <pre class="json-panel">{{ healthJson }}</pre>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { request } from '../api/request.js';

const errorMessage = ref('');
const endpoints = ref([]);
const healthJson = ref('');

onMounted(async () => {
  try {
    endpoints.value = await request('/endpoints');
  } catch (error) {
    errorMessage.value = error.message;
  }
});

async function checkHealth(endpointId) {
  errorMessage.value = '';
  try {
    healthJson.value = JSON.stringify(await request(`/system/endpoints/${endpointId}/health`), null, 2);
  } catch (error) {
    errorMessage.value = error.message;
  }
}
</script>
