<template>
  <section class="section-grid">
    <div class="section-header">
      <div>
        <h2>知识库</h2>
        <p>知识库绑定 LightRAG endpoint，用于授权、文档管理和查询测试。</p>
      </div>
      <button type="button" @click="createKnowledgeBase">创建样例知识库</button>
    </div>
    <p v-if="errorMessage" class="error-line">{{ errorMessage }}</p>
    <div class="table">
      <div class="table-row table-head">
        <span>名称</span>
        <span>编码</span>
        <span>Endpoint</span>
        <span>查询模式</span>
        <span>模型状态</span>
      </div>
      <div v-for="kb in knowledgeBases" :key="kb.id" class="table-row">
        <span>{{ kb.name }}</span>
        <span>{{ kb.code }}</span>
        <span>{{ endpointName(kb.endpointId) }}</span>
        <span>{{ kb.defaultQueryMode }}</span>
        <span>{{ kb.pendingRestart ? '待重启' : '已生效' }}</span>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { request } from '../api/request.js';

const knowledgeBases = ref([]);
const endpoints = ref([]);
const errorMessage = ref('');

onMounted(loadData);

async function loadData() {
  errorMessage.value = '';
  try {
    [knowledgeBases.value, endpoints.value] = await Promise.all([
      request('/knowledge-bases'),
      request('/endpoints')
    ]);
  } catch (error) {
    errorMessage.value = error.message;
  }
}

async function createKnowledgeBase() {
  errorMessage.value = '';
  try {
    const timestamp = Date.now().toString().slice(-5);
    await request('/knowledge-bases', {
      method: 'POST',
      body: {
        name: `样例知识库 ${timestamp}`,
        code: `sample-${timestamp}`,
        endpointId: endpoints.value[0]?.id || 'endpoint-local',
        defaultQueryMode: 'mix'
      }
    });
    knowledgeBases.value = await request('/knowledge-bases');
  } catch (error) {
    errorMessage.value = error.message;
  }
}

function endpointName(endpointId) {
  return endpoints.value.find((endpoint) => endpoint.id === endpointId)?.name || endpointId;
}
</script>
