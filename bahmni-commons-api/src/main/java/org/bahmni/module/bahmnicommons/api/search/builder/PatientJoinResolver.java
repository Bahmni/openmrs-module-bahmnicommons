package org.bahmni.module.bahmnicommons.api.search.builder;

import org.bahmni.module.bahmnicommons.api.search.SearchKeyConstants;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

public class PatientJoinResolver {

    private static final String JOIN_KEY_NAMES = "names";
    private static final String JOIN_KEY_ATTRIBUTES = "attributes";
    private static final String JOIN_KEY_ATTRIBUTE_TYPE = "attributeType";
    private static final String JOIN_KEY_IDENTIFIERS = "identifiers";
    private static final String JOIN_KEY_IDENTIFIER_TYPE = "identifierType";
    private static final String JOIN_KEY_IDENTIFIER_LOCATION = "identifierLocation";


    From<?, ?> joinNames(PatientQueryContext queryContext) {
        From<?, ?> cached = queryContext.joinCache.get(JOIN_KEY_NAMES);
        if (cached != null) {
            return cached;
        }
        Join<?, ?> namesJoin = queryContext.root.join(SearchKeyConstants.PATIENT_NAMES, JoinType.INNER);
        queryContext.predicates.add(queryContext.criteriaBuilder.isFalse(namesJoin.get(SearchKeyConstants.COMMON_VOIDED)));
        queryContext.joinCache.put(JOIN_KEY_NAMES, namesJoin);
        return namesJoin;
    }

    From<?, ?> joinAttributes(PatientQueryContext queryContext) {
        From<?, ?> cached = queryContext.joinCache.get(JOIN_KEY_ATTRIBUTES);
        if (cached != null) {
            return cached;
        }
        Join<?, ?> attributesJoin = queryContext.root.join(SearchKeyConstants.PATIENT_ATTRIBUTES, JoinType.INNER);
        queryContext.predicates.add(queryContext.criteriaBuilder.isFalse(attributesJoin.get(SearchKeyConstants.COMMON_VOIDED)));
        queryContext.joinCache.put(JOIN_KEY_ATTRIBUTES, attributesJoin);
        return attributesJoin;
    }

    From<?, ?> joinAttributeType(PatientQueryContext queryContext) {
        From<?, ?> cached = queryContext.joinCache.get(JOIN_KEY_ATTRIBUTE_TYPE);
        if (cached != null) {
            return cached;
        }
        // NOTE: joinAttributes() is called here, OUTSIDE of any computeIfAbsent lambda,
        // so it is free to safely mutate queryContext.joinCache itself (caching the
        // "attributes" join) without triggering a ConcurrentModificationException.
        From<?, ?> attributesJoin = joinAttributes(queryContext);
        From<?, ?> attributeTypeJoin = attributesJoin.join(SearchKeyConstants.ATTRIBUTE_TYPE, JoinType.INNER);
        queryContext.joinCache.put(JOIN_KEY_ATTRIBUTE_TYPE, attributeTypeJoin);
        return attributeTypeJoin;
    }

    From<?, ?> joinIdentifiers(PatientQueryContext queryContext) {
        From<?, ?> cached = queryContext.joinCache.get(JOIN_KEY_IDENTIFIERS);
        if (cached != null) {
            return cached;
        }
        Join<?, ?> identifiersJoin = queryContext.root.join(SearchKeyConstants.PATIENT_IDENTIFIERS, JoinType.INNER);
        queryContext.predicates.add(queryContext.criteriaBuilder.isFalse(identifiersJoin.get(SearchKeyConstants.COMMON_VOIDED)));
        queryContext.joinCache.put(JOIN_KEY_IDENTIFIERS, identifiersJoin);
        return identifiersJoin;
    }

    From<?, ?> joinIdentifierType(PatientQueryContext queryContext) {
        From<?, ?> cached = queryContext.joinCache.get(JOIN_KEY_IDENTIFIER_TYPE);
        if (cached != null) {
            return cached;
        }
        // NOTE: joinIdentifiers() is called here, OUTSIDE of any computeIfAbsent lambda,
        // so it is free to safely mutate queryContext.joinCache itself (caching the
        // "identifiers" join) without triggering a ConcurrentModificationException.
        // This is precisely the code path exercised by a request combining
        // "patient.identifiers.kind" and "patient.identifiers.value" in the same AND group.
        From<?, ?> identifiersJoin = joinIdentifiers(queryContext);
        From<?, ?> identifierTypeJoin = identifiersJoin.join(SearchKeyConstants.IDENTIFIER_TYPE, JoinType.INNER);
        queryContext.joinCache.put(JOIN_KEY_IDENTIFIER_TYPE, identifierTypeJoin);
        return identifierTypeJoin;
    }

    From<?, ?> joinIdentifierLocation(PatientQueryContext queryContext) {
        From<?, ?> cached = queryContext.joinCache.get(JOIN_KEY_IDENTIFIER_LOCATION);
        if (cached != null) {
            return cached;
        }
        From<?, ?> identifiersJoin = joinIdentifiers(queryContext);
        From<?, ?> identifierLocationJoin = identifiersJoin.join(SearchKeyConstants.IDENTIFIER_LOCATION, JoinType.INNER);
        queryContext.joinCache.put(JOIN_KEY_IDENTIFIER_LOCATION, identifierLocationJoin);
        return identifierLocationJoin;
    }
}
