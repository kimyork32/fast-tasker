package com.fasttasker.fast_tasker.domain.account;

import com.fasttasker.common.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    private final Email validEmail = new Email("test@example.com");
    private final Password validPassword = new Password("hashedPassword123");
    private final Password newValidPassword = new Password("newHashedPassword456");

    @Test
    void shouldCreateAccount() {
        Account account = new Account(validEmail, validPassword);

        assertThat(account).isNotNull();
        assertThat(account.getId()).isNotNull();
        assertThat(account.getEmail()).isEqualTo(validEmail);
        assertThat(account.getPassword()).isEqualTo(validPassword);
        assertThat(account.getStatus()).isEqualTo(AccountStatus.PENDING_VERIFICATION);
    }

    @Test
    void shouldThrowWhenEmailIsNull() {
        assertThatThrownBy(() -> new Account(null, validPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email cannot be null");
    }

    @Test
    void shouldThrowWhenPasswordIsNull() {
        assertThatThrownBy(() -> new Account(validEmail, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password cannot be null");
    }

    @Test
    void shouldChangePassword() {
        Account account = new Account(validEmail, validPassword);
        account.changePassword(newValidPassword);

        assertThat(account.getPassword()).isEqualTo(newValidPassword);
    }

    @Test
    void shouldThrowWhenNewPasswordIsNull() {
        Account account = new Account(validEmail, validPassword);

        assertThatThrownBy(() -> account.changePassword(null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Password cannot be null");
    }

    @Test
    void shouldBanAccount() {
        Account account = new Account(validEmail, validPassword);
        account.banned();

        assertThat(account.getStatus()).isEqualTo(AccountStatus.BANNED);
    }

    @Test
    void shouldActivateAccount() {
        Account account = new Account(validEmail, validPassword);
        account.activate();

        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void shouldDeactivateAccount() {
        Account account = new Account(validEmail, validPassword);
        account.deactivate();

        assertThat(account.getStatus()).isEqualTo(AccountStatus.INACTIVE);
    }

    @Test
    void shouldSetPendingVerification() {
        Account account = new Account(validEmail, validPassword);
        // First change to another status to ensure pendingVerification changes it
        account.activate();
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);

        account.pendingVerification();

        assertThat(account.getStatus()).isEqualTo(AccountStatus.PENDING_VERIFICATION);
    }
}