package org.bahmni.module.bahmnicommons.api.search.validation;

import org.bahmni.module.bahmnicommons.api.search.dto.PatientSearchRequest;
import org.bahmni.search.validation.SearchCriteriaValidationUtils;
import org.springframework.stereotype.Component;

@Component
public class PatientSearchCriteriaValidator {

    public void validateEntity(String entity, String supportedEntity) {
        SearchCriteriaValidationUtils.validateEntity(entity, supportedEntity);
    }

    public void validateRequest(PatientSearchRequest request) {
        SearchCriteriaValidationUtils.validateCriteria(request.getCriteria());
        SearchCriteriaValidationUtils.validateMeta(request.getEntity(), request.getMeta());
    }
}
