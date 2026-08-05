package org.bahmni.module.bahmnicommons.api.search.builder;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PatientResponseBuilder {

    private static final String KEY_UUID = "uuid";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_BIRTHDATE = "birthdate";
    private static final String KEY_VOIDED = "voided";
    private static final String KEY_NAME = "name";
    private static final String KEY_GIVEN_NAME = "givenName";
    private static final String KEY_MIDDLE_NAME = "middleName";
    private static final String KEY_FAMILY_NAME = "familyName";
    private static final String KEY_FAMILY_NAME_2 = "familyName2";
    private static final String KEY_IDENTIFIERS = "identifiers";
    private static final String KEY_IDENTIFIER = "identifier";
    private static final String KEY_PREFERRED = "preferred";
    private static final String KEY_ATTRIBUTES = "attributes";
    private static final String KEY_VALUE = "value";
    private static final String KEY_TYPE = "type";
    private static final String KEY_NAME_FIELD = "name";

    private static final DateTimeFormatter ISO_DATETIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

    public Map<String, Object> mapPatient(Patient patient) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(KEY_UUID, patient.getUuid());
        map.put(KEY_GENDER, patient.getGender());
        map.put(KEY_BIRTHDATE, formatIsoDateTime(patient.getBirthdate()));
        map.put(KEY_VOIDED, Boolean.TRUE.equals(patient.getVoided()));
        map.put(KEY_NAME, buildNameMap(patient.getPersonName()));
        map.put(KEY_IDENTIFIERS, buildIdentifiersList(patient));
        map.put(KEY_ATTRIBUTES, buildAttributesList(patient));
        return map;
    }

    private Map<String, Object> buildNameMap(PersonName name) {
        if (name == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(KEY_GIVEN_NAME, name.getGivenName());
        map.put(KEY_MIDDLE_NAME, name.getMiddleName());
        map.put(KEY_FAMILY_NAME, name.getFamilyName());
        map.put(KEY_FAMILY_NAME_2, name.getFamilyName2());
        map.put(KEY_VOIDED, Boolean.TRUE.equals(name.getVoided()));
        return map;
    }

    private List<Map<String, Object>> buildIdentifiersList(Patient patient) {
        List<Map<String, Object>> identifiersList = new ArrayList<>();
        List<PatientIdentifier> identifiers = patient.getActiveIdentifiers();
        if (identifiers == null || identifiers.isEmpty()) {
            return identifiersList;
        }
        for (PatientIdentifier patientIdentifier : identifiers) {
            Map<String, Object> identifierMap = new LinkedHashMap<>();
            identifierMap.put(KEY_TYPE, buildIdentifierTypeMap(patientIdentifier.getIdentifierType()));
            identifierMap.put(KEY_IDENTIFIER, patientIdentifier.getIdentifier());
            identifierMap.put(KEY_PREFERRED, Boolean.TRUE.equals(patientIdentifier.getPreferred()));
            identifiersList.add(identifierMap);
        }
        return identifiersList;
    }

    private Map<String, Object> buildIdentifierTypeMap(PatientIdentifierType identifierType) {
        if (identifierType == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(KEY_UUID, identifierType.getUuid());
        map.put(KEY_NAME_FIELD, identifierType.getName());
        return map;
    }

    private List<Map<String, Object>> buildAttributesList(Patient patient) {
        List<Map<String, Object>> attributesList = new ArrayList<>();
        List<PersonAttribute> attributes = patient.getActiveAttributes();
        if (attributes == null || attributes.isEmpty()) {
            return attributesList;
        }
        for (PersonAttribute attribute : attributes) {
            Map<String, Object> attributeMap = new LinkedHashMap<>();
            attributeMap.put(KEY_TYPE, buildAttributeTypeMap(attribute.getAttributeType()));
            attributeMap.put(KEY_VALUE, attribute.getValue());
            attributesList.add(attributeMap);
        }
        return attributesList;
    }

    private Map<String, Object> buildAttributeTypeMap(PersonAttributeType attributeType) {
        if (attributeType == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(KEY_UUID, attributeType.getUuid());
        map.put(KEY_NAME_FIELD, attributeType.getName());
        return map;
    }

    private String formatIsoDateTime(Date date) {
        if (date == null) return null;
        return ISO_DATETIME.format(date.toInstant());
    }
}
