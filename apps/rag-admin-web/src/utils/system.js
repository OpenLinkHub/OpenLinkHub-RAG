export function flattenTree(nodes, childKey = 'children', depth = 0, acc = []) {
  for (const node of nodes || []) {
    acc.push({ ...node, depth });
    flattenTree(node[childKey] || [], childKey, depth + 1, acc);
  }
  return acc;
}

export function normalizeMulti(value) {
  if (Array.isArray(value)) return value.map(Number);
  return value ? [Number(value)] : [];
}

export function collectExcludedIds(nodes, excludeId, childKey = 'children', idKey = 'id') {
  const excluded = new Set();
  if (excludeId == null) return excluded;

  function walk(list, block) {
    for (const node of list || []) {
      const id = node[idKey];
      const children = node[childKey] || [];
      if (block) {
        excluded.add(id);
        walk(children, true);
      } else if (id === excludeId) {
        excluded.add(id);
        walk(children, true);
      } else {
        walk(children, false);
      }
    }
  }

  walk(nodes, false);
  return excluded;
}

export function buildQuery(params) {
  const query = {};
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      query[key] = String(value);
    }
  });
  return query;
}
