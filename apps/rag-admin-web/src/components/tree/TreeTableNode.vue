<template>
  <template v-if="ancestorExpanded">
    <div class="table-row" :class="rowClass">
      <slot
        name="row"
        :node="node"
        :depth="depth"
        :has-children="hasChildren"
        :expanded="expanded"
        :toggle="() => $emit('toggle', node[rowKey])"
      />
      <span class="row-actions">
        <slot name="actions" :node="node" :depth="depth" />
      </span>
    </div>
    <template v-if="hasChildren && expanded">
      <TreeTableNode
        v-for="child in node[childrenKey]"
        :key="child[rowKey]"
        :node="child"
        :children-key="childrenKey"
        :row-key="rowKey"
        :row-class="rowClass"
        :depth="depth + 1"
        :expanded-ids="expandedIds"
        :ancestor-expanded="true"
        @toggle="$emit('toggle', $event)"
      >
        <template #row="slotProps">
          <slot name="row" v-bind="slotProps" />
        </template>
        <template #actions="slotProps">
          <slot name="actions" v-bind="slotProps" />
        </template>
      </TreeTableNode>
    </template>
  </template>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  node: { type: Object, required: true },
  childrenKey: { type: String, required: true },
  rowKey: { type: String, required: true },
  rowClass: { type: String, default: '' },
  depth: { type: Number, required: true },
  expandedIds: { type: Object, required: true },
  ancestorExpanded: { type: Boolean, default: true }
});

defineEmits(['toggle']);

const children = computed(() => props.node[props.childrenKey] || []);
const hasChildren = computed(() => children.value.length > 0);
const expanded = computed(() => props.expandedIds.has(props.node[props.rowKey]));
</script>
