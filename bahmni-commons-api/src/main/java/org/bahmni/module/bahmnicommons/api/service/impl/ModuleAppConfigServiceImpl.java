package org.bahmni.module.bahmnicommons.api.service.impl;

import org.bahmni.module.bahmnicommons.api.configuration.ModuleAppConfig;
import org.bahmni.module.bahmnicommons.api.service.ModuleAppConfigService;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;


public class ModuleAppConfigServiceImpl implements ModuleAppConfigService {
    @Override
    @Transactional(readOnly = true)
    public List<Object> getAppProperties(@NotNull List<String> moduleNames) {
        AdministrationService administrationService = Context.getAdministrationService();
        List<ModuleAppConfig> appConfigs = Context.getRegisteredComponents(ModuleAppConfig.class);
        return appConfigs.stream()
                .filter(moduleAppConfig -> moduleNames.contains(moduleAppConfig.getModuleName()))
                .flatMap(cfg -> cfg.getGlobalAppProperties().stream())
                .distinct()
                .map(property -> propertyWith(property, administrationService.getGlobalProperty(property)))
                .collect(Collectors.toList());
    }

    private SimpleObject propertyWith(String property, String value) {
        SimpleObject simpleObject = new SimpleObject();
        simpleObject.add("property", property);
        simpleObject.add("value", value);
        return simpleObject;
    }

}
