package org.bahmni.module.bahmnicommons.api.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatientSearchResponse {

    private final String context;
    private final SearchResponseMeta metaData;
    private final List<Map<String, Object>> results;
    private final List<Map<String, String>> links;
    private final SearchError error;

    private PatientSearchResponse(String context, List<Map<String, Object>> results,
                                   List<Map<String, String>> links, SearchError error) {
        this.context = context;
        this.metaData = new SearchResponseMeta();
        this.results = results;
        this.links = links;
        this.error = error;
    }

    public static PatientSearchResponse success(String context, List<Map<String, Object>> results) {
        return new PatientSearchResponse(context, results, Collections.emptyList(), null);
    }

    public static PatientSearchResponse error(String context, int status, List<String> messages) {
        return new PatientSearchResponse(context, Collections.emptyList(), Collections.emptyList(),
                new SearchError(status, messages));
    }

    public static PatientSearchResponse error(String context, int status, String message) {
        return error(context, status, Collections.singletonList(message));
    }

    public String getContext() {
        return context;
    }

    public SearchResponseMeta getMetaData() {
        return metaData;
    }

    public List<Map<String, Object>> getResults() {
        return results;
    }

    public List<Map<String, String>> getLinks() {
        return links;
    }

    public SearchError getError() {
        return error;
    }

    public boolean isSuccess() {
        return error == null;
    }
}
