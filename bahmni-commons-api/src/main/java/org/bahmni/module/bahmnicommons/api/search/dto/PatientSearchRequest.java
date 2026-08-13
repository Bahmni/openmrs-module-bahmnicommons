package org.bahmni.module.bahmnicommons.api.search.dto;

import org.bahmni.search.model.SearchCondition;

public class PatientSearchRequest {

    private String entity;
    private SearchCondition criteria;

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
}
