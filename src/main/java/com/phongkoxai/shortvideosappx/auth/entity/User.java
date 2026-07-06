package com.phongkoxai.shortvideosappx.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_username", columnList = "username"),
                @Index(name = "idx_users_email", columnList = "email")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    String id;

    @Column(nullable = false, length = 50)
    String username;

    @Column(nullable = false, unique = true, length = 100)
    String email;

    @Column(nullable = false)
    String password;

    @Column(length = 50)
    String firstName;

    @Column(length = 50)
    String lastName;

    @Column(length = 100)
    String displayName;

    @Column(nullable = false, length = 100)
    String nickname;

    LocalDate dob;

    @Column(length = 500)
    String bio;

    String avatarUrl;

    @Column(nullable = false)
    Boolean emailVerified;

    @Column(nullable = false)
    Boolean enabled;

    @Column(nullable = false)
    Boolean locked;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    AccountStatus accountStatus;

    @Column(nullable = false)
    Long followerCount;

    @Column(nullable = false)
    Long followingCount;

    @Column(nullable = false)
    Long videoCount;

    @Column(nullable = false, updatable = false)
    Instant createdAt;

    @Column(nullable = false)
    Instant updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            indexes = {
                    @Index(name = "idx_user_roles_user_id", columnList = "user_id"),
                    @Index(name = "idx_user_roles_role_id", columnList = "role_id")
            }
    )
    Set<Role> roles;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if(emailVerified == null) emailVerified = false;
        if (enabled == null) enabled = true;
        if (locked == null) locked = false;
        if (accountStatus == null) accountStatus = AccountStatus.ACTIVE;
        if (nickname == null || nickname.isBlank()) nickname = displayName != null && !displayName.isBlank() ? displayName : username;
        if (followerCount == null) followerCount = 0L;
        if (followingCount == null) followingCount = 0L;
        if (videoCount == null) videoCount = 0L;

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
