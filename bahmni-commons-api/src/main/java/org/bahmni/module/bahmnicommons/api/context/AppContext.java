package org.bahmni.module.bahmnicommons.api.context;

import org.openmrs.customdatatype.CustomDatatype;

import java.util.List;

public interface AppContext {
     <T> List<T> getRegisteredComponents(Class<T> type);
     CustomDatatype<?> getDatatype(String datatypeClassname, String datatypeConfig);
}
