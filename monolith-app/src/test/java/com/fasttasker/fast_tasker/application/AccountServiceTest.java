package com.fasttasker.fast_tasker.application;

import com.fasttasker.common.config.RabbitMQConfig;
import com.fasttasker.common.constant.RabbitMQConstants;
import com.fasttasker.fast_tasker.application.dto.account.AccountResponse;
import com.fasttasker.fast_tasker.application.dto.account.LoginRequest;
import com.fasttasker.fast_tasker.application.dto.account.LoginResponse;
import com.fasttasker.fast_tasker.application.dto.account.RegisterAccountRequest;
import com.fasttasker.fast_tasker.application.dto.notification.NotificationRequest;
import com.fasttasker.fast_tasker.application.mapper.AccountMapper;
import com.fasttasker.fast_tasker.application.service.AccountService;
import com.fasttasker.common.config.JwtService;
import com.fasttasker.fast_tasker.domain.account.*;
import com.fasttasker.fast_tasker.domain.notification.NotificationType;
import com.fasttasker.fast_tasker.domain.tasker.ITaskerRepository;
import com.fasttasker.fast_tasker.domain.tasker.Location;
import com.fasttasker.fast_tasker.domain.tasker.Profile;
import com.fasttasker.fast_tasker.domain.tasker.Tasker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private IAccountRepository accountRepository;
    @Mock
    private ITaskerRepository taskerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AccountMapper accountMapper;
    @Mock
    private JwtService jwtService;
    @Mock
    private RabbitTemplate rabbitTemplate;
    
    @InjectMocks
    private AccountService accountService;

    @Test
    void shouldRegisterAccountSuccess() {
        // 1. GIVEN
        var request = new RegisterAccountRequest(
                "newUser@domain.com",
                "password123456"
        );
        String hashedPassword = "hashedPassword-xyz";
        var responseDto = new AccountResponse(
                UUID.randomUUID().toString(),
                request.email(),
                AccountStatus.PENDING_VERIFICATION
        );

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        ArgumentCaptor<Tasker> taskerCaptor = ArgumentCaptor.forClass(Tasker.class);

        when(accountRepository.existsByEmailValue(request.email())).thenReturn(Boolean.valueOf(false));
        when(passwordEncoder.encode(request.rawPassword())).thenReturn(hashedPassword);
        when(accountMapper.toResponse(any(Account.class))).thenReturn(responseDto);
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArguments()[0]);
        when(taskerRepository.save(any(Tasker.class))).thenAnswer(i -> i.getArguments()[0]);

        // 2. WHEN
        AccountResponse response = accountService.registerAccount(request);

        // 3. THEN
        verify(accountRepository).existsByEmailValue("newUser@domain.com");
        verify(passwordEncoder).encode(request.rawPassword());
        verify(accountRepository).save(accountCaptor.capture());
        Account savedAccount = accountCaptor.getValue();

        assertThat(savedAccount.getEmail().getValue()).isEqualTo(request.email());
        assertThat(savedAccount.getPassword().getValue()).isEqualTo(hashedPassword);
        assertThat(savedAccount.getStatus()).isEqualTo(AccountStatus.PENDING_VERIFICATION);
        assertThat(savedAccount.getId()).isNotNull();

        verify(taskerRepository).save(taskerCaptor.capture());
        Tasker savedTasker = taskerCaptor.getValue();
        assertThat(savedTasker.getAccountId()).isEqualTo(savedAccount.getId());
        assertThat(savedTasker.getProfile()).isNull();

        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE_NAME), eq(RabbitMQConstants.ROUTING_KEY_NOTIFICATION), any(NotificationRequest.class));

        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.email()).isEqualTo("newUser@domain.com");
        assertThat(response.status()).isEqualTo(AccountStatus.PENDING_VERIFICATION);
    }

    @Test
    void shouldLoginSuccess() {
        // 1. GIVE

        // simulating an existing account
        String email = "old-user@domain.com";
        String rawPassword = "password123456";

        String fakeToken= "fake-token.eyJzdWIiOiJ";

        double latitude = -85.542353;
        double longitude = -172.252514;
        String address = "fake address";
        String zip = "04144";

        String firstName = "fakundo";
        String lastName = "Gonzales";
        String about = "fake about";

        var loginRequest = new LoginRequest(email, rawPassword);

        var accountToFind = Account.builder()
                .email(new Email(email))
                .password(new Password(rawPassword))
                .build();

        var  fakeLocation = Location.builder()
                .latitude(latitude)
                .longitude(longitude)
                .address(address)
                .zip(zip)
                .build();

        var fakeProfile = Profile.builder()
                .firstName(firstName)
                .lastName(lastName)
                .photo("")
                .location(fakeLocation)
                .about(about)
                .build();

        var savedTasker = Tasker.builder()
                .accountId(accountToFind.getId())
                .profile(fakeProfile)
                .build();

        // simulating that the email EXISTS
        when(accountRepository.getByEmailValue(loginRequest.email()))
                .thenReturn(accountToFind);

        // simulating the hashing of the password
        when(passwordEncoder.matches(loginRequest.rawPassword(), accountToFind.getPassword().getValue()))
                .thenReturn(Boolean.valueOf(true));

        // simulating the tasker to find
        when(taskerRepository.findByAccountId(accountToFind.getId())).thenReturn(savedTasker);

        // simulating the token generation to return the fake
        when(jwtService.generateToken(accountToFind.getId(), savedTasker.getId(), true)).thenReturn(fakeToken);

        // 2. WHEN
        LoginResponse response = accountService.login(loginRequest);

        // 3. THEN
        // verify that the email was found
        verify(accountRepository).getByEmailValue(email);

        // verify that the password was hashed
        verify(passwordEncoder).matches(rawPassword, accountToFind.getPassword().getValue());

        // verifying that generate token was called with correct ID
        verify(jwtService).generateToken(accountToFind.getId(), savedTasker.getId(), true);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo(fakeToken);
    }
}