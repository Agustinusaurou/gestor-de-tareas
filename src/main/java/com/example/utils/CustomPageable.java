package com.example.utils;

import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;

public class CustomPageable implements Pageable {
    private final int offset;
    private final int limit;

    public CustomPageable(int offset, int limit) {
        this.offset = offset;
        this.limit = limit;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return Sort.unsorted();
    }

    @Override
    public int getSize() {
        return limit;
    }

    @Override
    public int getNumber() {
        return offset / limit;
    }
}
