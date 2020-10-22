package org.bahmni.module.bahmnicommons.web.v1_0.controller.search;

import org.bahmni.module.bahmnicommons.BaseIntegrationTest;
import org.bahmni.module.bahmnicommons.contract.patient.response.PatientResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.openmrs.*;
import org.openmrs.api.PatientService;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

public class BahmniAppointmentsPatientSearchControllerTest extends BaseIntegrationTest {
    
    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private PatientService patientService;
    
    @Autowired
    private BahmniAppointmentsPatientSearchController bahmniAppointmentsPatientSearchController;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    @Before
    public void setUp() throws Exception {
        bahmniAppointmentsPatientSearchController.setPatientService(patientService);
        List<Patient> patients = new ArrayList<>();
        Patient patient = new Patient();
        patient.setPersonId(1);
        patients.add(patient);
        when(patientService.getPatients(Matchers.anyString())).thenReturn(patients);
    }
    
    @Test
    public void shouldSearchByIdentifier() {
        when(httpServletRequest.getParameter("q")).thenReturn("GAN200001");
        bahmniAppointmentsPatientSearchController.search(httpServletRequest, httpServletResponse);
        verify(patientService, times(1)).getPatients("GAN200001");
    }
    
    @Test
    public void shouldSearchByName() {
        when(httpServletRequest.getParameter("q")).thenReturn("John");
        ResponseEntity<AlreadyPaged<PatientResponse>> response = bahmniAppointmentsPatientSearchController.search(httpServletRequest, httpServletResponse);
        List<PatientResponse> patients = response.getBody().getPageOfResults();
        verify(patientService, times(1)).getPatients("John");
    }

    @Test
    public void shouldFailWithUnsupportedSearchParameter() {
        when(httpServletRequest.getParameter("customAttribute")).thenReturn("testCustomAttribute");
        exceptionRule.expect(ResponseException.class);
        exceptionRule.expectMessage("An unsupported search parameter was provided.");
        bahmniAppointmentsPatientSearchController.search(httpServletRequest, httpServletResponse);
    }
}
