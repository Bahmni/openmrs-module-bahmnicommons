package org.bahmni.module.bahmnicommons.web.v1_0.controller.search;

import org.bahmni.module.bahmnicommons.api.search.dto.PatientSearchRequest;
import org.bahmni.module.bahmnicommons.api.search.dto.PatientSearchResponse;
import org.bahmni.module.bahmnicommons.api.search.validation.PatientSearchCriteriaValidator;
import org.bahmni.module.bahmnicommons.api.service.BahmniPatientService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

@Controller("bahmniCommonsPatientSearchController")
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/patient")
public class PatientController {

    private static final String SUPPORTED_ENTITY = "patient";

    public static final String CURRENT_ENTITY_ATTRIBUTE = "patientSearch.currentEntity";

    private final BahmniPatientService bahmniPatientService;
    private final PatientSearchCriteriaValidator criteriaValidator;

    @Autowired
    public PatientController(BahmniPatientService bahmniPatientService,
                             PatientSearchCriteriaValidator criteriaValidator) {
        this.bahmniPatientService = bahmniPatientService;
        this.criteriaValidator = criteriaValidator;
    }

    @PostMapping(
            value = "/search",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<PatientSearchResponse> search(@RequestBody PatientSearchRequest request,
                                                        WebRequest webRequest) {
        String entity = request.getEntity();
        criteriaValidator.validateEntity(entity, SUPPORTED_ENTITY);
        webRequest.setAttribute(CURRENT_ENTITY_ATTRIBUTE, entity, RequestAttributes.SCOPE_REQUEST);
        criteriaValidator.validateRequest(request);
        PatientSearchResponse response = bahmniPatientService.search(request);
        return ResponseEntity.ok(response);
    }
}
