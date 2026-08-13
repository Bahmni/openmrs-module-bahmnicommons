package org.bahmni.module.bahmnicommons.api.search.dto;

import java.util.Date;

public class SearchResponseMeta {

    private final Date timestamp;

    public SearchResponseMeta() {
        this.timestamp = new Date();
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
