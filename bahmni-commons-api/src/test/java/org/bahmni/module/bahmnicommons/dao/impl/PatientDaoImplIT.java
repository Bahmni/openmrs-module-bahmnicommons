package org.bahmni.module.bahmnicommons.dao.impl;

import org.bahmni.module.bahmnicommons.BaseIntegrationTest;
import org.bahmni.module.bahmnicommons.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicommons.dao.PatientDao;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class PatientDaoImplIT extends BaseIntegrationTest {
    @Autowired
    private PatientDao patientDao;
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        executeDataSet("apiTestData.xml");
    }

    @Test
    @Ignore //ignored because of the GROUP BY clause with h2 db memory
    public void shouldSearchByPatientPrimaryIdentifier() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatients("GAN200001", "", null, "city_village", "", 100, 0, null,"",null,addressResultFields,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient.getUuid());
        assertEquals("GAN200001", patient.getIdentifier());
        assertEquals("Horatio", patient.getGivenName());
        assertEquals("Sinha", patient.getFamilyName());
        assertEquals("M", patient.getGender());
        assertEquals("1983-01-30", patient.getBirthDate().toString());
        assertEquals("{ \"city_village\" : \"Ramgarh\"}", patient.getAddressFieldValue());
        assertEquals("2008-08-15 15:57:09.0", patient.getDateCreated().toString());
        assertEquals(null, patient.getDeathDate());
        assertEquals("{\"National ID\":\"NAT100010\"}", patient.getExtraIdentifiers());
    }

    @Test
    @Ignore //ignored because of the GROUP BY clause with h2 db memory
    public void shouldSearchByPatientExtraIdentifier() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatients("100010", "", null, "city_village", "", 100, 0, null,"",null,addressResultFields,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true);
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient.getUuid());
        assertEquals("GAN200001", patient.getIdentifier());
        assertEquals("Horatio", patient.getGivenName());
        assertEquals("Sinha", patient.getFamilyName());
        assertEquals("M", patient.getGender());
        assertEquals("1983-01-30", patient.getBirthDate().toString());
        assertEquals("{ \"city_village\" : \"Ramgarh\"}", patient.getAddressFieldValue());
        assertEquals("2008-08-15 15:57:09.0", patient.getDateCreated().toString());
        assertEquals(null, patient.getDeathDate());
        assertEquals("{\"National ID\":\"NAT100010\"}", patient.getExtraIdentifiers());
    }

    @Test
    @Ignore //ignored because of the GROUP BY clause with h2 db memory
    public void shouldSearchByOnlyPatientPrimaryIdentifier() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatients("100010", "", null, "city_village", "", 100, 0, null,"",null,addressResultFields,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(0, patients.size());
    }
    @Test
    @Ignore //ignored because of the GROUP BY clause with h2 db memory
    public void shouldSearchByPartialPatientIdentifier() {
        List<PatientResponse> patients = patientDao.getPatients("02", "", null, "city_village", "", 100, 0, null,"",null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);

        assertEquals("GAN200002", patient.getIdentifier());
        assertNull(patient.getExtraIdentifiers());
    }

    @Test
    @Ignore //ignored because of the GROUP BY clause with h2 db memory
    public void shouldSearchByName() {

        List<PatientResponse> patients = patientDao.getPatients("", "Horatio", null, "city_village", "", 100, 0, null,"",null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        assertEquals(3, patients.size());
        PatientResponse patient1 = patients.get(0);
        PatientResponse patient2 = patients.get(1);
        List<String> uuids = asList("341b4e41-790c-484f-b6ed-71dc8da222db", "86526ed5-3c11-11de-a0ba-001e378eb67a");

        assertTrue(uuids.contains(patient1.getUuid()));
        assertTrue(uuids.contains(patient2.getUuid()));

        assertEquals("Horatio", patient1.getGivenName());
        assertEquals("Horatio", patient2.getGivenName());
    }

    @Test
    public void shouldSearchAcrossFirstNameAndLastName() {
        List<PatientResponse> patients = patientDao.getPatients("", "Horati Sinha", null, "city_village", "", 100, 0, null,"",null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        assertEquals(1, patients.size());
        PatientResponse patient1 = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient1.getUuid());
        assertEquals("Horatio", patient1.getGivenName());
        assertEquals("Sinha", patient1.getFamilyName());
    }

    @Test
    public void shouldSearchByVillage() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatients("", "", null, "city_village", "Ramgarh", 100, 0, null,"",null,addressResultFields,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient.getUuid());
        assertEquals("GAN200001", patient.getIdentifier());
        assertEquals("Horatio", patient.getGivenName());
        assertEquals("Sinha", patient.getFamilyName());
        assertEquals("M", patient.getGender());
        assertEquals("1983-01-30", patient.getBirthDate().toString());
        assertEquals("{ \"city_village\" : \"Ramgarh\"}", patient.getAddressFieldValue());
        assertEquals("2008-08-15 15:57:09.0", patient.getDateCreated().toString());
        assertEquals(null, patient.getDeathDate());
    }

    @Test
    public void shouldSearchByNameAndVillage() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatients("", "Sin", null, "city_village", "Ramgarh", 100, 0, null,"",null,addressResultFields,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient.getUuid());
        assertEquals("GAN200001", patient.getIdentifier());
        assertEquals("Horatio", patient.getGivenName());
        assertEquals("Sinha", patient.getFamilyName());
        assertEquals("M", patient.getGender());

        assertEquals("{ \"city_village\" : \"Ramgarh\"}", patient.getAddressFieldValue());
        assertEquals("2008-08-15 15:57:09.0", patient.getDateCreated().toString());
        assertEquals(null, patient.getDeathDate());
    }

    @Test
    @Ignore //ignored because of the GROUP BY clause with h2 db memory
    public void shouldSortResultsByCreationDate() {
        List<PatientResponse> patients = patientDao.getPatients("", "Sinha", null, "city_village", "", 100, 0, null,"",null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(2, patients.size());
        assertEquals("Sinha", patients.get(0).getFamilyName());
        assertEquals("Sinha", patients.get(0).getFamilyName());
    }

    @Test
    @Ignore //ignored because of the GROUP BY clause with h2 db memory
    public void shouldReturnResultAfterGivenOffset() throws Exception {
        List<PatientResponse> patients = patientDao.getPatients("", "Sinha", null, "city_village", "", 100, 1, null,"",null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(1, patients.size());

        patients = patientDao.getPatients("", "Sinha", null, "city_village", "", 100, 2, null,"",null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(0, patients.size());
    }

    @Test
    @Ignore //ignored because of the NumberFormatException with h2 db memory
    public void shouldFetchBasedOnPatientAttributeTypes() throws Exception {
        String[] patientAttributes = { "caste"};
        String[] patientResultFields = {"caste"};
        List<PatientResponse> patients = patientDao.getPatients("", "", "testCaste1", "city_village", null, 100, 0, patientAttributes,"",null,null,patientResultFields, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        assertEquals(1, patients.size());
    }

    @Test
    public void shouldThrowErrorWhenPatientAttributesIsNotPresent() throws Exception {
        String[] patientAttributes = {"caste","nonExistingAttribute"};
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid Attribute In Patient Attributes [caste, nonExistingAttribute]");
        List<PatientResponse> patients = patientDao.getPatients("", "", "testCaste1", "city_village", null, 100, 0, patientAttributes, "", null, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

    }

    @Test
    public void shouldThrowErrorWhenPatientAddressIsNotPresent() throws Exception {
        String[] patientAttributes = {"caste"};
        String addressField = "nonExistingAddressFiled";
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid address parameter");
        List<PatientResponse> patients = patientDao.getPatients("", "", "testCaste1", addressField, null, 100, 0, patientAttributes, "", null, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

    }

    @Test
    @Ignore //ignored because of the GROUP BY clause with h2 db memory
    public void shouldFetchPatientsByProgramAttributes(){
        List<PatientResponse> patients = patientDao.getPatients("", "", "", "city_village", null, 100, 0, null,"Stage1","stage",null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(1, patients.size());
        PatientResponse response = patients.get(0);
        assertEquals("GAN200002",response.getIdentifier());
        assertEquals("John",response.getGivenName());
        assertEquals("{\"stage\":\"Stage1\"}",response.getPatientProgramAttributeValue());
    }

    @Test
    public void shouldThrowErrorWhenProgramAttributesIsNotPresent() {
        String nonExistingAttribute = "nonExistingAttribute";
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid Program Attribute");
        patientDao.getPatients("", "", "", "city_village", null, 100, 0, null, "Stage1",nonExistingAttribute, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

    }

    @Test
    @Ignore //ignored because of the NumberFormatException with h2 db memory
    public void shouldFetchPatientsByAllSearchParametersExceptIdentifier(){
        String[] addressResultFields = {"city_village"};
        String[] patientResultFields = {"caste"};

        List<PatientResponse> patients = patientDao.getPatients("", "John", "testCaste1", "city_village", "Bilaspur", 100, 0, new String[]{"caste","givenNameLocal"},"Stage1","stage",addressResultFields,patientResultFields, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(1, patients.size());
        PatientResponse response = patients.get(0);
        assertEquals("GAN200002",response.getIdentifier());
        assertEquals("df8ae447-6745-45be-b859-403241d9913d",response.getUuid());
        assertEquals(1026,response.getPersonId());
        assertEquals("GAN200002",response.getIdentifier());
        assertEquals("{ \"city_village\" : \"Bilaspur\"}",response.getAddressFieldValue());
        assertEquals("John",response.getGivenName());
        assertEquals("Peeter",response.getMiddleName());
        assertEquals("Sinha",response.getFamilyName());
        assertEquals("F",response.getGender());
        assertEquals("{\"stage\":\"Stage1\"}",response.getPatientProgramAttributeValue());
    }


    @Test
    @Ignore
    public void shouldFetchPatientsByCodedConcepts(){

        List<PatientResponse> patients = patientDao.getPatients("", "John", "testCaste1", "city_village", "Bilaspur", 100, 0, new String[]{"caste"}, "Fac", "facility",null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(1, patients.size());
        PatientResponse response = patients.get(0);
        assertEquals("GAN200002",response.getIdentifier());
        assertEquals("df8ae447-6745-45be-b859-403241d9913d",response.getUuid());
        assertEquals(1026,response.getPersonId());
        assertEquals("GAN200002",response.getIdentifier());
        assertEquals("Bilaspur",response.getAddressFieldValue());
        assertEquals("John",response.getGivenName());
        assertEquals("Peeter",response.getMiddleName());
        assertEquals("Sinha",response.getFamilyName());
        assertEquals("F",response.getGender());
        assertEquals("{\"caste\":\"testCaste1\"}",response.getCustomAttribute());
        assertEquals("{\"facility\":\"Facility1, City1, Country1\"}",response.getPatientProgramAttributeValue());
    }

    @Test
    @Ignore //ignored because of the GROUP BY clause with h2 db memory
    public void shouldFetchPatientsByOnlyOneProgramAttribute(){
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatients("", "", null, "city_village", "", 100, 0, null,"Stage1","stage",addressResultFields,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(1, patients.size());
        PatientResponse response = patients.get(0);
        assertEquals("GAN200002",response.getIdentifier());
        assertEquals("df8ae447-6745-45be-b859-403241d9913d",response.getUuid());
        assertEquals(1026,response.getPersonId());
        assertEquals("GAN200002",response.getIdentifier());
        assertEquals("{ \"city_village\" : \"Bilaspur\"}",response.getAddressFieldValue());
        assertEquals("John",response.getGivenName());
        assertEquals("Peeter",response.getMiddleName());
        assertEquals("Sinha",response.getFamilyName());
        assertEquals("F",response.getGender());
        assertEquals("{\"stage\":\"Stage1\"}",response.getPatientProgramAttributeValue());
    }

    @Test
    @Ignore //ignored because of the GROUP BY clause with h2 db memory
    public void shouldSearchByPatientIdentifierWithAttributes() {
        List<PatientResponse> patients = patientDao.getPatients("", "John", null, "city_village", "", 100, 0, null,"",null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(2, patients.size());
    }

    @Test
    @Ignore //ignored because of the GROUP BY clause with h2 db memory
    public void shouldReturnAdmissionStatus() throws Exception{
        List<PatientResponse> patients = patientDao.getPatients("200000", null, null, "city_village", null, 10, 0, null, null, null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(1, patients.size());
        PatientResponse patient200000 = patients.get(0);
        assertFalse(patient200000.getHasBeenAdmitted());

        patients = patientDao.getPatients("200002", null, null, "city_village", null, 10, 0, null, null, null,null,null, "8d6c993e-c2cc-11de-8d13-0040c6dffd0f", false, false);
        assertEquals(1, patients.size());
        PatientResponse patient200003 = patients.get(0);
        assertTrue(patient200003.getHasBeenAdmitted());
    }

    @Test
    @Ignore //ignored because of the NumberFormatException with h2 db memory
    public void shouldReturnAddressAndPatientAttributes() throws Exception{
        String[] addressResultFields = {"address3"};
        String[] patientResultFields = {"middleNameLocal"  ,  "familyNameLocal" ,"givenNameLocal"};
        List<PatientResponse> patients = patientDao.getPatients("GAN200002", null, null, null, null, 100, 0, new String[]{"caste","givenNameLocal"},null,null,addressResultFields,patientResultFields, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(1, patients.size());
        PatientResponse patient200002 = patients.get(0);
        assertTrue("{\"givenNameLocal\":\"ram\",\"middleNameLocal\":\"singh\",\"familyNameLocal\":\"gond\"}".equals(patient200002.getCustomAttribute()));
        assertTrue("{ \"address3\" : \"Dindori\"}".equals(patient200002.getAddressFieldValue()));
    }

    /**
     * Ignored because of the h2 db interpretation of escape character for single quotes in query.
     * MySql respects blackslash (e.g \') and singlequote ('') both.
     * String query params escaped with \ throws error.
     */
    @Test
    @Ignore
    public void shouldSearchPatientByNameWithSingleQuote() throws Exception {
        List<PatientResponse> patients = patientDao.getPatients(null, "na'me", null, null, null, 10, 0, null, null, null, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        PatientResponse patient = patients.get(0);

        assertEquals(1, patients.size());
        assertEquals("na'me",patient.getFamilyName());

    }

    /**
     * Ignored because of the h2 db interpretation of escape character for single quotes in query.
     * MySql respects blackslash (e.g \') and singlequote ('') both.
     * String query params escaped with \ throws error.
     */
    @Test
    @Ignore
    public void shouldSearchPatientByNameWithOneSingleQuoteInSearchString() throws Exception {
        List<PatientResponse>  patients = patientDao.getPatients(null, "'", null, null, null, 10, 0, null, null, null, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        PatientResponse patientSearchWithJustSingleQuote = patients.get(0);

        assertEquals(1, patients.size());
        assertEquals("na'me",patientSearchWithJustSingleQuote.getFamilyName());
    }

    /**
     * Ignored because of the h2 db interpretation of escape character for single quotes in query.
     * MySql respects blackslash (e.g \') and singlequote ('') both.
     * String query params escaped with \ throws error.
     */
    @Test
    @Ignore
    public void shouldSearchPatientNameByMultipleSingleQuotesInSearchString() throws Exception {
        List<PatientResponse>  patients = patientDao.getPatients(null, "'''", null, null, null, 10, 0, null, null, null, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        assertEquals(0, patients.size());
    }


    @Test
    public void shouldGiveEmptyResultIfPatientDoesnotExistWithGivenPatientName() throws Exception {
        List<PatientResponse> patients = patientDao.getPatients(null, "johnny", null, null, null, 10, 0, null, null, null, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        assertEquals(0, patients.size());
    }

    @Test
    @Ignore //ignored because of the GROUP BY clause with h2 db memory
    public void shouldGiveAllThePatientsIfWeSearchWithPercentile() throws Exception {
        List<PatientResponse> patients = patientDao.getPatients(null, "%", null, null, null, 10, 0, null, null, null, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        assertEquals(10, patients.size());
    }

    @Test
    @Ignore //ignored because of the GROUP BY clause with h2 db memory
    public void shouldGiveAllThePatientsIfWeSearchWithPercentileAsIdentifier() throws Exception {
        List<PatientResponse> patients = patientDao.getPatients("%", null, null, null, null, 10, 0, null, null, null, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        assertEquals(10, patients.size());
    }

    @Test
    @Ignore //ignored because of the GROUP BY clause with h2 db memory
    public void shouldGiveThePatientsIfWeSearchBySpaceSeperatedString() throws Exception {
        List<PatientResponse> patients = patientDao.getPatients(null, "special character", null, null, null, 10, 0, null, null, null, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        assertEquals(2, patients.size());
    }

    @Test
    @Ignore //ignored because of the NumberFormatException with h2 db memory
    public void shouldFetchBasedOnPatientAttributeTypesWhenThereIsSingleQuoteInPatientAttribute() throws Exception {
        String[] patientAttributes = { "caste"};
        String[] patientResultFields = {"caste"};
        String[] addressResultFields = {"address3"};
        List<PatientResponse> patients = patientDao.getPatients("", "", "go'nd", null, null, 100, 0, patientAttributes,null,null,addressResultFields, patientResultFields, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        assertEquals(1, patients.size());

        assertEquals("{\"caste\":\"go'nd\"}", patients.get(0).getCustomAttribute());

        assertTrue("{ \"address3\" : \"Dindori\"}".equals(patients.get(0).getAddressFieldValue()));


        patients = patientDao.getPatients("", "", "'", null, null, 100, 0, patientAttributes,null,null,addressResultFields, patientResultFields, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        PatientResponse patientWithSingleQuoteInSearch = patients.get(0);

        assertEquals(1, patients.size());
        assertEquals("{\"caste\":\"go'nd\"}", patientWithSingleQuoteInSearch.getCustomAttribute());
        assertTrue("{ \"address3\" : \"Dindori\"}".equals(patientWithSingleQuoteInSearch.getAddressFieldValue()));


        patients = patientDao.getPatients("", "", "'''", null, null, 100, 0, patientAttributes,null,null,addressResultFields, patientResultFields, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        assertEquals(0, patients.size());
    }

    /**
     * Ignored because of the h2 db interpretation of escape character for single quotes in query.
     * MySql respects blackslash (e.g \') and singlequote ('') both.
     * String query params escaped with \ throws error.
     */
    @Test
    @Ignore
    public void shouldFetchPatientsByProgramAttributesWhenThereIsSingleQuoteInProgramAttribute(){
        List<PatientResponse> patients = patientDao.getPatients("", "", "", null, null, 100, 0, null,"Stage'12","stage",null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        PatientResponse response = patients.get(0);

        assertEquals(1, patients.size());
        assertEquals("{\"stage\":\"Stage'12\"}",response.getPatientProgramAttributeValue());
    }

    /**
     * Ignored because of the h2 db interpretation of escape character for single quotes in query.
     * MySql respects blackslash (e.g \') and singlequote ('') both.
     * String query params escaped with \ throws error.
     */
    @Test
    @Ignore
    public void shouldFetchPatientsByProgramAttributeWhenThereIsJustOneSingleQuoteInSearchString() throws Exception {
        List<PatientResponse> patients = patientDao.getPatients("", "", "", null, null, 100, 0, null,"'","stage",null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        PatientResponse response = patients.get(0);

        assertEquals(1, patients.size());
        assertEquals("{\"stage\":\"Stage'12\"}",response.getPatientProgramAttributeValue());
    }

    @Test
    @Ignore //ignored because of the GROUP BY clause with h2 db memory
    public void shouldFetchPatientsByParogramAttributeWhenThreAreMultipleSingleQuotesInSearchString() throws Exception {
        List<PatientResponse> patients = patientDao.getPatients("", "", "", null, null, 100, 0, null,"''''","stage",null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        assertEquals(0, patients.size());
    }

    /**
     * Ignored because of the h2 db interpretation of escape character for single quotes in query.
     * MySql respects blackslash (e.g \') and singlequote ('') both.
     * String query params escaped with \ throws error.
     */
    @Test
    @Ignore
    public void shouldFetchPatientsByPatientIdentifierWhenThereIsSingleQuoteInPatientIdentifier(){
        List<PatientResponse> patients = patientDao.getPatients("51'0003", "", "", null, null, 100, 0, null,null, null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        PatientResponse response = patients.get(0);

        assertEquals(1, patients.size());
        assertEquals("SEV51'0003", response.getIdentifier());
    }

    /***
     * Ignored because of h2 db for test gets the single quote in a string
     * @throws Exception
     */
    @Test
    @Ignore
    public void shouldFetchPatientsByPatientIdentifierWhenThereIsJustOneSingleQuoteInPatientIdentifier() throws Exception {
        List<PatientResponse> patients = patientDao.getPatients("'", "", "", null, null, 100, 0, null,null, null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(1, patients.size());
        PatientResponse response = patients.get(0);
        assertEquals("SEV51'0003", response.getIdentifier());
    }

    /**
     * Ignored because of the h2 db interpretation of escape character for single quotes in query.
     * MySql respects blackslash (e.g \') and singlequote ('') both.
     * String query params escaped with \ throws error.
     */
    @Test
    @Ignore
    public void shouldSearchPatientsByPatientIdentifierWhenThereAreMultipleSinglesInSearchString() throws Exception {

        List<PatientResponse> patients = patientDao.getPatients("'''", "", "", null, null, 100, 0, null,null, null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        assertEquals(0, patients.size());
    }

    @Test
    public void shouldNotReturnDuplicatePatientsEvenIfThereAreMultipleVisitsForThePatients() {
        List<PatientResponse> patients = patientDao.getPatients("", "1058GivenName", null, "city_village", "", 100, 0, null,"",null,null,null, "8d6c993e-c2cc-11de-8d34-0010c6affd0f", false, false);

        assertEquals(1, patients.size());
        PatientResponse patient1 = patients.get(0);

        assertEquals("1058GivenName", patient1.getGivenName());
    }

    @Test
    public void shouldReturnPatientEvenIfThereIsNoVisitForThePatientWhenFilterByVisitLocationIsFalse() {
        List<PatientResponse> patients = patientDao.getPatients("", "1059NoVisit", null, "city_village", "", 100, 0, null,"",null,null,null, "8d6c993e-c2cc-11de-8d34-0010c6affd0f", false, false);

        assertEquals(1, patients.size());
        PatientResponse patient1 = patients.get(0);

        assertEquals("1059NoVisit", patient1.getGivenName());
    }

    @Test
    public void shouldNotReturnPatientIfThereIsNoVisitForThePatientAndFilterByVisitLocationIsTrue() {
        List<PatientResponse> patients = patientDao.getPatients("", "1059NoVisit", null, "city_village", "", 100, 0, null,"",null,null,null, "8d6c993e-c2cc-11de-8d34-0010c6affd0f", true, false);

        assertEquals(0, patients.size());
    }

    @Test
    @Ignore //ignored because of the GROUP BY clause with h2 db memory
    public void shouldReturnPatientsWithinVisitLocationOfGivenLoginLocationWhenFilterByVisitLocationIsTrue() {
        List<PatientResponse> patients = patientDao.getPatients("", "someUnique", null, "city_village", "", 100, 0, null,"",null,null,null, "8d6c993e-c2cc-11de-8d34-0010c6affd0f", true, false);
        assertEquals(1, patients.size());

        PatientResponse patient = patients.get(0);
        assertEquals("someUniqueName", patient.getGivenName());
    }

    @Test
    @Ignore //ignored because of the GROUP BY clause with h2 db memory
    public void shouldReturnAllMatchingPatientsIrrespectiveOfVisitsWhenFilterByVisitLocationIsFalse() {
        List<PatientResponse> patients = patientDao.getPatients("", "someUnique", null, "city_village", "", 100, 0, null,"",null,null,null, "8d6c993e-c2cc-11de-8d34-0010c6affd0f", false, false);
        assertEquals(2, patients.size());

        PatientResponse patient1 = patients.get(0);
        PatientResponse patient2 = patients.get(1);
        assertEquals("someUniqueName", patient1.getGivenName());
        assertEquals("someUniqueOtherName", patient2.getGivenName());
    }

    @Test
    @Ignore //ignored because of the GROUP BY clause with h2 db memory
    public void shouldReturnPatientsWithinVisitLocationWhenLocationProvidedIsChildLocationAndFilterByLocationIsTrue() {
        List<PatientResponse> patients = patientDao.getPatients("", "someUnique", null, "city_village", "", 100, 0, null,"",null,null,null, "8d6c993e-c2cc-11de-8d13-0010c6addd0f", true, false);
        assertEquals(1, patients.size());

        PatientResponse patient = patients.get(0);
        assertEquals("someUniqueName", patient.getGivenName());
    }

    @Test
    @Ignore //ignored because of the GROUP BY clause with h2 db memory
    public void shouldReturnPatientsWithinTheVisitLocationWhenTheLocationPassedIsVisitLocationAndFilterByVisitLocationIsTrue() {
        List<PatientResponse> patients = patientDao.getPatients("", "someUnique", null, "city_village", "", 100, 0, null, "", null, null, null, "8d6c993e-c2cc-11de-8d13-0010c6aff12f", true, false);
        assertEquals(1, patients.size());

        PatientResponse patient = patients.get(0);
        assertEquals("someUniqueName", patient.getGivenName());
    }

    @Test
    @Ignore //h2 join issue when there are attributes that are non concept
    public void shouldReturnPersonAttributeConceptName() throws Exception{
        String[] patientResultFields = {"thaluk"};
        List<PatientResponse> patients = patientDao.getPatients("SEV500003", null, null, null, null, 100, 0, null,null,null,null,patientResultFields, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(1, patients.size());
        PatientResponse patient200002 = patients.get(0);
        assertEquals("{\"thaluk\":\"Taluk Shivtarai\"}",patient200002.getCustomAttribute());

    }


    @Test(expected = IllegalArgumentException.class)
    public void shouldReturnAllMatchingPatientsWhenLoginLocationIsNull() {
        patientDao.getPatients("", "someUnique", null, "city_village", "", 100, 0, null,"",null,null,null, null, false, false);

    }
}
