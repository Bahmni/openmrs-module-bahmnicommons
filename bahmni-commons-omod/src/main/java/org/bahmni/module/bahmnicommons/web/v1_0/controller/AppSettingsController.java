package org.bahmni.module.bahmnicommons.web.v1_0.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicommons.api.service.ModuleAppConfigService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmni/app/setting")
public class AppSettingsController extends BaseRestController {

    private static final Logger log = LogManager.getLogger(AppSettingsController.class);
    ModuleAppConfigService appConfigService;

    @Autowired
    public AppSettingsController(ModuleAppConfigService appConfigService) {
        this.appConfigService = appConfigService;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<Object> getApplicationProperties(@RequestParam(required = false) List<String> module) {
        if ((module == null) || (module.isEmpty())) {
            log.info("No module specified for retrieving properties");
            return Collections.emptyList();
        }
        log.info("Retrieving properties/settings ... " + String.join(", ", module));
        return appConfigService.getAppProperties(module);
    }
}
