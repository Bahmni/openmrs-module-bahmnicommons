package org.bahmni.module.bahmnicommons.api.configuration;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CommonAppConfig implements ModuleAppConfig {
    @Override
    public String getModuleName() {
        return "commons";
    }

    @Override
    public List<String> getGlobalAppProperties() {
        return Arrays.asList(
                "default_locale",
                "locale.allowed.list",
                "bahmni.contextCookieExpirationTimeInMinutes",
                "bahmni.enableAuditLog",
                "bahmni.enableEmailPrescriptionOption",
                "bahmni.quickLogoutComboKey",
                "mrs.genders"
        );
    }
}
