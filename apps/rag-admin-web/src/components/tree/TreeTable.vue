<template>
  <div class="tree-table">
    <div class="table-row table-head" :class="rowClass">
      <slot name="head" />
      <span>操作</span>
    </div>
    <TreeTableNode
      v-for="node in nodes"
      :key="node[rowKey]"
      :node="node"
      :children-key="childrenKey"
      :row-key="rowKey"
      :row-class="rowClass"
      :depth="0"
      :expanded-ids="expandedIds"
      @toggle="toggleExpand"
    >
      <template #row="slotProps">
        <slot name="row" v-bind="slotProps" />
      </template>
      <template #actions="slotProps">
        <slot name="actions" v-bind="slotProps" />
      </template>
    </TreeTableNode>
    <div v-if="!nodes?.length" class="table-empty">暂无数据</div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import TreeTableNode from './TreeTableNode.vue';
import { flattenTree } from '../../utils/system.js';

const props = defineProps({
  nodes: { type: Array, default: () => [] },
  childrenKey: { type: String, default: 'children' },
  rowKey: { type: String, default: 'id' },
  rowClass: { type: String, default: '' }
});

const expandedIds = ref(new Set());

watch(
  () => props.nodes,
  (nodes) => {
    expandedIds.value = new Set(flattenTree(nodes, props.childrenKey).map((n) => n[props.rowKey]));
  },
  { immediate: true, deep: true }
);

function toggleExpand(id) {
  const next = new Set(expandedIds.value);
  if (next.has(id)) next.delete(id);
  else next.add(id);
  expandedIds.value = next;
}
</script>
