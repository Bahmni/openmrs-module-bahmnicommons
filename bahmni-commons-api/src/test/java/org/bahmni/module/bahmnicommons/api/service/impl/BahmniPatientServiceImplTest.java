package org.bahmni.module.bahmnicommons.api.service.impl;


import org.bahmni.module.bahmnicommons.api.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicommons.api.contract.patient.response.PatientConfigResponse;
import org.bahmni.module.bahmnicommons.api.dao.PatientDao;
import org.bahmni.module.bahmnicommons.api.search.builder.PatientResponseBuilder;
import org.bahmni.module.bahmnicommons.api.search.dto.PatientSearchRequest;
import org.bahmni.module.bahmnicommons.api.search.dto.PatientSearchResponse;
import org.bahmni.search.cursor.CursorCodec;
import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.model.PaginationRequest;
import org.bahmni.search.model.SearchCondition;
import org.bahmni.search.model.SearchRequestMeta;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PersonService;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.Silent.class)
public class BahmniPatientServiceImplTest {
    @Mock
    private PersonService personService;
    @Mock
    private ConceptService conceptService;
    @Mock
    private PatientDao patientDao;
    @Mock
    private PatientResponseBuilder patientResponseBuilder;
    @Mock
    private AdministrationService administrationService;

    private BahmniPatientServiceImpl bahmniPatientService;

    @Before
    public void setup() {
        initMocks(this);
        bahmniPatientService = new BahmniPatientServiceImpl(personService, conceptService, patientDao, patientResponseBuilder, administrationService);
    }

    @Test
    public void shouldGetPatientConfig() throws Exception {
        List<PersonAttributeType> personAttributeTypes = new ArrayList<>();
        personAttributeTypes.add(new PersonAttributeType() {{
            this.setName("class");
            this.setDescription("Class");
            this.setFormat("org.openmrs.Concept");
            this.setSortWeight(10.0);
            this.setForeignKey(10);
        }});
        personAttributeTypes.add(new PersonAttributeType() {{
            this.setName("primaryContact");
            this.setDescription("Primary Contact");
            this.setFormat("java.lang.String");
            this.setSortWeight(10.0);
        }});

        when(personService.getAllPersonAttributeTypes()).thenReturn(personAttributeTypes);
        when(conceptService.getConcept(anyInt())).thenReturn(new Concept());

        PatientConfigResponse config = bahmniPatientService.getConfig();
        assertEquals(2, config.getPersonAttributeTypes().size());
        assertEquals("class", config.getPersonAttributeTypes().get(0).getName());
        assertEquals("primaryContact", config.getPersonAttributeTypes().get(1).getName());
    }

    @Test
    public void shouldGetPatientByPartialIdentifier() throws Exception {
        boolean shouldMatchExactPatientId = false;
        bahmniPatientService.get("partial_identifier", shouldMatchExactPatientId);
        verify(patientDao).getPatients("partial_identifier", shouldMatchExactPatientId);
    }

    @Test
    public void shouldCallGetPatientsUsingLuceneSearch() throws Exception {
        String[] addressResultFields = {"city_village"};
        PatientSearchParameters searchParameter = new PatientSearchParameters();
        searchParameter.setIdentifier("100010");
        searchParameter.setName("");
        searchParameter.setAddressFieldName("city_village");
        searchParameter.setAddressFieldValue("");
        searchParameter.setLength(100);
        searchParameter.setStart(0);
        searchParameter.setProgramAttributeFieldValue("");
        searchParameter.setAddressSearchResultFields(addressResultFields);
        searchParameter.setLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");
        searchParameter.setFilterPatientsByLocation(false);
        searchParameter.setFilterOnAllIdentifiers(false);
        searchParameter.setAttributeToFilterOut("filterAttribute");
        searchParameter.setAttributeValueToFilterOut("false");

        bahmniPatientService.luceneSearch(searchParameter);
        verify(patientDao, times(1)).getPatientsUsingLuceneSearch(searchParameter.getIdentifier(),
                searchParameter.getName(),
                searchParameter.getCustomAttribute(),
                searchParameter.getAddressFieldName(),
                searchParameter.getAddressFieldValue(),
                searchParameter.getLength(),
                searchParameter.getStart(),
                searchParameter.getPatientAttributes(),
                searchParameter.getProgramAttributeFieldValue(),
                searchParameter.getProgramAttributeFieldName(),
                searchParameter.getAddressSearchResultFields(),
                searchParameter.getPatientSearchResultFields(),
                searchParameter.getLoginLocationUuid(),
                searchParameter.getFilterPatientsByLocation(),
                searchParameter.getFilterOnAllIdentifiers(),
                searchParameter.getAttributeToFilterOut(),
                searchParameter.getAttributeValueToFilterOut());
    }

    @Test
    public void shouldDelegateToDaoForPatientSearch() {
        PatientSearchRequest request = validSearchRequest();
        mockDaoReturns(Collections.<Patient>emptyList());

        PatientSearchResponse response = bahmniPatientService.search(request);

        verify(patientDao, times(1)).findMatchingIds(
                eq(request.getCriteria()), isNull(Long.class), anyString(), isNull(String.class), eq(101));
        assertThat(response.isSuccess(), is(true));
        assertThat(response.getResults().size(), is(0));
    }

    @Test
    public void shouldDecodeCursorAndPassToDaoForPatientSearch() {
        String cursor = CursorCodec.encode("patient", 50L);

        PatientSearchRequest request = searchRequestWithPagination(10, cursor, "next");
        mockDaoReturns(Collections.<Patient>emptyList());

        bahmniPatientService.search(request);

        verify(patientDao, times(1)).findMatchingIds(
                any(SearchCondition.class), eq(50L), anyString(), eq("next"), eq(11));
    }

    @Test
    public void shouldCapLimitToMax500ForPatientSearch() {
        PatientSearchRequest request = searchRequestWithPagination(1000, null, null);
        mockDaoReturns(Collections.<Patient>emptyList());

        bahmniPatientService.search(request);

        verify(patientDao, times(1)).findMatchingIds(
                any(SearchCondition.class), isNull(Long.class), anyString(), isNull(String.class), eq(501));
    }

    @Test
    public void shouldUseConfiguredDefaultLimitFromGlobalPropertyForPatientSearch() {
        when(administrationService.getGlobalProperty("bahmni.patientSearch.pagination.defaultLimit")).thenReturn("20");
        PatientSearchRequest request = validSearchRequest();
        mockDaoReturns(Collections.<Patient>emptyList());

        bahmniPatientService.search(request);

        verify(patientDao, times(1)).findMatchingIds(
                any(SearchCondition.class), isNull(Long.class), anyString(), isNull(String.class), eq(21));
    }

    @Test
    public void shouldUseConfiguredMaxLimitFromGlobalPropertyForPatientSearch() {
        when(administrationService.getGlobalProperty("bahmni.patientSearch.pagination.maxLimit")).thenReturn("50");
        PatientSearchRequest request = searchRequestWithPagination(1000, null, null);
        mockDaoReturns(Collections.<Patient>emptyList());

        bahmniPatientService.search(request);

        verify(patientDao, times(1)).findMatchingIds(
                any(SearchCondition.class), isNull(Long.class), anyString(), isNull(String.class), eq(51));
    }

    @Test(expected = InvalidSearchCriteriaException.class)
    public void shouldThrowWhenConfiguredMaxLimitGlobalPropertyIsNonPositiveForPatientSearch() {
        when(administrationService.getGlobalProperty("bahmni.patientSearch.pagination.maxLimit")).thenReturn("0");
        PatientSearchRequest request = validSearchRequest();

        bahmniPatientService.search(request);
    }

    @Test
    public void shouldFallbackToDefaultWhenGlobalPropertyIsInvalidForPatientSearch() {
        when(administrationService.getGlobalProperty("bahmni.patientSearch.pagination.defaultLimit")).thenReturn("not-a-number");
        PatientSearchRequest request = validSearchRequest();
        mockDaoReturns(Collections.<Patient>emptyList());

        bahmniPatientService.search(request);

        verify(patientDao, times(1)).findMatchingIds(
                any(SearchCondition.class), isNull(Long.class), anyString(), isNull(String.class), eq(101));
    }

    @Test
    public void shouldReturnNextCursorWhenDaoReturnsMoreThanLimit() {
        PatientSearchRequest request = searchRequestWithPagination(2, null, null);
        Patient p1 = patientWithId(10);
        Patient p2 = patientWithId(9);
        Patient p3 = patientWithId(8); // extra row signals hasMore
        mockDaoReturns(Arrays.asList(p1, p2, p3));
        when(patientResponseBuilder.mapPatient(any(Patient.class))).thenReturn(new HashMap<String, Object>());

        PatientSearchResponse response = bahmniPatientService.search(request);

        assertThat(response.getResults().size(), is(2));
        assertThat(response.getMeta().getPagination().getNextCursor(), is(notNullValue()));
    }

    @Test
    public void shouldReturnNullNextCursorWhenNoMoreResults() {
        PatientSearchRequest request = searchRequestWithPagination(5, null, null);
        Patient p1 = patientWithId(10);
        Patient p2 = patientWithId(9);
        mockDaoReturns(Arrays.asList(p1, p2));
        when(patientResponseBuilder.mapPatient(any(Patient.class))).thenReturn(new HashMap<String, Object>());

        PatientSearchResponse response = bahmniPatientService.search(request);

        assertThat(response.getResults().size(), is(2));
        assertThat(response.getMeta().getPagination().getNextCursor(), is(nullValue()));
    }

    @Test
    public void shouldCountPatientsWhenIncludeTotalCountIsTrue() {
        PatientSearchRequest request = validSearchRequest();
        SearchRequestMeta meta = new SearchRequestMeta();
        meta.setIncludeTotalCount(true);
        request.setMeta(meta);
        mockDaoReturns(Collections.<Patient>emptyList());
        when(patientDao.countPatients(any(SearchCondition.class))).thenReturn(42L);

        PatientSearchResponse response = bahmniPatientService.search(request);

        assertThat(response.getMeta().getTotalCount(), is(42L));
        verify(patientDao, times(1)).countPatients(any(SearchCondition.class));
    }

    @Test
    public void shouldNotCountPatientsWhenIncludeTotalCountNotSet() {
        PatientSearchRequest request = validSearchRequest();
        mockDaoReturns(Collections.<Patient>emptyList());

        PatientSearchResponse response = bahmniPatientService.search(request);

        assertThat(response.getMeta().getTotalCount(), is(nullValue()));
        verify(patientDao, never()).countPatients(any(SearchCondition.class));
    }

    private void mockDaoReturns(List<Patient> patients) {
        List<Integer> matchingIds = new ArrayList<>();
        for (Patient patient : patients) {
            matchingIds.add(patient.getPatientId());
        }
        when(patientDao.findMatchingIds(
                any(SearchCondition.class), any(), anyString(), any(), anyInt()))
                .thenReturn(matchingIds);
        when(patientDao.findByIds(anyList())).thenReturn(patients);
    }

    private Patient patientWithId(int id) {
        Patient p = new Patient();
        p.setPatientId(id);
        return p;
    }

    private PatientSearchRequest validSearchRequest() {
        PatientSearchRequest request = new PatientSearchRequest();
        request.setEntity("patient");
        SearchCondition criteria = new SearchCondition();
        criteria.setField("patient.familyName");
        criteria.setComparator("eq");
        criteria.setValue("Doe");
        request.setCriteria(criteria);
        return request;
    }

    private PatientSearchRequest searchRequestWithPagination(int limit, String cursor, String direction) {
        PatientSearchRequest request = validSearchRequest();
        SearchRequestMeta meta = new SearchRequestMeta();
        PaginationRequest pagination = new PaginationRequest();
        pagination.setLimit(limit);
        pagination.setCursor(cursor);
        pagination.setDirection(direction);
        meta.setPagination(pagination);
        request.setMeta(meta);
        return request;
    }
}
