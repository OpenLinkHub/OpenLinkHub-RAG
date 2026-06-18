<template>
  <section class="dashboard-grid">
    <article class="metric-card">
      <p>知识库</p>
      <strong>{{ knowledgeBases.length }}</strong>
      <span>已登记 LightRAG 管理对象</span>
    </article>
    <article class="metric-card">
      <p>Endpoint</p>
      <strong>{{ endpoints.length }}</strong>
      <span>LightRAG 运行实例</span>
    </article>
    <article class="metric-card">
      <p>模型配置</p>
      <strong>{{ modelProfiles.length }}</strong>
      <span>可绑定配置档案</span>
    </article>
    <article class="metric-card">
      <p>当前用户</p>
      <strong>{{ user?.roles?.join(', ') }}</strong>
      <span>{{ user?.permissions?.length || 0 }} permissions</span>
    </article>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { request } from '../api/request.js';
import { useAuth } from '../composables/useAuth.js';

const { user } = useAuth();
const knowledgeBases = ref([]);
const endpoints = ref([]);
const modelProfiles = ref([]);

onMounted(async () => {
  [knowledgeBases.value, endpoints.value, modelProfiles.value] = await Promise.all([
    request('/knowledge-bases'),
    request('/endpoints'),
    request('/model-profiles')
  ]);
});
</script>
