package com.baito.my_app.common.domain;

import java.util.List;
import java.util.function.Function;

/**
 * Framework-free page envelope for application ports, so domain services and inbound ports do not
 * depend on Spring Data types.
 */
public record PageResult<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public <R> PageResult<R> map(Function<T, R> mapper) {
        return new PageResult<>(content.stream().map(mapper).toList(), page, size, totalElements, totalPages);
    }
}
