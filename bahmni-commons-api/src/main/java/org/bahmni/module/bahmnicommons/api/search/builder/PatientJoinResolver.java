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

    private String scopedKey(PatientQueryContext queryContext, String baseKey) {
        return baseKey + "#" + System.identityHashCode(queryContext.currentGroup);
    }

    From<?, ?> joinAttributes(PatientQueryContext queryContext) {
        String cacheKey = scopedKey(queryContext, JOIN_KEY_ATTRIBUTES);
        From<?, ?> cached = queryContext.joinCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        Join<?, ?> attributesJoin = queryContext.root.join(SearchKeyConstants.PATIENT_ATTRIBUTES, JoinType.INNER);
        queryContext.predicates.add(queryContext.criteriaBuilder.isFalse(attributesJoin.get(SearchKeyConstants.COMMON_VOIDED)));
        queryContext.joinCache.put(cacheKey, attributesJoin);
        return attributesJoin;
    }

    From<?, ?> joinAttributeType(PatientQueryContext queryContext) {
        String cacheKey = scopedKey(queryContext, JOIN_KEY_ATTRIBUTE_TYPE);
        From<?, ?> cached = queryContext.joinCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        From<?, ?> attributesJoin = joinAttributes(queryContext);
        From<?, ?> attributeTypeJoin = attributesJoin.join(SearchKeyConstants.ATTRIBUTE_TYPE, JoinType.INNER);
        queryContext.joinCache.put(cacheKey, attributeTypeJoin);
        return attributeTypeJoin;
    }

    From<?, ?> joinIdentifiers(PatientQueryContext queryContext) {
        String cacheKey = scopedKey(queryContext, JOIN_KEY_IDENTIFIERS);
        From<?, ?> cached = queryContext.joinCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        Join<?, ?> identifiersJoin = queryContext.root.join(SearchKeyConstants.PATIENT_IDENTIFIERS, JoinType.INNER);
        queryContext.predicates.add(queryContext.criteriaBuilder.isFalse(identifiersJoin.get(SearchKeyConstants.COMMON_VOIDED)));
        queryContext.joinCache.put(cacheKey, identifiersJoin);
        return identifiersJoin;
    }

    From<?, ?> joinIdentifierType(PatientQueryContext queryContext) {
        String cacheKey = scopedKey(queryContext, JOIN_KEY_IDENTIFIER_TYPE);
        From<?, ?> cached = queryContext.joinCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        From<?, ?> identifiersJoin = joinIdentifiers(queryContext);
        From<?, ?> identifierTypeJoin = identifiersJoin.join(SearchKeyConstants.IDENTIFIER_TYPE, JoinType.INNER);
        queryContext.joinCache.put(cacheKey, identifierTypeJoin);
        return identifierTypeJoin;
    }
}
