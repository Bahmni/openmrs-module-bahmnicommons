package org.bahmni.module.bahmnicommons.api.search.dto;

import org.bahmni.search.model.SearchCondition;
import org.bahmni.search.model.SearchRequestMeta;

public class PatientSearchRequest {

    private String entity;
    private SearchCondition criteria;
    private SearchRequestMeta meta;

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public SearchCondition getCriteria() {
        return criteria;
    }

    public void setCriteria(SearchCondition criteria) {
        this.criteria = criteria;
    }

    public SearchRequestMeta getMeta() {
        return meta;
    }

    public void setMeta(SearchRequestMeta meta) {
        this.meta = meta;
    }
}
