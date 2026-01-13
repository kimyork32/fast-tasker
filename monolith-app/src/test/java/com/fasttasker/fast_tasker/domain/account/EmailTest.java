package com.fasttasker.fast_tasker.domain.account;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @Test
    void shouldCreateEmailWithValidFormat() {
        String validEmailValue = "test@example.com";
        Email email = new Email(validEmailValue);

        assertThat(email).isNotNull();
        assertThat(email.getValue()).isEqualTo(validEmailValue);
    }

    @Test
    void shouldThrowWhenEmailIsNull() {
        assertThatThrownBy(() -> new Email(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email cannot be null");
    }

    @Test
    void shouldThrowWhenEmailFormatIsInvalid() {
        String invalidEmailValue = "invalid-email";
        assertThatThrownBy(() -> new Email(invalidEmailValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email format provided to Domain");
    }

    @Test
    void shouldThrowWhenEmailFormatIsInvalidWithoutTLD() {
        String invalidEmailValue = "user@domain";
        assertThatThrownBy(() -> new Email(invalidEmailValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email format provided to Domain");
    }

    @Test
    void shouldBeEqualForSameEmailValue() {
        Email email1 = new Email("same@example.com");
        Email email2 = new Email("same@example.com");

        assertThat(email1).isEqualTo(email2);
        assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
    }

    @Test
    void shouldNotBeEqualForDifferentEmailValue() {
        Email email1 = new Email("one@example.com");
        Email email2 = new Email("two@example.com");

        assertThat(email1).isNotEqualTo(email2);
        assertThat(email1.hashCode()).isNotEqualTo(email2.hashCode());
    }

    @Test
    void toStringShouldReturnEmailValue() {
        String emailValue = "tostring@example.com";
        Email email = new Email(emailValue);
        assertThat(email.getValue()).isEqualTo(emailValue);
    }
}