package com.fasttasker.fast_tasker.domain.conversation;

import com.fasttasker.common.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessageContentTest {

    @Nested
    @DisplayName("Constructor and Validation Tests")
    class ConstructorTests {

        @Test
        void shouldCreateMessageContentWithTextSuccessfully() {
            MessageContent content = new MessageContent("Hello", null);
            assertThat(content).isNotNull();
            assertThat(content.getText()).isEqualTo("Hello");
            assertThat(content.getAttachmentUrl()).isNull();
        }

        @Test
        void shouldCreateMessageContentWithAttachmentUrlSuccessfully() {
            MessageContent content = new MessageContent(null, "http://example.com/image.jpg");
            assertThat(content).isNotNull();
            assertThat(content.getText()).isNull();
            assertThat(content.getAttachmentUrl()).isEqualTo("http://example.com/image.jpg");
        }

        @Test
        void shouldCreateMessageContentWithBothTextAndAttachmentUrlSuccessfully() {
            MessageContent content = new MessageContent("Check this out", "http://example.com/video.mp4");
            assertThat(content).isNotNull();
            assertThat(content.getText()).isEqualTo("Check this out");
            assertThat(content.getAttachmentUrl()).isEqualTo("http://example.com/video.mp4");
        }

        @Test
        void shouldThrowWhenBothTextAndAttachmentUrlAreNull() {
            assertThatThrownBy(() -> new MessageContent(null, null))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Message content must have either text or an attachment URL.");
        }

        @Test
        void shouldThrowWhenBothTextAndAttachmentUrlAreEmpty() {
            assertThatThrownBy(() -> new MessageContent(" ", " "))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Message content must have either text or an attachment URL.");
        }
    }

    @Nested
    @DisplayName("Snippet Method Tests")
    class SnippetMethodTests {

        @Test
        void snippetShouldReturnTextWhenTextIsPresent() {
            MessageContent content = new MessageContent("Hello World", null);
            assertThat(content.snippet()).isEqualTo("Hello World");
        }

        @Test
        void snippetShouldReturnFotoWhenOnlyAttachmentUrlIsPresent() {
            MessageContent content = new MessageContent(null, "http://example.com/image.png");
            assertThat(content.snippet()).isEqualTo("Foto");
        }

        @Test
        void snippetShouldReturnTextWhenBothArePresent() {
            MessageContent content = new MessageContent("My photo", "http://example.com/image.png");
            assertThat(content.snippet()).isEqualTo("My photo");
        }

    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        void shouldBeEqualForSameTextAndAttachmentUrl() {
            MessageContent content1 = new MessageContent("Test", "url1");
            MessageContent content2 = new MessageContent("Test", "url1");
            assertThat(content1).isEqualTo(content2);
            assertThat(content1.hashCode()).hasSameHashCodeAs(content2.hashCode());
        }

        @Test
        void shouldNotBeEqualForDifferentText() {
            MessageContent content1 = new MessageContent("Test1", "url1");
            MessageContent content2 = new MessageContent("Test2", "url1");
            assertThat(content1).isNotEqualTo(content2);
        }

        @Test
        void shouldNotBeEqualForDifferentAttachmentUrl() {
            MessageContent content1 = new MessageContent("Test", "url1");
            MessageContent content2 = new MessageContent("Test", "url2");
            assertThat(content1).isNotEqualTo(content2);
        }

        @Test
        void shouldNotBeEqualForDifferentTextAndAttachmentUrl() {
            MessageContent content1 = new MessageContent("Test1", "url1");
            MessageContent content2 = new MessageContent("Test2", "url2");
            assertThat(content1).isNotEqualTo(content2);
        }
    }
}