package com.expenseshare.demo.entity;
import com.expenseshare.demo.enums.SettlementStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "settlements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Settlement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "payer_id", nullable = false)
    private User payer; // User who is paying

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver; // User who is receiving payment

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    private String description;

    @Enumerated(EnumType.STRING)
    private SettlementStatus status;

    @Column(name = "settlement_date", nullable = false)
    private LocalDateTime settlementDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Reference to settlement details for audit trail
    @OneToMany(mappedBy = "settlement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SettlementDetail> settlementDetails;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (settlementDate == null) {
            settlementDate = LocalDateTime.now();
        }
    }
}
