package com.fasttasker.fast_tasker.domain.task;

import com.fasttasker.common.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OfferTest {

    private Task mockTask;
    private UUID validOffertedById;
    private String validDescription;
    private int validPrice;

    @BeforeEach
    void setUp() {
        mockTask = Mockito.mock(Task.class);
        validOffertedById = UUID.randomUUID();
        validDescription = "I can complete this task efficiently.";
        validPrice = 150;
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        void shouldCreateOfferSuccessfully() {
            Offer offer = new Offer(validPrice, validDescription, validOffertedById, mockTask);

            assertThat(offer.getId()).isNotNull();
            assertThat(offer.getPrice()).isEqualTo(validPrice);
            assertThat(offer.getDescription()).isEqualTo(validDescription);
            assertThat(offer.getOffertedById()).isEqualTo(validOffertedById);
            assertThat(offer.getTask()).isEqualTo(mockTask);
            assertThat(offer.getStatus()).isEqualTo(OfferStatus.PENDING);
            assertThat(offer.getCreatedAt()).isNotNull();
        }

        @Test
        void shouldThrowWhenPriceIsTooLow() {
            assertThatThrownBy(() -> new Offer(4, validDescription, validOffertedById, mockTask))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Price must be between 5 and 999");
        }

        @Test
        void shouldThrowWhenDescriptionIsEmpty() {
            assertThatThrownBy(() -> new Offer(validPrice, " ", validOffertedById, mockTask))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Offer description cannot be empty");
        }

        @Test
        void shouldThrowWhenDescriptionIsTooLong() {
            String longDescription = "a".repeat(501);
            assertThatThrownBy(() -> new Offer(validPrice, longDescription, validOffertedById, mockTask))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Offer description exceeds 500 characters");
        }

        @Test
        void shouldThrowWhenOffertedByIdIsNull() {
            assertThatThrownBy(() -> new Offer(validPrice, validDescription, null, mockTask))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("OffertedById cannot be null");
        }

        @Test
        void shouldThrowWhenTaskIsNull() {
            assertThatThrownBy(() -> new Offer(validPrice, validDescription, validOffertedById, null))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Task cannot be null");
        }
    }

    @Nested
    @DisplayName("Business Method Tests")
    class BusinessMethodTests {

        private Offer offer;

        @BeforeEach
        void init() {
            offer = new Offer(validPrice, validDescription, validOffertedById, mockTask);
        }

        @Test
        void shouldAcceptPendingOffer() {
            offer.accept();
            assertThat(offer.getStatus()).isEqualTo(OfferStatus.ACCEPTED);
            assertThat(offer.isAccepted()).isTrue();
        }

        @Test
        void shouldThrowWhenAcceptingNonPendingOffer() {
            offer.accept(); // Status is now ACCEPTED
            assertThatThrownBy(offer::accept)
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Only pending offers can be accepted");
        }

        @Test
        void shouldRejectPendingOffer() {
            offer.reject();
            assertThat(offer.getStatus()).isEqualTo(OfferStatus.REJECTED);
            assertThat(offer.isRejected()).isTrue();
        }

        @Test
        void shouldThrowWhenRejectingNonPendingOffer() {
            offer.reject(); // Status is now REJECTED
            assertThatThrownBy(offer::reject)
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Only pending offers can be rejected");
        }

        @Test
        void shouldUpdatePendingOffer() {
            int newPrice = 200;
            String newDescription = "Updated offer with a better price.";

            offer.updateOffer(newPrice, newDescription);

            assertThat(offer.getPrice()).isEqualTo(newPrice);
            assertThat(offer.getDescription()).isEqualTo(newDescription);
        }

        @Test
        void shouldThrowWhenUpdatingNonPendingOffer() {
            offer.accept(); // Status is now ACCEPTED
            assertThatThrownBy(() -> offer.updateOffer(250, "new desc"))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Can only update pending offers");
        }

        @Test
        void getTaskerIdShouldReturnOffertedById() {
            assertThat(offer.getTaskerId()).isEqualTo(validOffertedById);
        }
    }

    @Nested
    @DisplayName("Status Check Tests")
    class StatusCheckTests {
        @Test
        void isPendingShouldBeTrueForNewOffer() {
            Offer offer = new Offer(validPrice, validDescription, validOffertedById, mockTask);
            assertThat(offer.isPending()).isTrue();
            assertThat(offer.isAccepted()).isFalse();
            assertThat(offer.isRejected()).isFalse();
        }
    }
}