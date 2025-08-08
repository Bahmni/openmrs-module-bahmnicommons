package org.bahmni.module.bahmnicommons.api.configuration;

import java.util.List;

public interface ModuleAppConfig {
    String getModuleName();
    List<String> getGlobalAppProperties();
}
