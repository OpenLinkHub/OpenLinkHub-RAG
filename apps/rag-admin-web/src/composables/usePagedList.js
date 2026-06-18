import { ref } from 'vue';

export function usePagedList(loadFn, initialPageSize = 10) {
  const items = ref([]);
  const keyword = ref('');
  const page = ref(1);
  const pageSize = ref(initialPageSize);
  const total = ref(0);
  const totalPages = ref(0);
  const loading = ref(false);
  const error = ref('');

  async function load() {
    loading.value = true;
    error.value = '';
    try {
      const result = await loadFn({
        page: page.value,
        pageSize: pageSize.value,
        keyword: keyword.value.trim() || undefined
      });
      items.value = result.items || [];
      total.value = result.total || 0;
      totalPages.value = result.totalPages || 0;
      if (totalPages.value > 0 && page.value > totalPages.value) {
        page.value = totalPages.value;
        return load();
      }
    } catch (err) {
      error.value = err.message;
      items.value = [];
    } finally {
      loading.value = false;
    }
  }

  function search() {
    page.value = 1;
    return load();
  }

  function reset() {
    keyword.value = '';
    page.value = 1;
    return load();
  }

  function changePage(nextPage) {
    page.value = nextPage;
    return load();
  }

  function changePageSize(nextSize) {
    pageSize.value = nextSize;
    page.value = 1;
    return load();
  }

  return {
    items,
    keyword,
    page,
    pageSize,
    total,
    totalPages,
    loading,
    error,
    load,
    search,
    reset,
    changePage,
    changePageSize
  };
}
