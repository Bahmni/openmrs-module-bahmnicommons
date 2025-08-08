package org.bahmni.module.bahmnicommons.api.context;

import java.util.List;

public interface AppContext {
     <T> List<T> getRegisteredComponents(Class<T> type);
}
