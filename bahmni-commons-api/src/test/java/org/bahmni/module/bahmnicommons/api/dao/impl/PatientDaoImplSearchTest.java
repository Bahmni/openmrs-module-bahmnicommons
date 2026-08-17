package org.bahmni.module.bahmnicommons.api.dao.impl;

import org.bahmni.module.bahmnicommons.api.search.builder.PatientCriteriaBuilder;
import org.bahmni.module.bahmnicommons.api.search.builder.PatientQueryContext;
import org.bahmni.search.model.SearchCondition;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Patient;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PatientDaoImplSearchTest {

    private static final String FETCH_NAMES = "names";
    private static final String FETCH_IDENTIFIERS = "identifiers";
    private static final String FETCH_ATTRIBUTES = "attributes";
    private static final String FIELD_VOIDED = "voided";

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private CriteriaQuery<Patient> criteriaQuery;

    @Mock
    private Root<Patient> root;

    @Mock
    private PatientCriteriaBuilder patientCriteriaBuilder;

    @Mock
    private Fetch namesFetch;

    @Mock
    private Fetch identifiersFetch;

    @Mock
    private Fetch attributesFetch;

    @Mock
    private Path<Boolean> voidedPath;

    @Mock
    private Path<Integer> patientIdPath;

    @Mock
    private Predicate voidedPredicate;

    @Mock
    private Order descOrder;

    @Mock
    private Query<Patient> hibernateQuery;

    private PatientDaoImpl patientDao;

    @Before
    public void setUp() {
        patientDao = new PatientDaoImpl(sessionFactory, patientCriteriaBuilder);

        when(sessionFactory.getCurrentSession()).thenReturn(session);
        when(session.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Patient.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Patient.class)).thenReturn(root);

        doReturn(namesFetch).when(root).fetch(eq(FETCH_NAMES), any(JoinType.class));
        doReturn(identifiersFetch).when(root).fetch(eq(FETCH_IDENTIFIERS), any(JoinType.class));
        doReturn(attributesFetch).when(root).fetch(eq(FETCH_ATTRIBUTES), any(JoinType.class));

        doReturn(voidedPath).when(root).get(FIELD_VOIDED);
        doReturn(patientIdPath).when(root).get("patientId");
        when(criteriaBuilder.isFalse(voidedPath)).thenReturn(voidedPredicate);
        when(criteriaBuilder.desc(any(Expression.class))).thenReturn(descOrder);
        when(criteriaBuilder.asc(any(Expression.class))).thenReturn(descOrder);

        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(criteriaQuery.distinct(true)).thenReturn(criteriaQuery);
        doReturn(criteriaQuery).when(criteriaQuery).where((Predicate[]) any());
        doReturn(criteriaQuery).when(criteriaQuery).orderBy((Order[]) any());

        when(session.createQuery(criteriaQuery)).thenReturn(hibernateQuery);
        when(hibernateQuery.setHint(anyString(), any())).thenReturn(hibernateQuery);
        when(hibernateQuery.setMaxResults(anyInt())).thenReturn(hibernateQuery);
    }

    @Test
    public void shouldFetchNamesIdentifiersAndAttributesToAvoidNPlusOne() {
        when(hibernateQuery.getResultList()).thenReturn(Collections.emptyList());

        patientDao.searchPatients(searchCondition(), null, "desc", null, 100);

        verify(root, times(1)).fetch(eq(FETCH_NAMES), eq(JoinType.LEFT));
        verify(root, times(1)).fetch(eq(FETCH_IDENTIFIERS), eq(JoinType.LEFT));
        verify(root, times(1)).fetch(eq(FETCH_ATTRIBUTES), eq(JoinType.LEFT));
    }

    @Test
    public void shouldExcludeVoidedPatients() {
        when(hibernateQuery.getResultList()).thenReturn(Collections.emptyList());

        patientDao.searchPatients(searchCondition(), null, "desc", null, 100);

        verify(root, times(1)).get(FIELD_VOIDED);
        verify(criteriaBuilder, times(1)).isFalse(voidedPath);
    }

    @Test
    public void shouldDelegateCriteriaToPatientCriteriaBuilder() {
        when(hibernateQuery.getResultList()).thenReturn(Collections.emptyList());
        SearchCondition condition = searchCondition();

        patientDao.searchPatients(condition, null, "desc", null, 100);

        verify(patientCriteriaBuilder, times(1)).apply(any(PatientQueryContext.class), eq(condition));
    }

    @Test
    public void shouldApplyDistinctAndPassDistinctThroughFalseHintToAvoidDuplicateRows() {
        when(hibernateQuery.getResultList()).thenReturn(Collections.emptyList());

        patientDao.searchPatients(searchCondition(), null, "desc", null, 100);

        verify(criteriaQuery, times(1)).distinct(true);
        verify(hibernateQuery, times(1)).setHint("hibernate.query.passDistinctThrough", false);
    }

    @Test
    public void shouldReturnPatientsReturnedByHibernateQuery() {
        Patient patient = new Patient();
        List<Patient> expected = Arrays.asList(patient);
        when(hibernateQuery.getResultList()).thenReturn(expected);

        List<Patient> actual = patientDao.searchPatients(searchCondition(), null, "desc", null, 100);

        assertThat(actual, is(expected));
    }

    @Test
    public void shouldApplyMaxResultsLimit() {
        when(hibernateQuery.getResultList()).thenReturn(Collections.emptyList());

        patientDao.searchPatients(searchCondition(), null, "desc", null, 50);

        verify(hibernateQuery, times(1)).setMaxResults(50);
    }

    @Test
    public void shouldOrderByPatientIdDescWhenSortOrderIsDesc() {
        when(hibernateQuery.getResultList()).thenReturn(Collections.emptyList());

        patientDao.searchPatients(searchCondition(), null, "desc", null, 100);

        verify(criteriaBuilder, times(1)).desc(patientIdPath);
        verify(criteriaQuery, times(1)).orderBy(any(Order.class));
    }

    private SearchCondition searchCondition() {
        SearchCondition condition = new SearchCondition();
        condition.setField("patient.names.familyName");
        condition.setComparator("eq");
        condition.setValue("Doe");
        return condition;
    }
}
