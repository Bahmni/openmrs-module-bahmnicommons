package org.bahmni.module.bahmnicommons.api.search.builder;

import org.bahmni.module.bahmnicommons.api.search.PatientSearchFields;
import org.bahmni.module.bahmnicommons.api.search.SearchKeyConstants;
import org.bahmni.search.builder.SearchFieldPredicate;
import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.exceptions.SearchResponseErrorStatus;
import org.bahmni.search.model.ConditionOperator;
import org.bahmni.search.model.FieldComparator;
import org.bahmni.search.model.FieldType;
import org.bahmni.search.model.SearchCondition;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PatientCriteriaBuilder {

    private static final DateTimeFormatter ISO_DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private final PatientJoinResolver joinResolver = new PatientJoinResolver();
    private final Map<String, SearchFieldPredicate> fieldRegistry;

    public PatientCriteriaBuilder() {
        this.fieldRegistry = Collections.unmodifiableMap(buildFieldRegistry());
    }

    public void apply(PatientQueryContext queryContext, SearchCondition criteria) {
        Predicate predicate = buildCriterion(queryContext, criteria);
        if (predicate != null) {
            queryContext.predicates.add(predicate);
        }
    }

    private Map<String, SearchFieldPredicate> buildFieldRegistry() {
        Map<String, SearchFieldPredicate> registry = new HashMap<>();

        registry.put(PatientSearchFields.PATIENT_FAMILY_NAME,
                createFieldPredicate(joinResolver::joinNames, SearchKeyConstants.PERSON_FAMILY_NAME, FieldType.STRING));
        registry.put(PatientSearchFields.PATIENT_GIVEN_NAME,
                createFieldPredicate(joinResolver::joinNames, SearchKeyConstants.PERSON_GIVEN_NAME, FieldType.STRING));
        registry.put(PatientSearchFields.PATIENT_MIDDLE_NAME,
                createFieldPredicate(joinResolver::joinNames, SearchKeyConstants.PERSON_MIDDLE_NAME, FieldType.STRING));

        registry.put(PatientSearchFields.PATIENT_GENDER,
                createFieldPredicate(queryContext -> queryContext.root, SearchKeyConstants.PATIENT_GENDER, FieldType.STRING));
        registry.put(PatientSearchFields.PATIENT_BIRTHDATE,
                createFieldPredicate(queryContext -> queryContext.root, SearchKeyConstants.PATIENT_BIRTHDATE, FieldType.DATE));
        registry.put(PatientSearchFields.PATIENT_REGISTRATION_DATE,
                createFieldPredicate(queryContext -> queryContext.root, SearchKeyConstants.PATIENT_DATE_CREATED, FieldType.DATE));


        registry.put(PatientSearchFields.PATIENT_ATTRIBUTE_KIND, this::buildAttributeKindPredicate);
        registry.put(PatientSearchFields.PATIENT_ATTRIBUTE_VALUE,
                createFieldPredicate(joinResolver::joinAttributes, SearchKeyConstants.ATTRIBUTE_VALUE, FieldType.STRING));

        registry.put(PatientSearchFields.PATIENT_IDENTIFIER_KIND, this::buildIdentifierKindPredicate);
        registry.put(PatientSearchFields.PATIENT_IDENTIFIER_VALUE,
                createFieldPredicate(joinResolver::joinIdentifiers, SearchKeyConstants.IDENTIFIER_VALUE, FieldType.STRING));

        return registry;
    }


    private SearchFieldPredicate createFieldPredicate(Function<PatientQueryContext, From<?, ?>> joinFunction,
                                                       String propertyName, FieldType fieldType) {
        return (queryContext, fieldName, comparator, value, operator) -> {
            validateComparator(fieldName, comparator, fieldType);
            @SuppressWarnings("unchecked")
            Path<?> fieldPath = joinFunction.apply((PatientQueryContext) queryContext).get(propertyName);
            return buildPredicate(queryContext.criteriaBuilder, fieldPath, comparator, value);
        };
    }

    private Predicate buildAttributeKindPredicate(org.bahmni.search.builder.QueryContext<?> ctx, String fieldName,
                                                  FieldComparator comparator, String value,
                                                  ConditionOperator operator) {
        validateComparator(fieldName, comparator, FieldType.STRING);
        PatientQueryContext queryContext = (PatientQueryContext) ctx;
        From<?, ?> attributeTypeJoin = joinResolver.joinAttributeType(queryContext);
        return buildKindMatchPredicate(queryContext.criteriaBuilder, attributeTypeJoin, value, operator);
    }

    private Predicate buildIdentifierKindPredicate(org.bahmni.search.builder.QueryContext<?> ctx, String fieldName,
                                                   FieldComparator comparator, String value,
                                                   ConditionOperator operator) {
        validateComparator(fieldName, comparator, FieldType.STRING);
        PatientQueryContext queryContext = (PatientQueryContext) ctx;
        From<?, ?> identifierTypeJoin = joinResolver.joinIdentifierType(queryContext);
        return buildKindMatchPredicate(queryContext.criteriaBuilder, identifierTypeJoin, value, operator);
    }
    
    private Predicate buildKindMatchPredicate(CriteriaBuilder criteriaBuilder, From<?, ?> typeJoin,
                                              String value, ConditionOperator operator) {
        Predicate uuidMatch = criteriaBuilder.equal(typeJoin.get(SearchKeyConstants.COMMON_UUID), value);
        Predicate nameMatch = criteriaBuilder.equal(typeJoin.get(SearchKeyConstants.COMMON_NAME), value);

        ConditionOperator effectiveOperator = operator != null ? operator : ConditionOperator.OR;
        return effectiveOperator == ConditionOperator.AND
                ? criteriaBuilder.and(uuidMatch, nameMatch)
                : criteriaBuilder.or(uuidMatch, nameMatch);
    }

    private Predicate buildCriterion(PatientQueryContext queryContext, SearchCondition criteria) {
        if (criteria == null) {
            return null;
        }
        if (criteria.isLeaf()) {
            return buildLeafCriterion(queryContext, criteria);
        }
        return combineChildPredicates(queryContext, criteria);
    }

    private Predicate buildLeafCriterion(PatientQueryContext queryContext, SearchCondition leafCriteria) {
        String fieldName = leafCriteria.getField();
        FieldComparator comparator = leafCriteria.getComparator();

        SearchFieldPredicate fieldPredicate = fieldRegistry.get(fieldName);
        if (fieldPredicate == null) {
            throw new InvalidSearchCriteriaException(
                    "Unknown search field: '" + fieldName + "'",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }

        return fieldPredicate.build(queryContext, fieldName, comparator,
                leafCriteria.getValue(), leafCriteria.getOperator());
    }

    private Predicate combineChildPredicates(PatientQueryContext queryContext, SearchCondition parentCriteria) {
        SearchCondition previousGroup = queryContext.currentGroup;
        queryContext.currentGroup = parentCriteria;
        try {
            List<Predicate> childPredicates = new ArrayList<>();
            if (parentCriteria.getConditions() != null) {
                for (SearchCondition childCriteria : parentCriteria.getConditions()) {
                    Predicate resolvedPredicate = buildCriterion(queryContext, childCriteria);
                    if (resolvedPredicate != null) {
                        childPredicates.add(resolvedPredicate);
                    }
                }
            }

            if (childPredicates.isEmpty()) {
                return null;
            }

            Predicate[] predicateArray = childPredicates.toArray(new Predicate[0]);
            return parentCriteria.getOperator() == ConditionOperator.OR
                    ? queryContext.criteriaBuilder.or(predicateArray)
                    : queryContext.criteriaBuilder.and(predicateArray);
        } finally {
            queryContext.currentGroup = previousGroup;
        }
    }


    @SuppressWarnings("unchecked")
    private Predicate buildPredicate(CriteriaBuilder criteriaBuilder, Path<?> fieldPath,
                                     FieldComparator comparator, String value) {
        switch (comparator) {
            case EQ: return criteriaBuilder.equal(fieldPath, value);
            case GT: return criteriaBuilder.greaterThan((Path<Date>) fieldPath, parseDate(value));
            case LT: return criteriaBuilder.lessThan((Path<Date>) fieldPath, parseDate(value));
            case GE: return criteriaBuilder.greaterThanOrEqualTo((Path<Date>) fieldPath, parseDate(value));
            case LE: return criteriaBuilder.lessThanOrEqualTo((Path<Date>) fieldPath, parseDate(value));
            default:
                throw new InvalidSearchCriteriaException(
                        "Unsupported comparator: " + comparator,
                        SearchResponseErrorStatus.BAD_REQUEST);
        }
    }

    private void validateComparator(String fieldName, FieldComparator comparator, FieldType fieldType) {
        if (!fieldType.supports(comparator)) {
            throw new InvalidSearchCriteriaException(
                    "Comparator '" + comparator.name().toLowerCase()
                            + "' is not supported for field '" + fieldName
                            + "'. Supported: " + fieldType.getSupportedComparators().toString().toLowerCase(),
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
    }

    private Date parseDate(String dateValue) {
        try {
            return Date.from(OffsetDateTime.parse(dateValue, ISO_DATETIME_FORMAT).toInstant());
        } catch (DateTimeParseException exception) {
            throw new InvalidSearchCriteriaException(
                    "Invalid date format: '" + dateValue
                            + "'. Expected yyyy-MM-dd'T'HH:mm:ss.SSSZ (e.g. 2024-01-01T10:30:00.000+0530)",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
    }
}
