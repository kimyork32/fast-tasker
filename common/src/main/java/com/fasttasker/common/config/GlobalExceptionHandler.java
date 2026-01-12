package com.fasttasker.common.config;

import com.fasttasker.common.exception.AccountNotFoundException;
import com.fasttasker.common.exception.DomainException;
import com.fasttasker.common.exception.EmailAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.LocalDateTime;

// learning about HTTP Status Codes: https://datatracker.ietf.org/doc/html/rfc2616
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String TITLE_CONFLICT = "Conflict";
    private static final String TITLE_BAD_REQUEST = "Bad Request";
    private static final String TITLE_NOT_FOUND = "Not Found";
    private static final String TITLE_INTERNAL_ERROR = "Internal Server Error";
    private static final String TYPE_EMAIL_EXISTS = "urn:problem:email-exists";
    private static final String TYPE_INVALID_DATA = "urn:problem:invalid-data";
    private static final String MSG_UNEXPECTED_ERROR = "An unexpected error occurred";

    // Conflict Handling -> 409 conflict
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleEmailExists(EmailAlreadyExistsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle(TITLE_CONFLICT);
        problem.setType(URI.create(TYPE_EMAIL_EXISTS));
        problem.setProperty("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    // Domain Validation Handling -> 400 bad request
    @ExceptionHandler({IllegalArgumentException.class, DomainException.class})
    public ResponseEntity<ProblemDetail> handleDomainValidation(RuntimeException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle(TITLE_BAD_REQUEST);
        problem.setType(URI.create(TYPE_INVALID_DATA));
        problem.setProperty("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    // Handling Not Found -> 404 not found
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(AccountNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle(TITLE_NOT_FOUND);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    // Fallback for any other unhandled error -> 400 internal server error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(Exception ex) {
        // log the critical error
        log.error("Unexpected error occurred in the application", ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, MSG_UNEXPECTED_ERROR);
        problem.setTitle(TITLE_INTERNAL_ERROR);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}