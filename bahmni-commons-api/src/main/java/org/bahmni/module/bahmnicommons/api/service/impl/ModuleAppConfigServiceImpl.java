package org.bahmni.module.bahmnicommons.api.service.impl;

import org.bahmni.module.bahmnicommons.api.configuration.ModuleAppConfig;
import org.bahmni.module.bahmnicommons.api.context.AppContext;
import org.bahmni.module.bahmnicommons.api.service.ModuleAppConfigService;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;


public class ModuleAppConfigServiceImpl implements ModuleAppConfigService {

    private AdministrationService administrationService;
    private AppContext appContext;

    public ModuleAppConfigServiceImpl(AppContext appContext, @Qualifier("adminService") AdministrationService administrationService) {
        this.administrationService = administrationService;
        this.appContext = appContext;
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable( value = "bahmnicommonsModuleAppProperties" )
    public List<Object> getAppProperties(@NotNull List<String> moduleNames) {
        List<ModuleAppConfig> appConfigs = appContext.getRegisteredComponents(ModuleAppConfig.class);
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
