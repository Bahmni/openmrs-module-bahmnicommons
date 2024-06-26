package org.bahmni.module.bahmnicommons.api.dao.impl;

import org.bahmni.module.bahmnicommons.api.BaseIntegrationTest;
import org.bahmni.module.bahmnicommons.api.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicommons.api.dao.PatientDao;
import org.junit.*;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.rules.ExpectedException;
import org.openmrs.Patient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertEquals;

public class PatientDaoImplLuceneIT extends BaseIntegrationTest {
    @Autowired
    private PatientDao patientDao;
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Rule
    public final EnvironmentVariables environmentVariables
            = new EnvironmentVariables();

    @Before
    public void setUp() throws Exception {
        executeDataSet("apiTestData.xml");
        updateSearchIndex();
    }

    @Test
    public void shouldSearchByPatientPrimaryIdentifier() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("GAN200001", "", null, "city_village", "", 100, 0, null,"",null,addressResultFields,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false, "", "");
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient.getUuid());
        assertEquals("GAN200001", patient.getIdentifier());
        assertEquals("Horatio", patient.getGivenName());
        assertEquals("Sinha", patient.getFamilyName());
        assertEquals("M", patient.getGender());
        assertEquals("1983-01-30 00:00:00.0", patient.getBirthDate().toString());
        assertEquals("{\"city_village\" : \"Ramgarh\"}", patient.getAddressFieldValue());
        assertEquals("2006-01-18 00:00:00.0", patient.getDateCreated().toString());
        assertEquals(null, patient.getDeathDate());
        assertEquals("{\"National ID\" : \"NAT100010\"}", patient.getExtraIdentifiers());
    }

    @Test
    public void shouldFilterOutPatientForPrimaryIdentifierIfFilterAttributeIsPresent() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("GAN200001", "", null, "city_village", "", 100, 0, null,"",null,addressResultFields,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false, "filterAttribute", "false");
        assertEquals(0, patients.size());
    }

    @Test
    public void shouldSearchByExactPatientIdentifierWhenLengthIsGreaterThanMaxNGramLength() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("GAN200004-2005-09-22-00-00", "", null, "city_village", "", 100, 0, null,"",null,addressResultFields,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false, "", "");
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("GAN200004-2005-09-22-00-00", patient.getIdentifier());
    }

    @Test
    public void shouldSearchByPatientExtraIdentifier() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("100010", "", null, "city_village", "", 100, 0, null,"",null,addressResultFields,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true, "", "");
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient.getUuid());
        assertEquals("GAN200001", patient.getIdentifier());
        assertEquals("Horatio", patient.getGivenName());
        assertEquals("Sinha", patient.getFamilyName());
        assertEquals("M", patient.getGender());
        assertEquals("1983-01-30 00:00:00.0", patient.getBirthDate().toString());
        assertEquals("{\"city_village\" : \"Ramgarh\"}", patient.getAddressFieldValue());
        assertEquals("2006-01-18 00:00:00.0", patient.getDateCreated().toString());
        assertEquals(null, patient.getDeathDate());
        assertEquals("{\"National ID\" : \"NAT100010\"}", patient.getExtraIdentifiers());
    }

    @Test
    public void shouldReturnPatientBasedOnIdentifier() {
        List<Patient> patients = patientDao.getPatients("GAN200001", true);
        assertEquals(1, patients.size());
        patients = patientDao.getPatients("GAN200001", false);
        assertEquals(1, patients.size());
        patients = patientDao.getPatients("", true);
        assertEquals(0, patients.size());
    }

    @Test
    public void shouldSearchByPartialPatientIdentifier() {
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("02", "", null, "city_village", "", 100, 0, null,"",null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false, "", "");
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);

        assertEquals("GAN200002", patient.getIdentifier());
        assertNull(patient.getExtraIdentifiers());
    }

    @Test
    public void shouldReturnResultAfterGivenOffset() throws Exception {
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("300001", "", null, "city_village", "", 100, 1, null,"",null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false, "", "");
        assertEquals(1, patients.size());

        patients = patientDao.getPatientsUsingLuceneSearch("300001", "", null, "city_village", "", 100, 2, null,"",null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false, "", "");
        assertEquals(0, patients.size());
    }

    @Test
    public void shouldThrowErrorWhenPatientAttributesIsNotPresent() throws Exception {
        String[] patientAttributes = {"caste","nonExistingAttribute"};
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid Attribute In Patient Attributes [caste, nonExistingAttribute]");
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("", "", "testCaste1", "city_village", null, 100, 0, patientAttributes, "", null, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false, "", "");

    }

    @Test
    public void shouldThrowErrorWhenPatientAddressIsNotPresent() throws Exception {
        String[] patientAttributes = {"caste"};
        String addressField = "nonExistingAddressFiled";
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid address parameter");
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("", "", "testCaste1", addressField, null, 100, 0, patientAttributes, "", null, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false, "", "");

    }

    @Test
    public void shouldThrowErrorWhenProgramAttributesIsNotPresent() {
        String nonExistingAttribute = "nonExistingAttribute";
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid Program Attribute");
        patientDao.getPatientsUsingLuceneSearch("", "", "", "city_village", null, 100, 0, null, "Stage1",nonExistingAttribute, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false, "", "");

    }

    @Test
    public void shouldReturnAdmissionStatus() throws Exception{
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("200000", null, null, "city_village", null, 10, 0, null, null, null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false, "", "");
        assertEquals(1, patients.size());
        PatientResponse patient200000 = patients.get(0);
        assertFalse(patient200000.getHasBeenAdmitted());

        patients = patientDao.getPatientsUsingLuceneSearch("200002", null, null, "city_village", null, 10, 0, null, null, null,null,null, "8d6c993e-c2cc-11de-8d13-0040c6dffd0f", false, false, "", "");
        assertEquals(1, patients.size());
        PatientResponse patient200003 = patients.get(0);
        assertTrue(patient200003.getHasBeenAdmitted());
    }

    @Test
    public void shouldReturnAddressAndPatientAttributes() throws Exception{
        String[] addressResultFields = {"address3"};
        String[] patientResultFields = {"middleNameLocal"  ,  "familyNameLocal" ,"givenNameLocal"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("GAN200002", null, null, null, null, 100, 0, new String[]{"caste","givenNameLocal"},null,null,addressResultFields,patientResultFields, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false, "", "");
        assertEquals(1, patients.size());
        PatientResponse patient200002 = patients.get(0);
        assertTrue("{\"middleNameLocal\" : \"singh\",\"familyNameLocal\" : \"gond\",\"givenNameLocal\" : \"ram\"}".equals(patient200002.getCustomAttribute()));
        assertTrue("{\"address3\" : \"Dindori\"}".equals(patient200002.getAddressFieldValue()));
    }

    @Test
    public void shouldGiveAllThePatientsIfWeSearchWithPercentileAsIdentifier() throws Exception {
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("%", null, null, null, null, 10, 0, null, null, null, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false, "", "");

        assertEquals(10, patients.size());
    }

    @Test
    public void shouldFetchPatientsByPatientIdentifierWhenThereIsSingleQuoteInPatientIdentifier(){
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("51'0003", "", "", null, null, 100, 0, null,null, null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false, "", "");

        PatientResponse response = patients.get(0);

        assertEquals(1, patients.size());
        assertEquals("SEV51'0003", response.getIdentifier());
    }

    @Test
    public void shouldFetchPatientsByPatientIdentifierWhenThereIsJustOneSingleQuoteInPatientIdentifier() throws Exception {
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("'", "", "", null, null, 100, 0, null,null, null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false, "", "");

        PatientResponse response = patients.get(0);

        assertEquals(1, patients.size());
        assertEquals("SEV51'0003", response.getIdentifier());
    }

    @Test
    public void shouldSearchPatientsByPatientIdentifierWhenThereAreMultipleSinglesInSearchString() throws Exception {

        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("'''", "", "", null, null, 100, 0, null,null, null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false, "", "");

        assertEquals(0, patients.size());
    }

    @Test
    public void shouldNotReturnDuplicatePatientsEvenIfThereAreMultipleVisitsForThePatients() {
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("HOS1225", "", null, "city_village", "", 100, 0, null,"",null,null,null, "8d6c993e-c2cc-11de-8d34-0010c6affd0f", false, false, "", "");

        assertEquals(1, patients.size());
        PatientResponse patient1 = patients.get(0);

        assertEquals("1058GivenName", patient1.getGivenName());
    }

    @Test
    public void shouldNotSearchExtraIdentifiersIfFilterOnAllIdenfiersIsFalse() {
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("100010", "", null, "city_village", "", 100, 0, null,"", null, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false, "", "");

        assertEquals(0, patients.size());
    }

    @Test
    public void shouldSearchAllIdentifiersIfFilterOnAllIdentifiersIsTrue() {
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("0001", "", null, "city_village", "", 100, 0, null,"", null, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true, "", "");

        assertEquals(3, patients.size());
        assertEquals("{\"National ID\" : \"NAT100010\"}", patients.get(0).getExtraIdentifiers());
        assertEquals("GAN300001",patients.get(1).getIdentifier());
    }

    @Test
    public void shouldNotReturnPatientsIfFilterOnAllIdenfiersIsTrueButNotAnExtraIdentifier() {
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("DLF200001", "", null, "city_village", "", 100, 0, null,"", null, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true, "", "");

        assertEquals(0, patients.size());
    }

    @Test
    public void shouldNotReturnDuplicatePatientsEvenIfTwoIdentifiersMatches() {
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("200006", "", null, "city_village", "", 100, 0, null,"", null, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true, "", "");

        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertTrue(patient.getIdentifier().contains("200006"));
        assertTrue(patient.getExtraIdentifiers().contains("200006"));
    }

    @Test
    @Ignore // to be fixed
    public void shouldSearchByPatientPrimaryIdentifierIfSpecifiedAndNotByName() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("GAN200000", "GAN200000", null, "city_village", "",
            100, 0, null, "", null, addressResultFields, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false, "", "");
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("86526ed5-3c11-11de-a0ba-001e378eb67a", patient.getUuid());
        assertEquals("GAN200001", patient.getIdentifier());
        assertEquals("Horatio", patient.getGivenName());
        assertEquals("Test", patient.getMiddleName());
        assertEquals("Banka", patient.getFamilyName());
        assertEquals("M", patient.getGender());
    }

    @Test
    @Ignore // to be fixed
    public void shouldSearchByPatientNameWhenIDsDoNotMatch() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("Peeter", "Peeter", null, "city_village", "",
            100, 0, null, "", null, addressResultFields, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false, "", "");
        assertEquals(27, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient.getUuid());
        assertEquals("GAN200001", patient.getIdentifier());
        assertEquals("Horatio", patient.getGivenName());
        assertEquals("Peeter", patient.getMiddleName());
        assertEquals("Sinha", patient.getFamilyName());
        assertEquals("M", patient.getGender());
        assertEquals("{\"city_village\" : \"Ramgarh\"}", patient.getAddressFieldValue());
        assertEquals(null, patient.getDeathDate());
        assertEquals("{\"National ID\" : \"NAT100010\"}", patient.getExtraIdentifiers());
    }

    @Test
    public void shouldSearchByAnyPatientIdentifierThenByName() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("NAT100010", "NAT100010", null, "city_village", "",
            100, 0, null, "", null, addressResultFields, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true, "", "");
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient.getUuid());
        assertEquals("GAN200001", patient.getIdentifier());
        assertEquals("Horatio", patient.getGivenName());
        assertEquals("Peeter", patient.getMiddleName());
        assertEquals("Sinha", patient.getFamilyName());
        assertEquals("M", patient.getGender());
        assertEquals("{\"city_village\" : \"Ramgarh\"}", patient.getAddressFieldValue());
        assertEquals(null, patient.getDeathDate());
        assertEquals("{\"National ID\" : \"NAT100010\"}", patient.getExtraIdentifiers());
    }
    @Test
    public void shouldSearchByAnyPatientIdentifierThenByNameAndHaveCombinedResult() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("NAT100010", "John", null, "city_village", "",
                100, 0, null, "", null, addressResultFields, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true, "", "");
        assertEquals(4, patients.size());
        patients = patientDao.getPatientsUsingLuceneSearch("uniqueStringSoNoResultsFromIdentifier", "John", null, "city_village", "",
                100, 0, null, "", null, addressResultFields, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true, "", "");
        assertEquals(3, patients.size());
        patients = patientDao.getPatientsUsingLuceneSearch("NAT100010", "uniqueStringSoNoResultsFromPatientNames", null, "city_village", "",
                100, 0, null, "", null, addressResultFields, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true, "", "");
        assertEquals(1, patients.size());
    }

    @Test
    public void shouldSearchByAnyPatientFullName() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("Horatio Peeter Sinha", "Horatio Peeter Sinha", null, "city_village", "",
                100, 0, null, "", null, addressResultFields, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true,"", "");
        assertNotNull(patients);
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient.getUuid());
        assertEquals("GAN200001", patient.getIdentifier());
        assertEquals("Horatio", patient.getGivenName());
        assertEquals("Peeter", patient.getMiddleName());
        assertEquals("Sinha", patient.getFamilyName());
        assertEquals("M", patient.getGender());
        assertEquals("{\"city_village\" : \"Ramgarh\"}", patient.getAddressFieldValue());
        assertEquals(null, patient.getDeathDate());
        assertEquals("{\"National ID\" : \"NAT100010\"}", patient.getExtraIdentifiers());
    }

    @Test
    public void shouldSearchAnyPatientWithSingleCharacterFirstName() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("R Dev Burman", "R Dev Burman", null, "city_village", null,
                100, 0, null, "", null, addressResultFields, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true, "", "");
        assertNotNull(patients);
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("8387b8b6-9142-49da-8ac7-6e2b9a8dba21", patient.getUuid());
        assertEquals("PAN200062", patient.getIdentifier());
        assertEquals("R", patient.getGivenName());
        assertEquals("Dev", patient.getMiddleName());
        assertEquals("Burman", patient.getFamilyName());
        assertEquals("M", patient.getGender());
        assertEquals("{\"city_village\" : \"Ramgarh\"}", patient.getAddressFieldValue());
        assertNull(patient.getDeathDate());
        assertNull(patient.getExtraIdentifiers());
    }

    @Test
    public void shouldSearchAnyPatientWithSingleCharacterSecondName() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("Sachin D Burman", "Sachin D Burman", null, "city_village", null,
                100, 0, null, "", null, addressResultFields, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true, "", "");
        assertNotNull(patients);
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("9f5fdc74-d61b-11ee-a506-0242ac120002", patient.getUuid());
        assertEquals("PAN200063", patient.getIdentifier());
        assertEquals("Sachin", patient.getGivenName());
        assertEquals("D", patient.getMiddleName());
        assertEquals("Burman", patient.getFamilyName());
        assertEquals("M", patient.getGender());
        assertEquals("{\"city_village\" : \"Ramgarh\"}", patient.getAddressFieldValue());
        assertNull(patient.getDeathDate());
        assertNull(patient.getExtraIdentifiers());
    }

    @Test
    public void shouldSearchAnyPatientWithSingleCharacterLastName() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("Rahul Dev B", "Rahul Dev B", null, "city_village", null,
                100, 0, null, "", null, addressResultFields, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true, "", "");
        assertNotNull(patients);
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("a797b93e-d61b-11ee-a506-0242ac120002", patient.getUuid());
        assertEquals("PAN200064", patient.getIdentifier());
        assertEquals("Rahul", patient.getGivenName());
        assertEquals("Dev", patient.getMiddleName());
        assertEquals("B", patient.getFamilyName());
        assertEquals("M", patient.getGender());
        assertEquals("{\"city_village\" : \"Ramgarh\"}", patient.getAddressFieldValue());
        assertNull(patient.getDeathDate());
        assertNull(patient.getExtraIdentifiers());
    }

    @Test
    public void shouldSearchAllPatientsWithSimilarFirstName() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("F Scott Fitzgerald", "F Scott Fitzgerald", null, "city_village", null,
                100, 0, null, "", null, addressResultFields, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true, "", "");
        assertNotNull(patients);
        assertEquals(2, patients.size());
        PatientResponse patientResponse1 = patients.get(0);
        assertEquals("6817dcdc-d623-11ee-a506-0242ac120002", patientResponse1.getUuid());
        assertEquals("PAN200065", patientResponse1.getIdentifier());
        assertEquals("Francis", patientResponse1.getGivenName());
        assertEquals("Scott", patientResponse1.getMiddleName());
        assertEquals("Fitzgerald", patientResponse1.getFamilyName());
        PatientResponse patientResponse2 = patients.get(1);
        assertEquals("6e318c2a-d624-11ee-a506-0242ac120002", patientResponse2.getUuid());
        assertEquals("PAN200066", patientResponse2.getIdentifier());
        assertEquals("F", patientResponse2.getGivenName());
        assertEquals("Scott", patientResponse2.getMiddleName());
        assertEquals("Fitzgerald", patientResponse2.getFamilyName());
    }

    @Test
    public void shouldSearchPatientsWithExactMatchWhenModeIsEXACT() throws Exception {
        environmentVariables.set("LUCENE_MATCH_TYPE","EXACT");
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("F Scott Fitzgerald", "F Scott Fitzgerald", null, "city_village", null,
                100, 0, null, "", null, addressResultFields, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true, "", "");
        assertNotNull(patients);
        assertEquals(1, patients.size());
        PatientResponse patientResponse = patients.get(0);
        assertEquals("6e318c2a-d624-11ee-a506-0242ac120002", patientResponse.getUuid());
        assertEquals("PAN200066", patientResponse.getIdentifier());
        assertEquals("F", patientResponse.getGivenName());
        assertEquals("Scott", patientResponse.getMiddleName());
        assertEquals("Fitzgerald", patientResponse.getFamilyName());
    }

    @Test
    public void shouldSearchPatientsWithMatchAnywhereWhenModeIsANYWHERE() throws Exception {
        environmentVariables.set("LUCENE_MATCH_TYPE","ANYWHERE");
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("Niel", "Niel", null, "city_village", null,
                100, 0, null, "", null, addressResultFields, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true, "", "");
        assertNotNull(patients);
        assertEquals(2, patients.size());
        PatientResponse patient1 = patients.get(0);
        assertEquals("a83ae586-d6bc-11ee-a506-0242ac120002", patient1.getUuid());
        assertEquals("PAN200067", patient1.getIdentifier());
        assertEquals("Daniel", patient1.getGivenName());
        assertEquals("Day", patient1.getMiddleName());
        assertEquals("Lewis", patient1.getFamilyName());
        PatientResponse patient2 = patients.get(1);
        assertEquals("8bf18320-d6bd-11ee-a506-0242ac120002", patient2.getUuid());
        assertEquals("PAN200068", patient2.getIdentifier());
        assertEquals("Nielsen", patient2.getGivenName());
        assertEquals("William", patient2.getMiddleName());
        assertEquals("Leslie", patient2.getFamilyName());
    }

    @Test
    public void shouldSearchPatientsNameWhichStartsWithSearchTermWhenModeIsSTART() throws Exception {
        environmentVariables.set("LUCENE_MATCH_TYPE","START");
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("Niel", "Niel", null, "city_village", null,
                100, 0, null, "", null, addressResultFields, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true, "", "");
        assertNotNull(patients);
        assertEquals(1, patients.size());
        PatientResponse patientResponse = patients.get(0);
        assertEquals("8bf18320-d6bd-11ee-a506-0242ac120002", patientResponse.getUuid());
        assertEquals("PAN200068", patientResponse.getIdentifier());
        assertEquals("Nielsen", patientResponse.getGivenName());
        assertEquals("William", patientResponse.getMiddleName());
        assertEquals("Leslie", patientResponse.getFamilyName());
    }
}
