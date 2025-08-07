package org.bahmni.module.bahmnicommons.api.service;

import org.bahmni.module.bahmnicommons.api.configuration.CommonAppConfig;
import org.bahmni.module.bahmnicommons.api.configuration.ModuleAppConfig;
import org.bahmni.module.bahmnicommons.api.context.AppContext;
import org.bahmni.module.bahmnicommons.api.service.impl.ModuleAppConfigServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class ModuleAppConfigServiceTest {

    @Mock
    AdministrationService administrationService;
    @Mock
    AppContext appContext;
    private ModuleAppConfigServiceImpl moduleAppConfigService;

    @Before
    public void setUp() {
        Mockito.when(appContext.getRegisteredComponents(ModuleAppConfig.class)).thenReturn(Arrays.asList(new CommonAppConfig()));
        moduleAppConfigService = new ModuleAppConfigServiceImpl(appContext, administrationService);
    }

    @Test
    public void shouldReturnSettingsForCommonModule() {
        List<Object> settings = moduleAppConfigService.getAppProperties(Arrays.asList("commons"));
        Assert.assertEquals("Expected number of setting for commons app is incorrect", 8, settings.size());
    }

    @Test
    public void shouldNotReturnAnySettingForUnknownModule() {
        List<Object> settings = moduleAppConfigService.getAppProperties(Arrays.asList("test"));
        Assert.assertEquals("Expected 0 settings for unknown module name", 0, settings.size());
    }

    @Test
    public void shouldReturnLocaleFromCommonModule() {
        Mockito.when(administrationService.getGlobalProperty("default_locale")).thenReturn("en-IN");
        List<Object> settings = moduleAppConfigService.getAppProperties(Arrays.asList("commons"));
        Optional<Object> localeSetting = findSetting(settings, "default_locale");
        if (!localeSetting.isPresent()) {
            Assert.fail("Did not find setting for default locale");
        }
        Assert.assertEquals("en-IN", gePropertyValue(localeSetting.get(), "value"));
    }

    private Object gePropertyValue(Object property, String value) {
        return ((SimpleObject) property).get(value);
    }

    private Optional<Object> findSetting(List<Object> settings, String name) {
        return settings.stream()
                .filter(setting -> name.equals(((SimpleObject) setting).get("property")))
                .collect(Collectors.toList()).stream()
                .findFirst();
    }
}