package com.expenseshare.demo.entity;


import com.expenseshare.demo.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "groups")
@ToString(exclude = "groups")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    @Column(unique = true)
    private String userName;

    @NotBlank
    @Size(max = 50)
    @Email
    @Column(unique = true)
    private String emailId;

    @Size(max = 120)
    private String password;

    @Column(name = "google_id")
    private String googleId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;


    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;



    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (role == null) {
            role = Role.USER;
        }
    }
    @ManyToOne(targetEntity = Group.class, fetch = FetchType.LAZY)
    @JsonIgnore

    public boolean isAdmin() {
        return Role.ADMIN.equals(this.role);
    }

    public boolean isUser() {
        return Role.USER.equals(this.role);
    }

}
