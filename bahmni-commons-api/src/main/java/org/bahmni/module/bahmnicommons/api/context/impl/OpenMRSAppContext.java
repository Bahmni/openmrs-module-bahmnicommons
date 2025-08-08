package org.bahmni.module.bahmnicommons.api.context.impl;

import org.bahmni.module.bahmnicommons.api.context.AppContext;
import org.openmrs.api.context.Context;

import java.util.List;

public class OpenMRSAppContext implements AppContext {
    @Override
    public <T> List<T> getRegisteredComponents(Class<T> type) {
        return Context.getRegisteredComponents(type);
    }
}
