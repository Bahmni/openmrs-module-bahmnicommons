package org.bahmni.module.bahmnicommons.api.service.impl;


import org.bahmni.module.bahmnicommons.api.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicommons.api.contract.patient.response.PatientConfigResponse;
import org.bahmni.module.bahmnicommons.api.dao.PatientDao;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PersonService;


import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class BahmniPatientServiceImplTest {
    @Mock
    private PersonService personService;
    @Mock
    private ConceptService conceptService;
    @Mock
    private PatientDao patientDao;

    private BahmniPatientServiceImpl bahmniPatientService;

    @Before
    public void setup() {
        initMocks(this);
        bahmniPatientService = new BahmniPatientServiceImpl(personService, conceptService, patientDao);
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
}
