package org.bahmni.module.bahmnicommons.api.context.impl;

import org.bahmni.module.bahmnicommons.api.context.AppContext;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.CustomDatatypeUtil;

import java.util.List;

public class OpenMRSAppContext implements AppContext {
    @Override
    public <T> List<T> getRegisteredComponents(Class<T> type) {
        return Context.getRegisteredComponents(type);
    }

    @Override
    public CustomDatatype<?> getDatatype(String datatypeClassname, String datatypeConfig) {
        return CustomDatatypeUtil.getDatatype(datatypeClassname, datatypeConfig);
    }
}
