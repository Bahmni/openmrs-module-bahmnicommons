package org.bahmni.module.bahmnicommons.api.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.bahmni.search.model.PaginationResponse;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResponseMeta {

    private final long timestamp;
    private final Long totalCount;
    private final PaginationResponse pagination;

    public SearchResponseMeta() {
        this.timestamp = System.currentTimeMillis();
        this.totalCount = null;
        this.pagination = null;
    }

    public SearchResponseMeta(PaginationResponse pagination, Long totalCount) {
        this.timestamp = System.currentTimeMillis();
        this.pagination = pagination;
        this.totalCount = totalCount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public PaginationResponse getPagination() {
        return pagination;
    }
}
