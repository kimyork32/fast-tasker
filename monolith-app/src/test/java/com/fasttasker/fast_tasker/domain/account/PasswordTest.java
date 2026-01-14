package com.fasttasker.fast_tasker.domain.account;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordTest {

    @Test
    void shouldCreatePasswordWithValidHash() {
        String validHash = "hashedPassword123ABC";
        Password password = new Password(validHash);

        assertThat(password).isNotNull();
        assertThat(password.getValue()).isEqualTo(validHash);
    }

    @Test
    void shouldThrowWhenHashIsNull() {
        assertThatThrownBy(() -> new Password(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password hash cannot be empty");
    }

    @Test
    void shouldThrowWhenHashIsBlank() {
        assertThatThrownBy(() -> new Password("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password hash cannot be empty");
    }

    @Test
    void shouldBeEqualForSameHashValue() {
        Password password1 = new Password("sameHashXYZ");
        Password password2 = new Password("sameHashXYZ");

        assertThat(password1).isEqualTo(password2);
        assertThat(password1.hashCode()).isEqualTo(password2.hashCode());
    }

    @Test
    void shouldNotBeEqualForDifferentHashValue() {
        Password password1 = new Password("hashOne");
        Password password2 = new Password("hashTwo");

        assertThat(password1).isNotEqualTo(password2);
        assertThat(password1.hashCode()).isNotEqualTo(password2.hashCode());
    }

    @Test
    void toStringShouldExcludeValue() {
        String hash = "secretHash123";
        Password password = new Password(hash);
        // the @ToString(exclude = "value") annotation means toString() should not reveal the actual hash
        assertThat(password.toString()).doesNotContain(hash);
        assertThat(password.toString()).contains("Password()"); // default lombok toString for no-args
    }
}