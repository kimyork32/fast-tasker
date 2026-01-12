package com.fasttasker.fast_tasker.domain.tasker;


import com.fasttasker.common.exception.DomainException;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * 
 */
@Entity
@Table(name = "tasker")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Tasker {

    @Id
    private UUID id;

    /**
     * ID of the account linked to the tasker
     */
    @Column(name = "account_id", unique = true, nullable = false)
    private UUID accountId;

    /**
     * profile data
     */
    @Embedded
    private Profile profile;

    @Builder(toBuilder = true)
    public Tasker(UUID accountId, Profile profile) {
        if (accountId == null) {
            throw new DomainException("accountId cannot be null");
        }
        this.id = UUID.randomUUID();
        this.accountId = accountId;
        this.profile = profile;
    }

    // obliques language (DDD)
    public static Tasker createWithoutProfile(UUID accountId) {
        return new Tasker(accountId, null);
    }

    public void updateProfile(Profile newProfile) {
        this.profile = newProfile;
    }

    public boolean isProfileComplete() {
        if (this.profile == null) {
            return false;
        }
        return hasText(this.profile.getFirstName()) &&
                hasText(this.profile.getLastName());
    }

    private boolean hasText(String str) {
        return str != null && !str.isBlank();
    }
}