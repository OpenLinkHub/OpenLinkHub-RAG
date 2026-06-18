<template>
  <div class="tree-picker-branch">
    <button
      type="button"
      class="tree-picker-option"
      :class="{ active: modelValue === node[rowKey], disabled: disabledIds.has(node[rowKey]) }"
      :disabled="disabledIds.has(node[rowKey])"
      :style="{ paddingLeft: `${depth * 18 + 12}px` }"
      @click="!disabledIds.has(node[rowKey]) && $emit('select', node[rowKey])"
    >
      <span v-if="hasChildren" class="tree-picker-marker">▸</span>
      {{ node[labelKey] }}
    </button>
    <TreePickerNode
      v-for="child in node[childrenKey] || []"
      :key="child[rowKey]"
      :node="child"
      :children-key="childrenKey"
      :row-key="rowKey"
      :label-key="labelKey"
      :model-value="modelValue"
      :disabled-ids="disabledIds"
      :depth="depth + 1"
      @select="$emit('select', $event)"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  node: { type: Object, required: true },
  childrenKey: { type: String, required: true },
  rowKey: { type: String, required: true },
  labelKey: { type: String, required: true },
  modelValue: { type: [Number, String, null], default: null },
  disabledIds: { type: Object, required: true },
  depth: { type: Number, required: true }
});

defineEmits(['select']);

const hasChildren = computed(() => (props.node[props.childrenKey] || []).length > 0);
</script>
