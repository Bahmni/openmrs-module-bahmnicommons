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
import org.bahmni.module.bahmnicommons.api.service.BahmniPatientService;
import org.bahmni.search.model.SearchRequestMeta;
import org.bahmni.search.model.SearchResponseMeta;
import org.bahmni.search.pagination.PageResult;
import org.bahmni.search.pagination.PaginationHelper;
import org.bahmni.search.pagination.ResolvedPagination;
import org.bahmni.module.bahmnicommons.api.visitlocation.BahmniVisitLocationServiceImpl;
import org.bahmni.module.bahmnicommons.api.dao.PatientDao;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PersonAttributeType;
import org.openmrs.RelationshipType;
import org.openmrs.api.AdministrationService;
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

    private static final String GP_PAGINATION_DEFAULT_LIMIT = "bahmni.search.pagination.defaultLimit";
    private static final String GP_PAGINATION_MAX_LIMIT = "bahmni.search.pagination.maxLimit";

    private static final int FALLBACK_DEFAULT_LIMIT = 100;
    private static final int FALLBACK_MAX_LIMIT = 500;

    private PersonService personService;
    private ConceptService conceptService;
    private PatientDao patientDao;
    private final PatientResponseBuilder patientResponseBuilder;
    private final AdministrationService administrationService;

    private static final Logger log = LogManager.getLogger(BahmniPatientServiceImpl.class);

    public BahmniPatientServiceImpl(PersonService personService, ConceptService conceptService,
                                    PatientDao patientDao, PatientResponseBuilder patientResponseBuilder,
                                    AdministrationService administrationService) {
        this.personService = personService;
        this.conceptService = conceptService;
        this.patientDao = patientDao;
        this.patientResponseBuilder = patientResponseBuilder;
        this.administrationService = administrationService;
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
        int defaultLimit = PaginationHelper.resolveGlobalProperty(
                administrationService.getGlobalProperty(GP_PAGINATION_DEFAULT_LIMIT), FALLBACK_DEFAULT_LIMIT, GP_PAGINATION_DEFAULT_LIMIT);
        int maxLimit = PaginationHelper.resolveGlobalProperty(
                administrationService.getGlobalProperty(GP_PAGINATION_MAX_LIMIT), FALLBACK_MAX_LIMIT, GP_PAGINATION_MAX_LIMIT);

        ResolvedPagination resolved = PaginationHelper.resolvePaginationContext(meta, ENTITY_PATIENT, defaultLimit, maxLimit);

        List<Integer> matchingIds = patientDao.findMatchingIds(
                request.getCriteria(), resolved.getCursorId(), resolved.getSortOrder(),
                resolved.getDirection(), resolved.getFetchSize());

        boolean hasMore = PaginationHelper.hasMore(matchingIds.size(), resolved.getEffectiveLimit());
        List<Integer> idsToFetch = hasMore
                ? matchingIds.subList(0, resolved.getEffectiveLimit())
                : matchingIds;

        List<Patient> rawPatients = patientDao.findByIds(idsToFetch);

        PageResult<Patient> pageResult = PaginationHelper.paginate(
                ENTITY_PATIENT, rawPatients, Patient::getPatientId, resolved, hasMore);

        List<Map<String, Object>> results = new ArrayList<>();
        for (Patient patient : pageResult.getItems()) {
            results.add(patientResponseBuilder.mapPatient(patient));
        }

        Long totalCount = PaginationHelper.resolveTotalCount(meta,
                () -> patientDao.countPatients(request.getCriteria()));

        SearchResponseMeta responseMeta = new SearchResponseMeta(pageResult.getPaginationResponse(), totalCount);
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


