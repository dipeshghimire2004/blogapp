package org.blogapp.dg_blogapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {

    private List<T> content;

    private int pageNo;

    private int pageSize;

    private long totalElements;

    private int totalPages;

    private Boolean first;

    private Boolean last;

    public PageResponse(Page<?> page, List<T> content) {
        this.content = content;
        this.pageNo = page.getNumber() + 1;
        this.pageSize = page.getSize();
        this.totalElements= page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.first = page.isFirst();
        this.last = page.isLast();
    }


}
