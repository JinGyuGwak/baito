package com.baito.my_app.common.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.function.Function;

/**
 * Framework-free page envelope for application ports, so domain services and inbound ports do not
 * depend on Spring Data types.
 */
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class PageResult<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public <R> PageResult<R> map(Function<T, R> mapper) {
        return new PageResult<>(content.stream().map(mapper).toList(), page, size, totalElements, totalPages);
    }
}
