package com.expenseshare.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_balances",
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "debtor_id", "creditor_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne
    @JoinColumn(name = "debtor_id", nullable = false)
    private User debtor; // User who owes money

    @ManyToOne
    @JoinColumn(name = "creditor_id", nullable = false)
    private User creditor; // User who is owed money

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount; // Amount owed by debtor to creditor

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Version
    private Long version; // For optimistic locking

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        lastUpdated = LocalDateTime.now();
    }


}
