package com.shiv.MoneyManager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_profiles")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileEntity {


    /**
     * This class represents a User Profile entity mapped to the "tbl_profiles" table in the database.
     * It uses JPA for ORM and Lombok to reduce boilerplate code.
     */

    @Id // Marks 'id' as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incremented by the database
    private Long id;
    // User's full name
    private String fullName;
    @Column(unique = true) // Enforces unique constraint on email
    private String email;
    // Encrypted password (should be encoded with BCrypt)
    private String password;
    // URL or path to user's profile picture
    private String profileImageUrl;
    @Column(updatable = false) // Prevents this column from being updated once inserted
    @CreationTimestamp // Automatically sets the current timestamp when record is created
    private LocalDateTime createdAt;
    @UpdateTimestamp // Automatically updates this timestamp on every update
    private LocalDateTime updatedAt;
    // Indicates whether the account is active (useful for email verification or blocking users)
    private Boolean isActive;
    // Token used for account activation or password reset links
    private String activationToken;

    /**
     * Lifecycle callback method executed before the entity is persisted (saved for the first time).
     * Sets 'isActive' to false if it hasn't been explicitly set.
     */
    @PrePersist
    public  void prePersist(){
        if(this.isActive == null){
            isActive = false;
        }
    }
}
