package org.bahmni.module.bahmnicommons.web.v1_0.controller.search.advice;

import org.bahmni.module.bahmnicommons.api.search.dto.PatientSearchResponse;
import org.bahmni.module.bahmnicommons.web.v1_0.controller.search.PatientController;
import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.exceptions.SearchException;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.ContextAuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import java.util.Collections;
import java.util.List;

@ControllerAdvice(assignableTypes = PatientController.class)
public class PatientSearchExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(PatientSearchExceptionHandler.class);

    @ExceptionHandler(InvalidSearchCriteriaException.class)
    @ResponseBody
    public ResponseEntity<PatientSearchResponse> handleInvalidSearchCriteria(
            InvalidSearchCriteriaException e, WebRequest webRequest) {
        return errorResponse(currentEntity(webRequest), e.getStatus().getCode(), e.getMessages());
    }

    @ExceptionHandler(ContextAuthenticationException.class)
    @ResponseBody
    public ResponseEntity<PatientSearchResponse> handleAuthenticationRequired(
            ContextAuthenticationException e, WebRequest webRequest) {
        String message = e.getMessage() != null ? e.getMessage() : "Authentication required";
        return errorResponse(currentEntity(webRequest), HttpStatus.UNAUTHORIZED.value(), message);
    }

    @ExceptionHandler(APIAuthenticationException.class)
    @ResponseBody
    public ResponseEntity<PatientSearchResponse> handleAccessDenied(
            APIAuthenticationException e, WebRequest webRequest) {
        String message = e.getMessage() != null ? e.getMessage() : "Access denied";
        return errorResponse(currentEntity(webRequest), HttpStatus.FORBIDDEN.value(), message);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public ResponseEntity<PatientSearchResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException e, WebRequest webRequest) {
        String message = e.getMessage() != null ? e.getMessage() : "Request method not supported";
        return errorResponse(currentEntity(webRequest), HttpStatus.METHOD_NOT_ALLOWED.value(), message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ResponseEntity<PatientSearchResponse> handleMalformedRequestBody(
            HttpMessageNotReadableException e, WebRequest webRequest) {
        Throwable cause = e.getMostSpecificCause();
        String message = cause != null && cause.getMessage() != null
                ? cause.getMessage()
                : "Malformed request body";
        log.error("Malformed patient search request body", e);
        return errorResponse(currentEntity(webRequest), HttpStatus.BAD_REQUEST.value(), message);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResponseEntity<PatientSearchResponse> handleUnexpectedError(
            RuntimeException e, WebRequest webRequest) {
        SearchException searchException =
                new SearchException("Unexpected error during patient search", e);
        log.error(searchException.getMessage(), searchException);
        int statusCode = searchException.getStatus().getCode();
        return errorResponse(currentEntity(webRequest), statusCode,
                "An unexpected error occurred while processing the search request");
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<PatientSearchResponse> handleUnexpectedCheckedError(
            Exception e, WebRequest webRequest) {
        SearchException searchException =
                new SearchException("Unexpected error during patient search", e);
        log.error(searchException.getMessage(), searchException);
        int statusCode = searchException.getStatus().getCode();
        return errorResponse(currentEntity(webRequest), statusCode,
                "An unexpected error occurred while processing the search request");
    }

    private ResponseEntity<PatientSearchResponse> errorResponse(String entity, int status, List<String> messages) {
        return ResponseEntity.status(status).body(PatientSearchResponse.error(entity, status, messages));
    }

    private ResponseEntity<PatientSearchResponse> errorResponse(String entity, int status, String message) {
        return errorResponse(entity, status, Collections.singletonList(message));
    }

    private String currentEntity(WebRequest webRequest) {
        Object entity = webRequest.getAttribute(
                PatientController.CURRENT_ENTITY_ATTRIBUTE, WebRequest.SCOPE_REQUEST);
        return entity != null ? entity.toString() : null;
    }
}
