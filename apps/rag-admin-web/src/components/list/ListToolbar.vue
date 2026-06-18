<template>
  <div class="list-toolbar">
    <label class="search-field">
      <span>{{ label }}</span>
      <input
        :value="keyword"
        type="search"
        :placeholder="placeholder"
        @input="$emit('update:keyword', $event.target.value)"
        @keydown.enter.prevent="$emit('search')"
      />
    </label>
    <div class="list-toolbar-actions">
      <button type="button" @click="$emit('search')">搜索</button>
      <button type="button" class="ghost" @click="$emit('reset')">重置</button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  keyword: { type: String, default: '' },
  label: { type: String, default: '关键词' },
  placeholder: { type: String, default: '请输入关键词搜索' }
});
defineEmits(['update:keyword', 'search', 'reset']);
</script>
