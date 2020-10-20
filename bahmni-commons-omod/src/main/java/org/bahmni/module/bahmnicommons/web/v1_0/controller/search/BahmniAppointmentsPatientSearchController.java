package org.bahmni.module.bahmnicommons.web.v1_0.controller.search;

import org.bahmni.module.bahmnicommons.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicommons.contract.patient.response.PatientResponse;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for REST web service access to
 * the Search resource.
 * To be used by the Bahmni appointment scheduling module.
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/appointments/search/patient")
public class BahmniAppointmentsPatientSearchController extends BaseRestController {
    
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<AlreadyPaged<PatientResponse>> search(HttpServletRequest request, HttpServletResponse response) throws ResponseException {

        try {
            RequestContext requestContext = RestUtil.getRequestContext(request, response);
            PatientSearchParameters searchParameters = new PatientSearchParameters(requestContext);
            List<Patient> patients = Context.getPatientService().getPatients(searchParameters.getName());
                        
            List<PatientResponse> patientResponseList = patients.stream().map(patient -> {
                PatientResponse patientResponse = new PatientResponse();
                patientResponse.setUuid(patient.getUuid());
                patientResponse.setGivenName(patient.getGivenName());
                patientResponse.setMiddleName(patient.getMiddleName());
                patientResponse.setFamilyName(patient.getFamilyName());
                patientResponse.setPersonId(patient.getPersonId());
                patientResponse.setIdentifier(String.valueOf(patient.getPatientIdentifier()));
                patientResponse.setGender(patient.getGender());
                patientResponse.setBirthDate(patient.getBirthdate());
                patientResponse.setDeathDate(patient.getDeathDate());
                patientResponse.setDateCreated(patient.getDateCreated());
                return patientResponse;
            }).collect(Collectors.toList());
            
            AlreadyPaged alreadyPaged = new AlreadyPaged(requestContext, patientResponseList, false);
            return new ResponseEntity(alreadyPaged,HttpStatus.OK);
        }catch (IllegalArgumentException e){
            return new ResponseEntity(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
