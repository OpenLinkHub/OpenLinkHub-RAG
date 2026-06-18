<template>
  <section class="section-grid">
    <div class="section-header">
      <div>
        <h2>模型配置</h2>
        <p>MVP 保存配置档案并绑定知识库，切换后提示重启 LightRAG。</p>
      </div>
      <button type="button" @click="switchDefaultModel">绑定默认配置</button>
    </div>

    <p v-if="errorMessage" class="error-line">{{ errorMessage }}</p>

    <div class="table model-table">
      <div class="table-row table-head">
        <span>名称</span>
        <span>LLM</span>
        <span>Embedding</span>
        <span>状态</span>
      </div>
      <div v-for="profile in modelProfiles" :key="profile.id" class="table-row">
        <span>{{ profile.name }}</span>
        <span>{{ profile.llmBinding }} / {{ profile.llmModel }}</span>
        <span>{{ profile.embeddingBinding }} / {{ profile.embeddingModel }}</span>
        <span>{{ profile.status }}</span>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { request } from '../api/request.js';

const errorMessage = ref('');
const knowledgeBases = ref([]);
const modelProfiles = ref([]);

onMounted(async () => {
  [knowledgeBases.value, modelProfiles.value] = await Promise.all([
    request('/knowledge-bases'),
    request('/model-profiles')
  ]);
});

async function switchDefaultModel() {
  errorMessage.value = '';
  try {
    const kbId = knowledgeBases.value[0]?.id || 'kb-default';
    await request(`/knowledge-bases/${kbId}/model-profile`, {
      method: 'POST',
      body: { modelProfileId: modelProfiles.value[0]?.id || 'model-default' }
    });
    knowledgeBases.value = await request('/knowledge-bases');
  } catch (error) {
    errorMessage.value = error.message;
  }
}
</script>
