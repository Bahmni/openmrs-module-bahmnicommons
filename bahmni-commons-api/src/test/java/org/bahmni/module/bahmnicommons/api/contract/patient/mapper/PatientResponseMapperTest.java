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
        int expectedAge = 54;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -expectedAge);
        Date birthDate = cal.getTime();
        patient.setBirthdate(birthDate);
        patient.setUuid("someUUid");

        PatientResponse patientResponse = patientResponseMapper.map(patient, null, null, null, null);

        Assert.assertEquals(12, patientResponse.getPersonId());
        Assert.assertEquals(birthDate, patientResponse.getBirthDate());
        Assert.assertEquals(Integer.toString(expectedAge), patientResponse.getAge());
        Assert.assertEquals("someUUid", patientResponse.getUuid());
        Assert.assertEquals("FAN007", patientResponse.getIdentifier());
        Assert.assertEquals("{\"test\" : \"Extra009\"}", patientResponse.getExtraIdentifiers());
    }

    @Test
    public void shouldMapPersonAttributes() throws Exception {
        PersonAttributeType personAttributeType = new PersonAttributeType();
        personAttributeType.setName("givenNameLocal");
        patient.setAttributes(Sets.newSet(new PersonAttribute(personAttributeType,"someName")));
        String[] patientResultFields = {"givenNameLocal"};
        PatientResponse patientResponse = patientResponseMapper.map(patient, null, patientResultFields, null, null);

        Assert.assertEquals("{\"givenNameLocal\" : \"someName\"}", patientResponse.getCustomAttribute());
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

        Assert.assertEquals("{\"occupation\" : \"FSN\"}", patientResponse.getCustomAttribute());
    }

    @Test
    public void shouldAddSlashToSupportSpecialCharactersInJSON() throws Exception {
        PersonAttributeType personAttributeType = new PersonAttributeType();
        personAttributeType.setName("familyNameLocal");
        patient.setAttributes(Sets.newSet(new PersonAttribute(personAttributeType,"so\"me\\Name")));
        String[] patientResultFields = {"familyNameLocal"};
        PatientResponse patientResponse = patientResponseMapper.map(patient, null, patientResultFields, null, null);

        Assert.assertEquals("{\"familyNameLocal\" : \"so\\\"me\\\\Name\"}", patientResponse.getCustomAttribute());
    }

    @Test
    public void shouldMapPatientAddress() throws Exception {
        PersonAddress personAddress= new PersonAddress(2);
        personAddress.setAddress2("someAddress");
        patient.setAddresses(Sets.newSet(personAddress));

        PatientResponse patientResponse = patientResponseMapper.map(patient, null, null, new String[]{"address_2"}, null);
        Assert.assertEquals("{\"address_2\" : \"someAddress\"}", patientResponse.getAddressFieldValue());

    }

    @Test
    public void shouldMapVisitSummary() throws Exception {

        PatientResponse patientResponse = patientResponseMapper.map(patient, null, null, null, null);
        Assert.assertEquals("someLocationUUid", patientResponse.getActiveVisitUuid());
        Assert.assertEquals(Boolean.FALSE, patientResponse.getHasBeenAdmitted());
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
        Calendar birthCal = Calendar.getInstance();
        birthCal.set(2000, Calendar.JANUARY, 1);
        Date birthDate = birthCal.getTime();
        Calendar deathCal = Calendar.getInstance();
        deathCal.set(2020, Calendar.JANUARY, 1);
        Date deathDate = deathCal.getTime();
        PatientResponse patient = new PatientResponse();
        patient.setBirthDate(birthDate);
        patient.setDeathDate(deathDate);
        int expectedAge = 20;
        Assert.assertEquals(Integer.toString(expectedAge), patient.getAge());
    }
}
