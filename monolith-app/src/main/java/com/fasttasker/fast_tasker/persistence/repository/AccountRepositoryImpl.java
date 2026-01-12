package com.fasttasker.fast_tasker.persistence.repository;

import com.fasttasker.common.exception.AccountNotFoundException;
import com.fasttasker.fast_tasker.domain.account.Account;
import com.fasttasker.fast_tasker.domain.account.IAccountRepository;
import com.fasttasker.fast_tasker.persistence.jpa.JpaAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryImpl implements IAccountRepository {

    private final JpaAccountRepository jpaAccountRepository;

    @Override
    public Account save(Account account) {
        return jpaAccountRepository.save(account);
    }

    @Override
    public Account findById(UUID id) {
        return jpaAccountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Override
    public boolean existsByEmailValue(String emailValue) {
        return jpaAccountRepository.existsByEmailValue(emailValue);
    }

    @Override
    public Account getByEmailValue(String emailValue) {
        return jpaAccountRepository.findByEmailValue(emailValue)
                .orElseThrow(() -> new AccountNotFoundException(emailValue));
    }
}