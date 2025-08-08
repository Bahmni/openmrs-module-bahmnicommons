package org.bahmni.module.bahmnicommons.api.service;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public interface ModuleAppConfigService {
    List<Object> getAppProperties(@NotNull List<String> moduleNames);
}
