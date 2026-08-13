package org.bahmni.module.bahmnicommons.api.service;

import org.bahmni.module.bahmnicommons.api.contract.patient.response.PatientConfigResponse;
import org.bahmni.module.bahmnicommons.api.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicommons.api.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicommons.api.search.dto.PatientSearchRequest;
import org.bahmni.module.bahmnicommons.api.search.dto.PatientSearchResponse;
import org.openmrs.Patient;
import org.openmrs.RelationshipType;
import org.openmrs.annotation.Authorized;

import java.util.List;

public interface BahmniPatientService {
    @Authorized({"Get Patients"})
    List<PatientResponse> search(PatientSearchParameters searchParameters);
    @Authorized({"Get Patients"})
    List<PatientResponse> luceneSearch(PatientSearchParameters searchParameters);

    @Authorized({"Get Patients"})
    public List<Patient> get(String partialIdentifier, boolean shouldMatchExactPatientId);

    public PatientConfigResponse getConfig();

    public List<RelationshipType> getByAIsToB(String aIsToB);

    @Authorized({"Get Patients"})
    PatientSearchResponse search(PatientSearchRequest request);

}
