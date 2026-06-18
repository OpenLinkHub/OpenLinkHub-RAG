<template>
  <div class="list-pagination">
    <div class="list-pagination-meta">
      共 {{ total }} 条，第 {{ page }} / {{ Math.max(totalPages, 1) }} 页
    </div>
    <div class="list-pagination-controls">
      <label>
        每页
        <select :value="pageSize" @change="$emit('change-page-size', Number($event.target.value))">
          <option v-for="size in pageSizes" :key="size" :value="size">{{ size }}</option>
        </select>
      </label>
      <button type="button" :disabled="page <= 1" @click="$emit('change-page', page - 1)">上一页</button>
      <button type="button" :disabled="page >= totalPages || totalPages === 0" @click="$emit('change-page', page + 1)">下一页</button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  page: { type: Number, required: true },
  pageSize: { type: Number, required: true },
  total: { type: Number, required: true },
  totalPages: { type: Number, required: true },
  pageSizes: { type: Array, default: () => [10, 20, 50] }
});
defineEmits(['change-page', 'change-page-size']);
</script>
