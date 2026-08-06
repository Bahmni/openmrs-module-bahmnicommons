package org.bahmni.module.bahmnicommons.web.v1_0.controller.search;

import org.bahmni.module.bahmnicommons.api.search.dto.PatientSearchRequest;
import org.bahmni.module.bahmnicommons.api.search.dto.PatientSearchResponse;
import org.bahmni.module.bahmnicommons.api.search.validation.PatientSearchCriteriaValidator;
import org.bahmni.module.bahmnicommons.api.service.BahmniPatientService;
import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.exceptions.SearchResponseErrorStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class PatientControllerTest {

    private static final String SUPPORTED_ENTITY = "patient";

    @Mock
    private BahmniPatientService bahmniPatientService;

    @Mock
    private PatientSearchCriteriaValidator criteriaValidator;

    @InjectMocks
    private PatientController patientController;

    private WebRequest webRequest;

    @Before
    public void setUp() {
        initMocks(this);
        webRequest = new ServletWebRequest(new MockHttpServletRequest());
    }

    @Test
    public void shouldValidateEntityAndReturnSuccessResponseOnSearch() {
        PatientSearchRequest request = new PatientSearchRequest();
        request.setEntity(SUPPORTED_ENTITY);
        PatientSearchResponse expectedResponse =
                PatientSearchResponse.success(SUPPORTED_ENTITY, Collections.emptyList());
        when(bahmniPatientService.search(request)).thenReturn(expectedResponse);

        ResponseEntity<PatientSearchResponse> response = patientController.search(request, webRequest);

        verify(criteriaValidator, times(1)).validateEntity(SUPPORTED_ENTITY, SUPPORTED_ENTITY);
        verify(bahmniPatientService, times(1)).search(request);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedResponse, response.getBody());
        assertEquals(SUPPORTED_ENTITY, webRequest.getAttribute(
                PatientController.CURRENT_ENTITY_ATTRIBUTE, WebRequest.SCOPE_REQUEST));
    }

    @Test(expected = InvalidSearchCriteriaException.class)
    public void shouldPropagateExceptionAndNotInvokeServiceWhenEntityValidationFails() {
        PatientSearchRequest request = new PatientSearchRequest();
        request.setEntity("invalidEntity");
        doThrowOnValidateEntity("invalidEntity");

        try {
            patientController.search(request, webRequest);
        } finally {
            verify(bahmniPatientService, never()).search(any(PatientSearchRequest.class));
        }
    }

    private void doThrowOnValidateEntity(String entity) {
        org.mockito.Mockito.doThrow(new InvalidSearchCriteriaException(
                        "Entity '" + entity + "' is not supported. Supported entities: [" + SUPPORTED_ENTITY + "]",
                        SearchResponseErrorStatus.BAD_REQUEST))
                .when(criteriaValidator).validateEntity(entity, SUPPORTED_ENTITY);
    }
}
