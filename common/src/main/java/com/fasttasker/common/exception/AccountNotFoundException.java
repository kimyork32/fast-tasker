package com.fasttasker.common.exception;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) {
        super(message);
    }
    public AccountNotFoundException(UUID id) {
        super("Account not found with ID: " + id);
    }
    public static  AccountNotFoundException fromEmailValue(String emailValue) {
        return new AccountNotFoundException("Account not found for email value: " + emailValue);
    }
}