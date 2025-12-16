package edu.univ.erp.api.common;

import java.util.Collections;
import java.util.List;

public record PagedResult<T>(
        List<T> items,
        int page,
        int pageSize,
        long totalItems
) {
    public PagedResult {
        if (page < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Invalid pagination arguments");
        }
        items = Collections.unmodifiableList(items);
    }

    public long totalPages() {
        if (pageSize == 0) {
            return 0;
        }
        return (long) Math.ceil((double) totalItems / pageSize);
    }
}

