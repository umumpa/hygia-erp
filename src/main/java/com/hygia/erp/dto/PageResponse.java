package com.hygia.erp.dto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

import java.util.List;

public class PageResponse<T> {
    public List<T> content;
    public int page;
    public int size;
    public long total;
    public int totalPages;

    public PageResponse(Page<T> pageData) {
        this(
            pageData.getContent(),
            pageData.getNumber(),
            pageData.getSize(),
            pageData.getTotalElements(),
            pageData.getTotalPages()
        );
    }

    /** 主构造函数 */
    public PageResponse(List<T> content, int page, int size, long total, int totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = totalPages;
    }


    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }


    public static <T> PageResponse<T> of(Slice<T> slice) {
        return new PageResponse<>(
            slice.getContent(),
            slice.getNumber(),
            slice.getSize(),
            -1L,
            -1
        );
    }
}