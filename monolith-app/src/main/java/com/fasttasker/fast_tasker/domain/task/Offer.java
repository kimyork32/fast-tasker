package com.fasttasker.fast_tasker.domain.task;

import com.fasttasker.common.exception.DomainException;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents an offer made by a Tasker for a specific Task.
 */
@Entity
@Table(name = "offer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode
public class Offer {

    @Id
    private UUID id;

    /**
     * budget must be a positive integer
     * greater than 5 and less than 999
     */
    @Column(name = "price", nullable = false)
    private int price;

    /**
     * max size of 500 characters
     */
    @Column(name = "description", length = 500, nullable = false)
    private String description;

    /**
     * Status of the offer (PENDING, ACCEPTED, REJECTED)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OfferStatus status;

    /**
     * ID of the Tasker who made this offer
     */
    @Column(name = "offerted_by_id", nullable = false)
    private UUID offertedById;

    /**
     * Timestamp when the offer was created
     */
    @Column(name = "create_at", nullable = false)
    private Instant createdAt;

    /**
     * The Task this offer belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @ToString.Exclude
    private Task task;

    @Builder(toBuilder = true)
    public Offer(int price, String description, UUID offertedById, Task task) {
        validatePrice(price);
        validateDescription(description);
        if (offertedById == null) {
            throw new DomainException("OffertedById cannot be null");
        }
        if (task == null) {
            throw new DomainException("Task cannot be null");
        }

        this.id = UUID.randomUUID();
        this.price = price;
        this.description = description;
        this.offertedById = offertedById;
        this.task = task;
        this.status = OfferStatus.PENDING;
        this.createdAt = Instant.now();
    }

    // ============================================================================
    // BUSINESS METHODS
    // ============================================================================

    /**
     * Business method to accept this offer.
     * Called by Task.acceptOffer()
     *
     * @throws DomainException if the offer is not in PENDING status
     */
    public void accept() {
        if (this.status != OfferStatus.PENDING) {
            throw new DomainException("Only pending offers can be accepted");
        }
        this.status = OfferStatus.ACCEPTED;
    }

    /**
     * Business method to reject this offer.
     * Called by Task.acceptOffer() for non-selected offers.
     *
     * @throws DomainException if the offer is not in PENDING status
     */
    public void reject() {
        if (this.status != OfferStatus.PENDING) {
            throw new DomainException("Only pending offers can be rejected");
        }
        this.status = OfferStatus.REJECTED;
    }

    /**
     * Gets the ID of the Tasker who made this offer.
     * This is an alias for getOffertedById() to match the method name expected by Task.
     *
     * @return the Tasker's ID
     */
    public UUID getTaskerId() {
        return this.offertedById;
    }

    /**
     * Business method to update the offer details.
     *
     * @param newPrice new price for the offer
     * @param newDescription new description for the offer
     * @throws DomainException if the offer is not in PENDING status
     */
    public void updateOffer(int newPrice, String newDescription) {
        if (this.status != OfferStatus.PENDING) {
            throw new DomainException("Can only update pending offers");
        }

        validatePrice(newPrice);
        validateDescription(newDescription);

        this.price = newPrice;
        this.description = newDescription;
    }

    /**
     * Checks if this offer is pending.
     *
     * @return true if status is PENDING, false otherwise
     */
    public boolean isPending() {
        return this.status == OfferStatus.PENDING;
    }

    /**
     * Checks if this offer is accepted.
     *
     * @return true if status is ACCEPTED, false otherwise
     */
    public boolean isAccepted() {
        return this.status == OfferStatus.ACCEPTED;
    }

    /**
     * Checks if this offer is rejected.
     *
     * @return true if status is REJECTED, false otherwise
     */
    public boolean isRejected() {
        return this.status == OfferStatus.REJECTED;
    }

    // ============================================================================
    // PRIVATE VALIDATION METHODS
    // ============================================================================

    private void validatePrice(int price) {
        if (price < 5 || price > 999) {
            throw new DomainException("Price must be between 5 and 999");
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new DomainException("Offer description cannot be empty");
        }
        if (description.length() > 500) {
            throw new DomainException("Offer description exceeds 500 characters");
        }
    }
}