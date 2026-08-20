package org.bahmni.module.bahmnicommons.api.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.bahmni.search.model.SearchResponseMeta;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatientSearchResponse {

    private final String context;
    private final SearchResponseMeta meta;
    private final List<Map<String, Object>> results;
    private final SearchError error;

    private PatientSearchResponse(String context, SearchResponseMeta meta,
                                   List<Map<String, Object>> results, SearchError error) {
        this.context = context;
        this.meta = meta;
        this.results = results;
        this.error = error;
    }

    public static PatientSearchResponse success(String context, List<Map<String, Object>> results) {
        return new PatientSearchResponse(context, new SearchResponseMeta(), results, null);
    }

    public static PatientSearchResponse success(String context, List<Map<String, Object>> results,
                                                 SearchResponseMeta meta) {
        return new PatientSearchResponse(context, meta, results, null);
    }

    public static PatientSearchResponse error(String context, int status, List<String> messages) {
        return new PatientSearchResponse(context, new SearchResponseMeta(),
                Collections.emptyList(), new SearchError(status, messages));
    }

    public static PatientSearchResponse error(String context, int status, String message) {
        return error(context, status, Collections.singletonList(message));
    }

    public String getContext() {
        return context;
    }

    public SearchResponseMeta getMeta() {
        return meta;
    }

    public List<Map<String, Object>> getResults() {
        return results;
    }

    public SearchError getError() {
        return error;
    }

    public boolean isSuccess() {
        return error == null;
    }
}
