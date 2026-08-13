package org.bahmni.module.bahmnicommons.api.search.dto;

import java.util.Collections;
import java.util.List;

public class SearchError {

    private final int status;
    private final List<String> messages;

    public SearchError(int status, List<String> messages) {
        this.status = status;
        this.messages = Collections.unmodifiableList(messages);
    }

    public SearchError(int status, String message) {
        this(status, Collections.singletonList(message));
    }

    public int getStatus() {
        return status;
    }

    public List<String> getMessages() {
        return messages;
    }
}
