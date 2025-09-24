package org.bahmni.module.bahmnicommons.web.v1_0.search.location;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.bahmnicommons.api.context.AppContext;
import org.openmrs.Location;
import org.openmrs.LocationAttributeType;
import org.openmrs.api.LocationService;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BahmniLocationSearchByAttributeHandler implements SearchHandler {

    private final LocationService locationService;
    private final AppContext appContext;
    private Log log = LogFactory.getLog(BahmniLocationSearchByAttributeHandler.class);
    public static final String PARAM_ATTRIBUTE_NAME = "attrName";
    public static final String PARAM_ATTRIBUTE_VALUE = "attrValue";
    public static final String SEARCH_BY_ATTRIBUTE = "byAttribute";
    public static final String PARAM_INCLUDE_ALL = "includeAll";

    public static final String ERR_UNDEFINED_ATTRIBUTE_NAME = "Undefined attribute %s";
    public static final String INVALID_ATTRIBUTE_VALUE = "Invalid attribute value for %s";


    public static final String INVALID_ATTRIBUTE_TYPE_DEFINITION = "Invalid Attribute type definition for %s";

    @Autowired
    public BahmniLocationSearchByAttributeHandler(LocationService locationService, AppContext appContext) {
        this.locationService = locationService;
        this.appContext = appContext;
    }

    @Override
    public SearchConfig getSearchConfig() {
        SearchQuery searchQuery = new SearchQuery.Builder("Allows you to find locations by attribute")
                .withRequiredParameters(PARAM_ATTRIBUTE_NAME, PARAM_ATTRIBUTE_VALUE)
                .build();
        return new SearchConfig(SEARCH_BY_ATTRIBUTE,
                RestConstants.VERSION_1 + "/location",
                Arrays.asList("2.0.* - 2.*"),
                searchQuery);
    }

    @Override
    public PageableResult search(RequestContext requestContext) throws ResponseException {
        String attributeName = requestContext.getParameter(PARAM_ATTRIBUTE_NAME);
        String attributeValue = requestContext.getParameter(PARAM_ATTRIBUTE_VALUE);
        String includeAllStr = requestContext.getParameter(PARAM_INCLUDE_ALL);

        String query = requestContext.getParameter("q");
        boolean includeRetired = false;
        if (!StringUtils.isEmpty(includeAllStr)) {
            includeRetired = Boolean.getBoolean(includeAllStr);
        }

        Map<LocationAttributeType, Object> attributeTypeObjectMap = getLocationAttributeTypeObjectMap(attributeName, attributeValue);

        List<Location> locations = locationService.getLocations(query, null, attributeTypeObjectMap,
                includeRetired, requestContext.getStartIndex(), requestContext.getLimit());
        return new AlreadyPaged<>(requestContext, locations, false);
    }


    private Map<LocationAttributeType, Object> getLocationAttributeTypeObjectMap(String attributeName, String attributeValue) {
        LocationAttributeType attributeType = findLocationAttributeType(attributeName);
        if (attributeType == null) {
            throw new IllegalArgumentException(String.format(ERR_UNDEFINED_ATTRIBUTE_NAME, attributeName));
        }
        CustomDatatype attrDataType = appContext.getDatatype(attributeType.getDatatypeClassname(), attributeType.getDatatypeConfig());
        if (attrDataType == null) {
            throw new IllegalArgumentException(String.format(INVALID_ATTRIBUTE_TYPE_DEFINITION, attributeName));
        }

        try {
            Object value = attrDataType.fromReferenceString(attributeValue);
            Map<LocationAttributeType, Object> attributeTypeObjectMap = new HashMap<>();
            attributeTypeObjectMap.put(attributeType, value);
            return attributeTypeObjectMap;

        } catch (InvalidCustomValueException e) {
            String errorMessage = String.format(INVALID_ATTRIBUTE_VALUE, attributeName);
            log.error(errorMessage, e);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private LocationAttributeType findLocationAttributeType(String attributeName) {
        List<LocationAttributeType> allLocationAttributeTypes = locationService.getAllLocationAttributeTypes();
        for (LocationAttributeType attributeType : allLocationAttributeTypes) {
            boolean result = attributeType.getName().equalsIgnoreCase(attributeName);
            if (result) {
                return attributeType;
            }
        }
        return null;
    }
}
