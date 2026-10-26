package com.example.marketplace.dto.product;

import java.util.List;

public class ProductPageResponse {

    private List<ProductResponse> content;
    private Integer page;
    private Integer pageSize;
    private Long totalElement;
    private Integer totalPages;

    public ProductPageResponse(List<ProductResponse> content, Integer page, Integer totalPages, Integer pageSize, Long totalElement) {
        this.content = content;
        this.page = page;
        this.totalPages = totalPages;
        this.pageSize = pageSize;
        this.totalElement = totalElement;
    }

    public ProductPageResponse() {
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public List<ProductResponse> getContent() {
        return content;
    }

    public void setContent(List<ProductResponse> content) {
        this.content = content;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Long getTotalElement() {
        return totalElement;
    }

    public void setTotalElement(Long totalElement) {
        this.totalElement = totalElement;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
}
