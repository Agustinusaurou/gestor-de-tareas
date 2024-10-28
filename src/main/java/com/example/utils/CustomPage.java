package com.example.utils;

import io.micronaut.data.model.Page;
import java.util.List;
import io.micronaut.data.model.Pageable;

public class CustomPage<T> implements Page<T> {
    private final List<T> content;
    private final long totalSize;
    private final Pageable pageable;

    public CustomPage(List<T> content, Pageable pageable, long totalElements) {
        this.content = content;
        this.pageable = pageable;
        this.totalSize = totalElements;
    }

    @Override
    public List<T> getContent() {
        return content;
    }

    @Override
    public Pageable getPageable() {
        return pageable;
    }

    @Override
    public int getSize() {
        return pageable.getSize();
    }

    @Override
    public int getTotalPages() {
        return ((int)totalSize + pageable.getSize() - 1) / pageable.getSize();
    }


    @Override
    public long getTotalSize() {
        return totalSize;
    }

    @Override
    public boolean hasTotalSize() {
        return true;
    }
}
