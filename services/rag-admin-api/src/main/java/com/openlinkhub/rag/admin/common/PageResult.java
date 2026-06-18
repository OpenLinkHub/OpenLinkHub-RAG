package com.openlinkhub.rag.admin.common;

import java.util.List;

public final class PageResult<T> {

    private final List<T> items;
    private final long total;
    private final int page;
    private final int pageSize;
    private final int totalPages;

    public PageResult(List<T> items, long total, int page, int pageSize) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = pageSize <= 0 ? 0 : (int) Math.ceil((double) total / pageSize);
    }

    public List<T> getItems() {
        return items;
    }

    public long getTotal() {
        return total;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
