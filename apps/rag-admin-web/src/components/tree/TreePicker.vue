<template>
  <div class="tree-picker">
    <button
      type="button"
      class="tree-picker-option"
      :class="{ active: modelValue == null }"
      @click="select(null)"
    >
      {{ rootLabel }}
    </button>
    <TreePickerNode
      v-for="node in filteredNodes"
      :key="node[rowKey]"
      :node="node"
      :children-key="childrenKey"
      :row-key="rowKey"
      :label-key="labelKey"
      :model-value="modelValue"
      :disabled-ids="disabledIds"
      :depth="0"
      @select="select"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue';
import TreePickerNode from './TreePickerNode.vue';
import { collectExcludedIds } from '../../utils/system.js';

const props = defineProps({
  modelValue: { type: [Number, String, null], default: null },
  nodes: { type: Array, default: () => [] },
  childrenKey: { type: String, default: 'children' },
  rowKey: { type: String, default: 'id' },
  labelKey: { type: String, default: 'name' },
  excludeId: { type: [Number, String, null], default: null },
  rootLabel: { type: String, default: '无（顶级）' }
});

const emit = defineEmits(['update:modelValue']);

const disabledIds = computed(() => collectExcludedIds(props.nodes, props.excludeId, props.childrenKey, props.rowKey));
const filteredNodes = computed(() => props.nodes || []);

function select(value) {
  emit('update:modelValue', value);
}
</script>
