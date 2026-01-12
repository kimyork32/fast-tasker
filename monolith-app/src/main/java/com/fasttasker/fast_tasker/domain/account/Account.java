package com.fasttasker.fast_tasker.domain.account;

import com.fasttasker.common.exception.DomainException;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * represents the root of the Aggregate Root for user's account (Tasker).
 * This entity manages the central identity, credentials and account status.
 */
@Entity
@Table(name = "account")
@NoArgsConstructor (access = AccessLevel.PROTECTED)
@Getter
@ToString
public class Account {

    /**
     * unique ID generates for the application. Primary Key (PK)
     */
    @Id
    private UUID id;

    /**
     * Email for account, mapped as an  immutable VO.
     * The column email in the BD must be unique.
     */
    @Embedded
    private Email email;

    @Embedded
    private Password password;

    /**
     * The account state of the account.
     * its stored as a string in the BD greater readability.
     */
    @Enumerated(EnumType.STRING)
    @Column(name ="status", nullable = false)
    private AccountStatus status;

    @Builder(toBuilder = true)
    public Account(Email email, Password password) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        this.id = UUID.randomUUID();
        this.email = email;
        this.password = password;
        this.status = AccountStatus.PENDING_VERIFICATION;
    }

    public void changePassword(Password newPassword) {
        if (newPassword == null) {
            throw new DomainException("Password cannot be null");
        }
        this.password = newPassword;
    }

    public void banned() {
        this.status = AccountStatus.BANNED;
    }

    public void activate() {
        this.status = AccountStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = AccountStatus.INACTIVE;
    }

    public void pendingVerification() {
        this.status = AccountStatus.PENDING_VERIFICATION;
    }
}