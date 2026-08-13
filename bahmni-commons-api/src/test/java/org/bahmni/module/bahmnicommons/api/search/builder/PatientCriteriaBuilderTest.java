package org.bahmni.module.bahmnicommons.api.search.builder;

import org.bahmni.module.bahmnicommons.api.search.PatientSearchFields;
import org.bahmni.module.bahmnicommons.api.search.SearchKeyConstants;
import org.bahmni.search.model.SearchCondition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Patient;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PatientCriteriaBuilderTest {

    private static final String IDENTIFIER_TYPE_UUID = "3cc26720-39cd-4cbe-a0db-c9916f5f8e79";
    private static final String IDENTIFIER_VALUE = "djfk";

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Root<Patient> root;

    @Mock
    private Join<Object, Object> identifiersJoin;

    @Mock
    private Join<Object, Object> identifierTypeJoin;

    @Mock
    private Path voidedPath;

    @Mock
    private Predicate voidedPredicate;

    @Mock
    private Path uuidPath;

    @Mock
    private Path namePath;

    @Mock
    private Path identifierValuePath;

    @Mock
    private Predicate uuidMatchPredicate;

    @Mock
    private Predicate nameMatchPredicate;

    @Mock
    private Predicate kindPredicate;

    @Mock
    private Predicate valuePredicate;

    @Mock
    private Predicate combinedAndPredicate;

    private PatientCriteriaBuilder patientCriteriaBuilder;
    private List<Predicate> predicates;
    private PatientQueryContext queryContext;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        patientCriteriaBuilder = new PatientCriteriaBuilder();
        predicates = new ArrayList<>();
        queryContext = new PatientQueryContext(criteriaBuilder, root, predicates);

        // Single join to the "identifiers" collection is created and cached.
        doReturn(identifiersJoin).when(root).join(eq(SearchKeyConstants.PATIENT_IDENTIFIERS), any(JoinType.class));
        doReturn(voidedPath).when(identifiersJoin).get(SearchKeyConstants.COMMON_VOIDED);
        when(criteriaBuilder.isFalse(voidedPath)).thenReturn(voidedPredicate);

        // Single join from identifiers -> identifierType, off of the SAME identifiersJoin instance.
        doReturn(identifierTypeJoin).when(identifiersJoin).join(eq(SearchKeyConstants.IDENTIFIER_TYPE), any(JoinType.class));
        doReturn(uuidPath).when(identifierTypeJoin).get(SearchKeyConstants.COMMON_UUID);
        doReturn(namePath).when(identifierTypeJoin).get(SearchKeyConstants.COMMON_NAME);
        when(criteriaBuilder.equal(uuidPath, IDENTIFIER_TYPE_UUID)).thenReturn(uuidMatchPredicate);
        when(criteriaBuilder.equal(namePath, IDENTIFIER_TYPE_UUID)).thenReturn(nameMatchPredicate);
        when(criteriaBuilder.or(uuidMatchPredicate, nameMatchPredicate)).thenReturn(kindPredicate);

        doReturn(identifierValuePath).when(identifiersJoin).get(SearchKeyConstants.IDENTIFIER_VALUE);
        when(criteriaBuilder.equal(identifierValuePath, IDENTIFIER_VALUE)).thenReturn(valuePredicate);

        when(criteriaBuilder.and(new Predicate[]{kindPredicate, valuePredicate})).thenReturn(combinedAndPredicate);
    }


    @Test
    public void shouldReuseSameIdentifierJoinForKindAndValueConditionsInAndGroup() {
        SearchCondition group = buildKindAndValueGroup(IDENTIFIER_TYPE_UUID, IDENTIFIER_VALUE);

        patientCriteriaBuilder.apply(queryContext, group);

        verify(root, times(1)).join(eq(SearchKeyConstants.PATIENT_IDENTIFIERS), any(JoinType.class));
        verify(identifiersJoin, times(1)).join(eq(SearchKeyConstants.IDENTIFIER_TYPE), any(JoinType.class));
    }

    @Test
    public void shouldCombineKindAndValuePredicatesWithAndForTheSameIdentifierRow() {
        SearchCondition group = buildKindAndValueGroup(IDENTIFIER_TYPE_UUID, IDENTIFIER_VALUE);

        patientCriteriaBuilder.apply(queryContext, group);

        verify(criteriaBuilder, times(1)).and(new Predicate[]{kindPredicate, valuePredicate});
        assertThat(predicates, hasItem(combinedAndPredicate));
    }

    @Test
    public void shouldExcludeVoidedIdentifiersOnlyOnceEvenWithMultipleFieldsOnSameJoin() {
        SearchCondition group = buildKindAndValueGroup(IDENTIFIER_TYPE_UUID, IDENTIFIER_VALUE);

        patientCriteriaBuilder.apply(queryContext, group);

        long voidedPredicateCount = predicates.stream().filter(p -> p == voidedPredicate).count();
        assertThat(voidedPredicateCount, is(1L));
    }

    private SearchCondition buildKindAndValueGroup(String kindValue, String identifierValue) {
        SearchCondition kindLeaf = new SearchCondition();
        kindLeaf.setField(PatientSearchFields.PATIENT_IDENTIFIER_KIND);
        kindLeaf.setComparator("eq");
        kindLeaf.setValue(kindValue);

        SearchCondition valueLeaf = new SearchCondition();
        valueLeaf.setField(PatientSearchFields.PATIENT_IDENTIFIER_VALUE);
        valueLeaf.setComparator("eq");
        valueLeaf.setValue(identifierValue);

        SearchCondition group = new SearchCondition();
        group.setOperator("AND");
        group.setConditions(Arrays.asList(kindLeaf, valueLeaf));
        return group;
    }
}
