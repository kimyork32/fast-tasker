package com.fasttasker.common.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("the email address '" + email + "' is already in use");
    }
}