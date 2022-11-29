package org.bahmni.module.bahmnicommons.api.contract.patient.search;

public class QueryParam {
    private String paramName;
    private Object paramValue;

    public QueryParam(String paramName, Object paramValue) {
        this.paramName = paramName;
        this.paramValue = paramValue;
    }

    public Object getParamValue() {
        return paramValue;
    }

    public String getParamName() {
        return paramName;
    }
}
