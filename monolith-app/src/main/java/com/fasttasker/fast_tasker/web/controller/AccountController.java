package com.fasttasker.fast_tasker.web.controller;

import com.fasttasker.fast_tasker.application.service.AccountService;
import com.fasttasker.fast_tasker.application.dto.account.*;
import com.fasttasker.common.config.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 
 */
@Slf4j
@RestController
@RequestMapping("api/v1/auth")
public class AccountController {

    private final AccountService accountService;
    private final JwtService jwtService;

    /**
     * constructor for dependencies injection
     * @param accountService service
     */
    @Autowired
    public AccountController(AccountService accountService, JwtService jwtService) {
        this.accountService = accountService;
        this.jwtService = jwtService;
    }

    /**
     *
     * @param request the DTO {@link RegisterAccountRequest} that contain email and rawPassword
     * @return an {@link ResponseEntity} that contain the DTO {@link AccountResponse}
     * of the newly created user and  HTTP status code {@code 201 Created}
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterAccountRequest request) {
        AccountResponse accountResponse = accountService.registerAccount(request);

        log.info("accountId: {}", accountResponse.id());
        String newToken = jwtService.generateToken(UUID.fromString(accountResponse.id()), null,false); // profileCompleted = false

        var registerResponse = new RegisterResponse(
                accountResponse.id(),
                accountResponse.email(),
                newToken
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(registerResponse);
    }

    /**
     *
     * @param request the DTO {@link ResponseEntity} that contain email and rawPassword
     * @return an {@link ResponseEntity} that contain the DTO {@link AccountResponse}
     * of the newly created user and  HTTP status code {@code 201 OK}
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request
    ) {
        LoginResponse loginResponse = accountService.login(request);

        return ResponseEntity.ok(loginResponse);
    }
}