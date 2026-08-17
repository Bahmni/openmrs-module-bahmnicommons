package org.bahmni.module.bahmnicommons.api.service.impl;


import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicommons.api.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicommons.api.contract.patient.response.PatientConfigResponse;
import org.bahmni.module.bahmnicommons.api.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicommons.api.search.builder.PatientResponseBuilder;
import org.bahmni.module.bahmnicommons.api.search.dto.PatientSearchRequest;
import org.bahmni.module.bahmnicommons.api.search.dto.PatientSearchResponse;
import org.bahmni.module.bahmnicommons.api.search.dto.SearchResponseMeta;
import org.bahmni.module.bahmnicommons.api.service.BahmniPatientService;
import org.bahmni.search.model.PaginationRequest;
import org.bahmni.search.model.PaginationResponse;
import org.bahmni.search.model.SearchRequestMeta;
import org.bahmni.search.pagination.PaginationHelper;
import org.bahmni.module.bahmnicommons.api.visitlocation.BahmniVisitLocationServiceImpl;
import org.bahmni.module.bahmnicommons.api.dao.PatientDao;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PersonAttributeType;
import org.openmrs.RelationshipType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Lazy //to toString rid of cyclic dependencies
@Transactional
public class BahmniPatientServiceImpl implements BahmniPatientService {
    private static final String ENTITY_PATIENT = "patient";

    private PersonService personService;
    private ConceptService conceptService;
    private PatientDao patientDao;
    private final PatientResponseBuilder patientResponseBuilder;

    private static final Logger log = LogManager.getLogger(BahmniPatientServiceImpl.class);

    //@Autowired
    public BahmniPatientServiceImpl(PersonService personService, ConceptService conceptService,
                                    PatientDao patientDao) {
        this(personService, conceptService, patientDao, new PatientResponseBuilder());
    }

    public BahmniPatientServiceImpl(PersonService personService, ConceptService conceptService,
                                    PatientDao patientDao, PatientResponseBuilder patientResponseBuilder) {
        this.personService = personService;
        this.conceptService = conceptService;
        this.patientDao = patientDao;
        this.patientResponseBuilder = patientResponseBuilder;
    }

    @Override
    public PatientConfigResponse getConfig() {
        List<PersonAttributeType> personAttributeTypes = personService.getAllPersonAttributeTypes();

        PatientConfigResponse patientConfigResponse = new PatientConfigResponse();
        for (PersonAttributeType personAttributeType : personAttributeTypes) {
            Concept attributeConcept = null;
            if (personAttributeType.getFormat().equals("org.openmrs.Concept")) {
                attributeConcept = conceptService.getConcept(personAttributeType.getForeignKey());
            }
            patientConfigResponse.addPersonAttribute(personAttributeType, attributeConcept);
        }
        return patientConfigResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> search(PatientSearchParameters searchParameters) {
        Supplier<Location> visitLocation = () -> getVisitLocation(searchParameters.getLoginLocationUuid());
        Supplier<List<String>> configuredAddressFields = () -> patientDao.getConfiguredPatientAddressFields();
        return patientDao.getPatients(searchParameters, visitLocation, configuredAddressFields);
    }

    @Override
    @Transactional
    public List<PatientResponse> luceneSearch(PatientSearchParameters searchParameters) {
        return patientDao.getPatientsUsingLuceneSearch(searchParameters.getIdentifier(),
                searchParameters.getName(),
                searchParameters.getCustomAttribute(),
                searchParameters.getAddressFieldName(),
                searchParameters.getAddressFieldValue(),
                searchParameters.getLength(),
                searchParameters.getStart(),
                searchParameters.getPatientAttributes(),
                searchParameters.getProgramAttributeFieldValue(),
                searchParameters.getProgramAttributeFieldName(),
                searchParameters.getAddressSearchResultFields(),
                searchParameters.getPatientSearchResultFields(),
                searchParameters.getLoginLocationUuid(),
                searchParameters.getFilterPatientsByLocation(),
                searchParameters.getFilterOnAllIdentifiers(),
                searchParameters.getAttributeToFilterOut(),
                searchParameters.getAttributeValueToFilterOut());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Patient> get(String partialIdentifier, boolean shouldMatchExactPatientId) {
        return patientDao.getPatients(partialIdentifier, shouldMatchExactPatientId);
    }

    @Override
    public List<RelationshipType> getByAIsToB(String aIsToB) {
        return patientDao.getByAIsToB(aIsToB);
    }

    @Override
    public PatientSearchResponse search(PatientSearchRequest request) {
        SearchRequestMeta meta = request.getMeta();
        PaginationRequest pagination = PaginationHelper.resolvePagination(meta);
        int effectiveLimit = PaginationHelper.resolveEffectiveLimit(pagination.getLimit());
        String sortOrder = PaginationHelper.resolveSortOrder(pagination.getSortOrder());
        String direction = pagination.getDirection();
        Long cursorId = PaginationHelper.decodeCursor(pagination.getCursor());
        boolean isPrev = PaginationHelper.isPrevDirection(direction);

        int fetchSize = effectiveLimit + 1;
        List<Patient> rawPatients = patientDao.searchPatients(
                request.getCriteria(), cursorId, sortOrder, direction, fetchSize);

        boolean hasMore = PaginationHelper.hasMore(rawPatients.size(), effectiveLimit);
        List<Patient> patients = PaginationHelper.trimAndOrient(rawPatients, effectiveLimit, isPrev);

        List<Map<String, Object>> results = new ArrayList<>();
        for (Patient patient : patients) {
            results.add(patientResponseBuilder.mapPatient(patient));
        }

        PaginationResponse paginationResponse = patients.isEmpty()
                ? PaginationHelper.emptyPaginationResponse()
                : PaginationHelper.buildPaginationResponse(
                        patients.get(0).getPatientId(),
                        patients.get(patients.size() - 1).getPatientId(),
                        hasMore, cursorId, isPrev);

        Long totalCount = PaginationHelper.resolveTotalCount(meta,
                () -> patientDao.countPatients(request.getCriteria()));

        SearchResponseMeta responseMeta = new SearchResponseMeta(paginationResponse, totalCount);
        return PatientSearchResponse.success(ENTITY_PATIENT, results, responseMeta);
    }

    private Location getVisitLocation(String loginLocationUuid) {

        if (StringUtils.isBlank(loginLocationUuid)) {
            return null;
        }
        BahmniVisitLocationServiceImpl bahmniVisitLocationService = new BahmniVisitLocationServiceImpl(Context.getLocationService());
        return bahmniVisitLocationService.getVisitLocation(loginLocationUuid);
    }

}
