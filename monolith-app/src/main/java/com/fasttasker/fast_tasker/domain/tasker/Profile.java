package com.fasttasker.fast_tasker.domain.tasker;

import com.fasttasker.common.exception.DomainException;
import jakarta.persistence.*;
import lombok.*;

/**
 * 
 */
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
@ToString
public class Profile {
    /**
     * minimum one name required and maximum three (words)
     */
    @Column(name = "first_name", length = 60)
    private String firstName;

    /**
     * minimum one name required and maximum three (words)
     */
    @Column(name = "last_name", length = 60)
    private String lastName;

    /**
     * url of the photo
     */
    @Column(name = "photo_url", length = 256)
    private String photo;

    @Embedded
    private Location location;

    /**
     * max 200 characters
     */
    @Column(name = "about", length = 200)
    private String about;

    /**
     * average of the stars given by the clients
     * each star is a value between 0 and 5.
     * one decimal place precision
     */
    @Column(name = "reputation")
    private float reputation;

    /**
     * number of the comments given by the clients
     */
    @Column(name = "client_reviews")
    private int clientReviews;

    @Column(name = "completed_tasks")
    private int completedTasks;


    @Builder(toBuilder = true)
    public Profile(String firstName, String lastName, String photo, Location location, String about) {
        if (firstName == null || lastName == null) {
            throw new DomainException("firstName and lastName cannot be null");
        }
        if (firstName.isEmpty() || lastName.isEmpty()) {
            throw new DomainException("firstName and lastName cannot be empty");
        }
        if (location == null) {
            throw new DomainException("location cannot be null");
        }
        if (about.length() > 200) {
            throw new DomainException("about cannot be longer than 200 characters");
        }
        this.firstName = firstName;
        this.lastName = lastName;
        this.photo = photo;
        this.location = location;
        this.about = about;
        this.reputation = 0.f;
        this.clientReviews = 0;
        this.completedTasks = 0;
    }
}