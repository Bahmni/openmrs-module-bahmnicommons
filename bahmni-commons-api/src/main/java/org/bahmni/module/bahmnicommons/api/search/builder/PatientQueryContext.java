package org.bahmni.module.bahmnicommons.api.search.builder;

import org.bahmni.search.builder.QueryContext;
import org.bahmni.search.model.SearchCondition;
import org.openmrs.Patient;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

public class PatientQueryContext extends QueryContext<Patient> {

    public SearchCondition currentGroup;

    public PatientQueryContext(CriteriaBuilder criteriaBuilder, Root<Patient> root, List<Predicate> predicates) {
        super(criteriaBuilder, root, predicates);
    }
}
