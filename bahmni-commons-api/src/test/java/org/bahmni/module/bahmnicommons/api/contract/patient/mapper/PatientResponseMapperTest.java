package org.bahmni.module.bahmnicommons.api.contract.patient.mapper;

import org.bahmni.module.bahmnicommons.api.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicommons.api.visitlocation.BahmniVisitLocationServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.openmrs.*;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
@PowerMockIgnore("javax.management.*")
public class PatientResponseMapperTest {

    private PatientResponseMapper patientResponseMapper;

    @Mock
    VisitService visitService;

    @Mock
    BahmniVisitLocationServiceImpl bahmniCommonsVisitLocationService;

    @Mock
    ConceptService conceptService;

    Patient patient;

    @Before
    public void setUp() throws Exception {
        patient = new Patient();
        Location location = new Location(1);
        PowerMockito.mockStatic(Context.class);
        Visit visit = new Visit(1);
        visit.setUuid("someLocationUUid");
        visit.setLocation(location);
        List<Visit> visits = new ArrayList<>();
        visits.add(visit);
        PowerMockito.when(visitService.getActiveVisitsByPatient(patient)).thenReturn(visits);
        PowerMockito.when(Context.getVisitService()).thenReturn(visitService);
        PowerMockito.when(bahmniCommonsVisitLocationService.getVisitLocation(eq(null))).thenReturn(location);

        patientResponseMapper = new PatientResponseMapper(Context.getVisitService(), bahmniCommonsVisitLocationService);
        patient.setPatientId(12);
        PatientIdentifier primaryIdentifier = new PatientIdentifier("FAN007", new PatientIdentifierType(), new Location(1));
        PatientIdentifier extraIdentifier = new PatientIdentifier("Extra009", new PatientIdentifierType(), new Location(1));
        extraIdentifier.getIdentifierType().setName("test");
        primaryIdentifier.setPreferred(true);
        patient.setIdentifiers(Sets.newSet(primaryIdentifier, extraIdentifier));

    }

    @Test
    public void shouldMapPatientBasicDetails() throws Exception {
        patient.setBirthdate(new Date(2000000l));
        patient.setUuid("someUUid");

        PatientResponse patientResponse = patientResponseMapper.map(patient, null, null, null, null);

        Assert.assertEquals(patientResponse.getPersonId(), 12);
        Assert.assertEquals(patientResponse.getBirthDate().getTime(), 2000000l);
        Assert.assertEquals(patientResponse.getAge(), "54");
        Assert.assertEquals(patientResponse.getUuid(), "someUUid");
        Assert.assertEquals(patientResponse.getIdentifier(), "FAN007");
        Assert.assertEquals(patientResponse.getExtraIdentifiers(), "{\"test\" : \"Extra009\"}");
    }

    @Test
    public void shouldMapPersonAttributes() throws Exception {
        PersonAttributeType personAttributeType = new PersonAttributeType();
        personAttributeType.setName("givenNameLocal");
        patient.setAttributes(Sets.newSet(new PersonAttribute(personAttributeType,"someName")));
        String[] patientResultFields = {"givenNameLocal"};
        PatientResponse patientResponse = patientResponseMapper.map(patient, null, patientResultFields, null, null);

        Assert.assertEquals(patientResponse.getCustomAttribute(),"{\"givenNameLocal\" : \"someName\"}");
    }

    @Test
    public void shouldMapPersonAttributesForConceptType() throws Exception {
        PersonAttributeType personAttributeType = new PersonAttributeType();
        personAttributeType.setName("occupation");
        personAttributeType.setFormat("org.openmrs.Concept");
        patient.setAttributes(Sets.newSet(new PersonAttribute(personAttributeType,"100")));
        String[] patientResultFields = {"occupation"};
        Concept concept = new Concept();
        ConceptName conceptName = new ConceptName();
        conceptName.setName("FSN");
        Locale defaultLocale = new Locale("en", "GB");
        conceptName.setLocale(defaultLocale);
        concept.setFullySpecifiedName(conceptName);
        conceptName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
        PowerMockito.mockStatic(Context.class);
        PowerMockito.when(Context.getLocale()).thenReturn(defaultLocale);

        when(Context.getConceptService()).thenReturn(conceptService);
        PowerMockito.when(conceptService.getConcept("100")).thenReturn(concept);

        PatientResponse patientResponse = patientResponseMapper.map(patient, null, patientResultFields, null, null);

        Assert.assertEquals(patientResponse.getCustomAttribute(),"{\"occupation\" : \"FSN\"}");
    }

    @Test
    public void shouldAddSlashToSupportSpecialCharactersInJSON() throws Exception {
        PersonAttributeType personAttributeType = new PersonAttributeType();
        personAttributeType.setName("familyNameLocal");
        patient.setAttributes(Sets.newSet(new PersonAttribute(personAttributeType,"so\"me\\Name")));
        String[] patientResultFields = {"familyNameLocal"};
        PatientResponse patientResponse = patientResponseMapper.map(patient, null, patientResultFields, null, null);

        Assert.assertEquals(patientResponse.getCustomAttribute(),"{\"familyNameLocal\" : \"so\\\"me\\\\Name\"}");
    }

    @Test
    public void shouldMapPatientAddress() throws Exception {
        PersonAddress personAddress= new PersonAddress(2);
        personAddress.setAddress2("someAddress");
        patient.setAddresses(Sets.newSet(personAddress));

        PatientResponse patientResponse = patientResponseMapper.map(patient, null, null, new String[]{"address_2"}, null);
        Assert.assertEquals(patientResponse.getAddressFieldValue(),"{\"address_2\" : \"someAddress\"}");

    }

    @Test
    public void shouldMapVisitSummary() throws Exception {

        PatientResponse patientResponse = patientResponseMapper.map(patient, null, null, null, null);
        Assert.assertEquals(patientResponse.getActiveVisitUuid(),"someLocationUUid");
        Assert.assertEquals(patientResponse.getHasBeenAdmitted(), Boolean.FALSE);
    }

    @Test
    public void shouldReturnBirthDateAsNullWhenBirthDateIsNotSet() {
        PatientResponse patient = new PatientResponse();
        Assert.assertNull(patient.getAge());
    }

    @Test
    public void shouldReturnBirthDateWhenBirthDateIsSet() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -30);
        Date birthDate = cal.getTime();
        PatientResponse patient = new PatientResponse();
        patient.setBirthDate(birthDate);
        int expectedAge = 30;
        Assert.assertEquals(Integer.toString(expectedAge), patient.getAge());
    }

    @Test
    public void shouldReturnCorrectAgeWhenTodayIsBirthday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -20);
        Date birthDate = cal.getTime();
        PatientResponse patient = new PatientResponse();
        patient.setBirthDate(birthDate);
        int expectedAge = 20;
        Assert.assertEquals(Integer.toString(expectedAge), patient.getAge());
    }

    @Test
    public void shouldReturnCorrectAgeWhenTodayIsBeforeBirthday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -20);
        cal.add(Calendar.DAY_OF_YEAR, 1);
        Date birthDate = cal.getTime();
        PatientResponse patient = new PatientResponse();
        patient.setBirthDate(birthDate);
        int expectedAge = 19;
        Assert.assertEquals(Integer.toString(expectedAge), patient.getAge());
    }
    @Test
    public void shouldReturnCorrectAgeWhenTodayIsBeforeBirthdayButNotBirthMonth() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -20);
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date birthDate = cal.getTime();
        PatientResponse patient = new PatientResponse();
        patient.setBirthDate(birthDate);
        int expectedAge = 19;
        Assert.assertEquals(Integer.toString(expectedAge), patient.getAge());
    }

    @Test
    public void shouldReturnDeathAgeIfPatientIsDeceased() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -20);
        Date birthDate = cal.getTime();
        // The year of death is deliberately set to four years ago to account for leap years.
        cal.add(Calendar.YEAR, -4);
        Date deathDate = cal.getTime();
        PatientResponse patient = new PatientResponse();
        patient.setBirthDate(birthDate);
        patient.setDeathDate(deathDate);
        int expectedAge = -4;
        Assert.assertEquals(Integer.toString(expectedAge), patient.getAge());
    }
}
