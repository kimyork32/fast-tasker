package com.fasttasker.fast_tasker.application.service;

import com.fasttasker.common.config.RabbitMQConfig;
import com.fasttasker.common.constant.RabbitMQConstants;
import com.fasttasker.common.exception.EmailAlreadyExistsException;
import com.fasttasker.fast_tasker.application.dto.account.AccountResponse;
import com.fasttasker.fast_tasker.application.dto.account.LoginRequest;
import com.fasttasker.fast_tasker.application.dto.account.LoginResponse;
import com.fasttasker.fast_tasker.application.dto.notification.NotificationRequest;
import com.fasttasker.fast_tasker.application.dto.account.RegisterAccountRequest;
import com.fasttasker.fast_tasker.application.exception.*;
import com.fasttasker.fast_tasker.application.mapper.AccountMapper;
import com.fasttasker.common.config.JwtService;
import com.fasttasker.fast_tasker.domain.account.*;
import com.fasttasker.fast_tasker.domain.notification.NotificationType;
import com.fasttasker.fast_tasker.domain.task.ITaskRepository;
import com.fasttasker.fast_tasker.domain.task.Task;
import com.fasttasker.fast_tasker.domain.task.TaskStatus;
import com.fasttasker.fast_tasker.domain.tasker.ITaskerRepository;
import com.fasttasker.fast_tasker.domain.tasker.Tasker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class AccountService {

    private final IAccountRepository accountRepository;
    private final ITaskerRepository taskerRepository;
    private final ITaskRepository taskRepository;
    private final AccountMapper accountMapper;
    private final RabbitTemplate rabbitTemplate;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AccountService(
            IAccountRepository accountRepository,
            ITaskerRepository taskerRepository,
            ITaskRepository taskRepository,
            AccountMapper accountMapper, RabbitTemplate rabbitTemplate,
            PasswordEncoder passwordEncoder, JwtService jwtService
    ) {
        this.accountRepository = accountRepository;
        this.taskerRepository = taskerRepository;
        this.taskRepository = taskRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.accountMapper = accountMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AccountResponse registerAccount(RegisterAccountRequest request) {
        if (accountRepository.existsByEmailValue(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        // the password is receiver of the client in plain text. The client will send
        // it using a secure channel such as HTTPS.
        String hashedPassword = passwordEncoder.encode(request.rawPassword());

        var account = Account.builder()
                .email(new Email(request.email()))
                .password(new Password(hashedPassword))
                .build();

        // save account, if any error occurs then rollback
        Account savedAccount = accountRepository.save(account);

        var defaultTasker = Tasker.createWithoutProfile(savedAccount.getId());

        // save tasker, if any error occurs then rollback
        Tasker savedTasker = taskerRepository.save(defaultTasker);

        // notifying of the tasker that your account has been created
        NotificationRequest notificationRequest = new NotificationRequest(
                savedTasker.getId(),
                null,
                NotificationType.SYSTEM
        );

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConstants.ROUTING_KEY_NOTIFICATION,
                    notificationRequest
            );
        } catch (Exception e) {
            log.error("cannot send notification to RabbitMQ: {}", e.getMessage());
        }

        return accountMapper.toResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Account account = accountRepository.getByEmailValue(request.email());

        if (!passwordEncoder.matches(request.rawPassword(), account.getPassword().getValue())) {
            throw new InvalidPasswordException();
        }

        if (account.getStatus() == AccountStatus.BANNED) {
            throw new BannedAccountException();
        }

        // FIXME: currently, this throws an exception if the Tasker does not exist, breaking
        //  the login. Use `Optional` in the future to handle this smoothly
        Tasker tasker = taskerRepository.findByAccountId(account.getId());

        // calculate if the profile is complete
        boolean profileCompleted = tasker.isProfileComplete();

        // return a JWT token
        String token = jwtService.generateToken(account.getId(), tasker.getId(), profileCompleted);
        return new LoginResponse(token);
    }

    @Transactional
    public void changePassword(UUID accountId, String oldPass, String newPass) {
        Account account = findAccountById(accountId);

        if (!passwordEncoder.matches(oldPass, account.getPassword().getValue())) {
            throw new PasswordIncorrectException();
        }

        String newHashedPassword = passwordEncoder.encode(newPass);
        account.changePassword(new Password(newHashedPassword));
        accountRepository.save(account);
    }

    @Transactional
    public void activate(UUID accountId) {
        Account account = findAccountById(accountId);
        account.activate();
        accountRepository.save(account);
    }

    @Transactional
    public void ban(UUID accountId) {
        Account account = findAccountById(accountId);
        account.banned();
        accountRepository.save(account);

        List<Task> userTasks = taskRepository.findByPosterIdAndStatus(accountId, TaskStatus.ACTIVE);
        for (Task task : userTasks) {
            task.cancel();
        }
        taskRepository.saveAll(userTasks);
    }

    @Transactional(readOnly = true)
    public AccountResponse getById(UUID accountId) {
        Account account = findAccountById(accountId);
        return accountMapper.toResponse(account);
    }

    private Account findAccountById(UUID accountId) {
        return accountRepository.findById(accountId);
    }
}