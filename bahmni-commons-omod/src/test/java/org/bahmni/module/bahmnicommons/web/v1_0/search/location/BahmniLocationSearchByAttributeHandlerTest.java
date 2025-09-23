package org.bahmni.module.bahmnicommons.web.v1_0.search.location;

import org.bahmni.module.bahmnicommons.api.context.AppContext;
import org.bahmni.module.bahmnicommons.web.v1_0.search.location.BahmniLocationSearchByAttributeHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Location;
import org.openmrs.LocationAttributeType;
import org.openmrs.api.LocationService;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.datatype.BooleanDatatype;
import org.openmrs.customdatatype.datatype.FreeTextDatatype;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class BahmniLocationSearchByAttributeHandlerTest {

    public static final String CUSTOM_ATTRIBUTE_1 = "custom_attribute_1";
    public static final String CUSTOM_ATTRIBUTE_2 = "custom_attribute_2";
    public static final String BOOL_DATATYPE_CLASSNAME = "org.openmrs.customdatatype.datatype.BooleanDatatype";
    public static String TEXT_DATATYPE_CLASSNAME = "org.openmrs.customdatatype.datatype.FreeTextDatatype";
    @Mock
    LocationService locationService;
    BahmniLocationSearchByAttributeHandler searchHandler;
    @Mock
    RequestContext requestContext;
    @Mock
    AppContext appContext;

    @Before
    public void setUp() {
        LocationAttributeType attributeType1 = new LocationAttributeType();
        attributeType1.setName(CUSTOM_ATTRIBUTE_1);

        attributeType1.setDatatypeClassname(TEXT_DATATYPE_CLASSNAME);

        LocationAttributeType attributeType2 = new LocationAttributeType();
        attributeType2.setName(CUSTOM_ATTRIBUTE_2);
        attributeType1.setDatatypeClassname(BOOL_DATATYPE_CLASSNAME);

        when(locationService.getAllLocationAttributeTypes()).thenReturn(Arrays.asList(attributeType1, attributeType2));
        when(requestContext.getParameter(BahmniLocationSearchByAttributeHandler.PARAM_ATTRIBUTE_NAME)).thenReturn(CUSTOM_ATTRIBUTE_1);
        when(requestContext.getParameter(BahmniLocationSearchByAttributeHandler.PARAM_ATTRIBUTE_VALUE)).thenReturn("abc");
        when(requestContext.getStartIndex()).thenReturn(1);
        when(requestContext.getLimit()).thenReturn(10);

        CustomDatatype freeTextDatatype = new FreeTextDatatype();
        when(appContext.getDatatype(eq(TEXT_DATATYPE_CLASSNAME), any())).thenReturn(freeTextDatatype);

        CustomDatatype boolTextDatatype = new BooleanDatatype();
        when(appContext.getDatatype(eq(BOOL_DATATYPE_CLASSNAME), any())).thenReturn(boolTextDatatype);
        searchHandler = new BahmniLocationSearchByAttributeHandler(locationService, appContext);
    }

    @Test
    public void shouldSearchLocationByAttribute() {
        Location l1  = new Location();
        l1.setName("Location 1");
        when(locationService.getLocations(any(), any(), anyMap(), eq(false), eq(1), eq(10))).thenReturn(Arrays.asList(l1));
        AlreadyPaged result = (AlreadyPaged) searchHandler.search(requestContext);
        verify(locationService).getLocations(any(), any(), anyMap(), eq(false), eq(1), eq(10));
        Assert.assertEquals(1, result.getPageOfResults().size());
    }


}