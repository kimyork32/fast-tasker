package com.fasttasker.fast_tasker.domain.tasker;

import com.fasttasker.common.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskerTest {

    private final UUID validAccountId = UUID.randomUUID();
    private final Profile validProfile = Profile.builder()
            .firstName("Test")
            .lastName("User")
            .location(Location.builder().latitude(0).longitude(0).zip("000").build())
            .about("Test")
            .build();

    @Test
    void shouldCreateTasker() {
        Tasker tasker = new Tasker(validAccountId, validProfile);

        assertThat(tasker).isNotNull();
        assertThat(tasker.getId()).isNotNull();
        assertThat(tasker.getAccountId()).isEqualTo(validAccountId);
        assertThat(tasker.getProfile()).isEqualTo(validProfile);
    }

    @Test
    void shouldThrowWhenAccountIdNull() {
        assertThatThrownBy(() -> new Tasker(null, validProfile))
                .isInstanceOf(DomainException.class)
                .hasMessage("accountId cannot be null");
    }

    @Test
    void shouldCreateWithoutProfile() {
        Tasker tasker = Tasker.createWithoutProfile(validAccountId);

        assertThat(tasker).isNotNull();
        assertThat(tasker.getId()).isNotNull();
        assertThat(tasker.getAccountId()).isEqualTo(validAccountId);
        assertThat(tasker.getProfile()).isNull();
    }

    @Test
    void shouldUpdateProfile() {
        Tasker tasker = Tasker.createWithoutProfile(validAccountId);

        tasker.updateProfile(validProfile);

        assertThat(tasker.getProfile()).isNotNull();
        assertThat(tasker.getProfile().getFirstName()).isEqualTo("Test");
    }
}