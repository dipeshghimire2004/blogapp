# 🎁 Donation Model Review - "Buy Me a Coffee" Style

## Overview
You're building a donation system where users can support content creators (blog post authors) with monetary donations - similar to "Buy Me a Coffee" or Ko-fi.

---

## Current Database Model Analysis

### ✅ What's Good

1. **Proper Relationships**
   - Sender (donor) → User
   - Receiver (content creator) → User
   - BlogPost (optional) → Specific post being supported
   
2. **UUID Primary Key** - Good for distributed systems and security

3. **Soft Delete** - Using `@SQLRestriction("deleted=false")` from BaseEntity

4. **Auditing** - Inherits `updatedAt` from BaseEntity

5. **BigDecimal for Money** - Correct choice for financial calculations

6. **Commission Field** - Smart! You can take a platform fee

---

## ❌ Critical Issues Found

### Issue 1: Wrong Table Name
```java
@Table(name = "blog_posts")  // ❌ WRONG! This conflicts with BlogPost entity
```

**Problem:** You're using the same table name as `BlogPost` entity. This will cause conflicts.

**Fix:**
```java
@Table(name = "donations")  // ✅ Correct
```

---

### Issue 2: Missing @Column Annotations
```java
private BigDecimal amount;           // ❌ No constraints
private BigDecimal commission;       // ❌ No constraints
private DonationStatus status;       // ❌ Not specified as ENUM
private String donation_number;      // ❌ No uniqueness constraint
```

**Problems:**
- No NOT NULL constraints
- No precision/scale for BigDecimal in database
- Status not marked as ENUM type
- donation_number should be unique but isn't enforced

---

### Issue 3: Missing Validation
```java
@Digits(integer = 10, fraction = 2)  // ✅ Good for validation
private BigDecimal amount;

// ❌ But missing:
// - @NotNull
// - @Positive (amount should be > 0)
// - @Min/@Max constraints
```

---

### Issue 4: Snake Case in Java
```java
private String donation_number;  // ❌ Java convention is camelCase
```

Should be:
```java
private String donationNumber;  // ✅ Java convention
@Column(name = "donation_number")  // ✅ Database column name
```

---

### Issue 5: Missing Important Fields

For a "Buy Me a Coffee" style donation system, you're missing:

1. **Payment Gateway Fields**
   - `paymentMethod` (Khalti, eSewa, card, etc.)
   - `transactionId` (from payment gateway)
   - `paymentGatewayResponse` (raw response for debugging)

2. **Timestamps**
   - `createdAt` (when donation was initiated)
   - `completedAt` (when payment succeeded)
   - `failedAt` (when payment failed)

3. **User Experience Fields**
   - `donorName` (if anonymous donation, store display name)
   - `message` (donor's message to creator)
   - `isAnonymous` (hide donor identity)
   - `isPublic` (show on public donation list)

4. **Financial Fields**
   - `currency` (NPR, USD, etc.)
   - `netAmount` (amount after commission)
   - `refundAmount` (if refunded)
   - `refundReason`

5. **Metadata**
   - `ipAddress` (for fraud detection)
   - `userAgent` (device info)
   - `failureReason` (why payment failed)

---

### Issue 6: FetchType.EAGER Everywhere
```java
@ManyToOne(fetch = FetchType.EAGER)  // ❌ Can cause N+1 queries
```

**Problem:** EAGER loading loads all related entities immediately, even when not needed.

**Better Approach:**
- Use `LAZY` by default
- Use `@EntityGraph` or JOIN FETCH when you need related data

---

### Issue 7: Missing Indexes

For a donation system, you'll frequently query by:
- `receiver_id` (show donations received by a creator)
- `sender_id` (show donations made by a user)
- `blog_post_id` (donations for specific post)
- `status` (pending, completed, failed)
- `createdAt` (recent donations)

These need database indexes for performance.

---

## 🎯 Recommended Complete Model

```java
package org.blogapp.dg_blogapp.payment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.blogapp.dg_blogapp.model.BaseEntity;
import org.blogapp.dg_blogapp.model.BlogPost;
import org.blogapp.dg_blogapp.model.User;
import org.blogapp.dg_blogapp.payment.enums.DonationStatus;
import org.blogapp.dg_blogapp.payment.enums.PaymentMethod;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "donations", indexes = {
    @Index(name = "idx_receiver_id", columnList = "receiver_id"),
    @Index(name = "idx_sender_id", columnList = "sender_id"),
    @Index(name = "idx_blog_post_id", columnList = "blog_post_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_donation_number", columnList = "donation_number")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted=false")
public class Donation extends BaseEntity {

    // ==========================================
    // RELATIONSHIPS
    // ==========================================
    
    /**
     * The user who is making the donation (donor)
     * Can be null for guest donations
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    /**
     * The content creator receiving the donation
     * Required - every donation must have a receiver
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    @NotNull(message = "Receiver is required")
    private User receiver;

    /**
     * Optional: The specific blog post being supported
     * Null if it's a general profile donation
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_post_id")
    private BlogPost blogPost;

    // ==========================================
    // FINANCIAL FIELDS
    // ==========================================
    
    /**
     * Donation amount in the smallest currency unit (paisa for NPR)
     * Example: 100.50 NPR = 10050 paisa
     */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Digits(integer = 10, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    /**
     * Platform commission/fee
     */
    @Column(name = "commission", precision = 10, scale = 2)
    @PositiveOrZero(message = "Commission cannot be negative")
    @Digits(integer = 10, fraction = 2)
    @Builder.Default
    private BigDecimal commission = BigDecimal.ZERO;

    /**
     * Net amount received by creator (amount - commission)
     */
    @Column(name = "net_amount", precision = 10, scale = 2)
    @Digits(integer = 10, fraction = 2)
    private BigDecimal netAmount;

    /**
     * Currency code (NPR, USD, etc.)
     */
    @Column(name = "currency", length = 3, nullable = false)
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    @Builder.Default
    private String currency = "NPR";

    // ==========================================
    // PAYMENT STATUS & TRACKING
    // ==========================================
    
    /**
     * Current status of the donation
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Status is required")
    @Builder.Default
    private DonationStatus status = DonationStatus.INITIATED;

    /**
     * Unique donation reference number
     * Format: DON-YYYYMMDD-XXXXX
     */
    @Column(name = "donation_number", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Donation number is required")
    private String donationNumber;

    /**
     * Payment method used (Khalti, eSewa, Card, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PaymentMethod paymentMethod;

    /**
     * Transaction ID from payment gateway
     */
    @Column(name = "transaction_id", unique = true, length = 100)
    private String transactionId;

    /**
     * Raw response from payment gateway (for debugging)
     */
    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;

    // ==========================================
    // USER EXPERIENCE FIELDS
    // ==========================================
    
    /**
     * Donor's display name (for anonymous donations)
     */
    @Column(name = "donor_name", length = 100)
    @Size(max = 100, message = "Donor name too long")
    private String donorName;

    /**
     * Optional message from donor to creator
     */
    @Column(name = "message", columnDefinition = "TEXT")
    @Size(max = 500, message = "Message too long")
    private String message;

    /**
     * Hide donor identity from public
     */
    @Column(name = "is_anonymous", nullable = false)
    @Builder.Default
    private boolean isAnonymous = false;

    /**
     * Show this donation on public donation list
     */
    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private boolean isPublic = true;

    // ==========================================
    // TIMESTAMPS
    // ==========================================
    
    /**
     * When donation was initiated
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When payment was successfully completed
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * When payment failed
     */
    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    // ==========================================
    // REFUND FIELDS
    // ==========================================
    
    /**
     * Refund amount (if refunded)
     */
    @Column(name = "refund_amount", precision = 10, scale = 2)
    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    private BigDecimal refundAmount;

    /**
     * Reason for refund
     */
    @Column(name = "refund_reason", length = 255)
    @Size(max = 255)
    private String refundReason;

    /**
     * When refund was processed
     */
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    // ==========================================
    // METADATA (for fraud detection & analytics)
    // ==========================================
    
    /**
     * IP address of donor
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User agent (browser/device info)
     */
    @Column(name = "user_agent", length = 255)
    private String userAgent;

    /**
     * Reason for payment failure
     */
    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    /**
     * Soft delete flag (inherited from BaseEntity)
     */
    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private boolean deleted = false;

    // ==========================================
    // HELPER METHODS
    // ==========================================
    
    /**
     * Calculate net amount after commission
     */
    @PrePersist
    @PreUpdate
    public void calculateNetAmount() {
        if (amount != null && commission != null) {
            this.netAmount = amount.subtract(commission);
        }
    }

    /**
     * Mark donation as completed
     */
    public void markAsCompleted() {
        this.status = DonationStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Mark donation as failed
     */
    public void markAsFailed(String reason) {
        this.status = DonationStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.failureReason = reason;
    }

    /**
     * Check if donation can be refunded
     */
    public boolean canBeRefunded() {
        return status == DonationStatus.COMPLETED 
            && refundAmount == null
            && createdAt.isAfter(LocalDateTime.now().minusDays(30)); // 30-day refund window
    }
}
```

---

## 📊 Supporting Enums

### PaymentMethod Enum
```java
package org.blogapp.dg_blogapp.payment.enums;

public enum PaymentMethod {
    KHALTI("Khalti"),
    ESEWA("eSewa"),
    CONNECT_IPS("ConnectIPS"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    BANK_TRANSFER("Bank Transfer"),
    MOBILE_BANKING("Mobile Banking");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
```

### Updated DonationStatus Enum
```java
package org.blogapp.dg_blogapp.payment.enums;

public enum DonationStatus {
    INITIATED,      // Donation created, payment not started
    PENDING,        // Payment in progress
    COMPLETED,      // Payment successful
    FAILED,         // Payment failed
    REFUNDED,       // Donation refunded
    CANCELLED       // Donation cancelled by user
}
```

---

## 🗄️ Database Schema (Generated)

```sql
CREATE TABLE donations (
    -- Primary Key
    id UUID PRIMARY KEY,
    
    -- Relationships
    sender_id BIGINT REFERENCES users(id),
    receiver_id BIGINT NOT NULL REFERENCES users(id),
    blog_post_id UUID REFERENCES blog_posts(id),
    
    -- Financial
    amount DECIMAL(10,2) NOT NULL,
    commission DECIMAL(10,2) DEFAULT 0,
    net_amount DECIMAL(10,2),
    currency VARCHAR(3) NOT NULL DEFAULT 'NPR',
    
    -- Payment Tracking
    status VARCHAR(20) NOT NULL DEFAULT 'INITIATED',
    donation_number VARCHAR(50) UNIQUE NOT NULL,
    payment_method VARCHAR(30),
    transaction_id VARCHAR(100) UNIQUE,
    gateway_response TEXT,
    
    -- User Experience
    donor_name VARCHAR(100),
    message TEXT,
    is_anonymous BOOLEAN NOT NULL DEFAULT false,
    is_public BOOLEAN NOT NULL DEFAULT true,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    failed_at TIMESTAMP,
    
    -- Refunds
    refund_amount DECIMAL(10,2),
    refund_reason VARCHAR(255),
    refunded_at TIMESTAMP,
    
    -- Metadata
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    failure_reason VARCHAR(255),
    
    -- Soft Delete
    deleted BOOLEAN NOT NULL DEFAULT false,
    is_active BOOLEAN NOT NULL DEFAULT true,
    
    -- Indexes
    INDEX idx_receiver_id (receiver_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_blog_post_id (blog_post_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_donation_number (donation_number)
);
```

---

## 🔍 Common Queries You'll Need

### 1. Get All Donations for a Creator
```java
@Query("SELECT d FROM Donation d WHERE d.receiver.id = :userId AND d.status = 'COMPLETED' ORDER BY d.createdAt DESC")
List<Donation> findCompletedDonationsByReceiver(@Param("userId") Long userId);
```

### 2. Get Total Earnings for a Creator
```java
@Query("SELECT SUM(d.netAmount) FROM Donation d WHERE d.receiver.id = :userId AND d.status = 'COMPLETED'")
BigDecimal getTotalEarnings(@Param("userId") Long userId);
```

### 3. Get Recent Public Donations (for leaderboard)
```java
@Query("SELECT d FROM Donation d WHERE d.isPublic = true AND d.status = 'COMPLETED' ORDER BY d.createdAt DESC")
Page<Donation> findRecentPublicDonations(Pageable pageable);
```

### 4. Get Donations by Blog Post
```java
@Query("SELECT d FROM Donation d WHERE d.blogPost.id = :postId AND d.status = 'COMPLETED'")
List<Donation> findDonationsByBlogPost(@Param("postId") UUID postId);
```

### 5. Get Pending Donations (for cleanup job)
```java
@Query("SELECT d FROM Donation d WHERE d.status = 'PENDING' AND d.createdAt < :cutoffTime")
List<Donation> findStalePendingDonations(@Param("cutoffTime") LocalDateTime cutoffTime);
```

---

## 📝 Updated Repository

```java
package org.blogapp.dg_blogapp.payment.repository;

import org.blogapp.dg_blogapp.payment.enums.DonationStatus;
import org.blogapp.dg_blogapp.payment.model.Donation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonationRepository extends JpaRepository<Donation, UUID> {
    
    // Find by donation number
    Optional<Donation> findByDonationNumber(String donationNumber);
    
    // Find by transaction ID
    Optional<Donation> findByTransactionId(String transactionId);
    
    // Get donations received by a user
    List<Donation> findByReceiverIdAndStatus(Long receiverId, DonationStatus status);
    
    // Get donations made by a user
    List<Donation> findBySenderIdAndStatus(Long senderId, DonationStatus status);
    
    // Get donations for a specific blog post
    List<Donation> findByBlogPostIdAndStatus(UUID blogPostId, DonationStatus status);
    
    // Get recent public donations
    Page<Donation> findByIsPublicTrueAndStatusOrderByCreatedAtDesc(
        DonationStatus status, Pageable pageable);
    
    // Calculate total earnings
    @Query("SELECT COALESCE(SUM(d.netAmount), 0) FROM Donation d " +
           "WHERE d.receiver.id = :userId AND d.status = 'COMPLETED'")
    BigDecimal calculateTotalEarnings(@Param("userId") Long userId);
    
    // Count donations received
    @Query("SELECT COUNT(d) FROM Donation d " +
           "WHERE d.receiver.id = :userId AND d.status = 'COMPLETED'")
    Long countDonationsReceived(@Param("userId") Long userId);
    
    // Find stale pending donations (for cleanup)
    @Query("SELECT d FROM Donation d " +
           "WHERE d.status = 'PENDING' AND d.createdAt < :cutoffTime")
    List<Donation> findStalePendingDonations(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Get top donors for a creator
    @Query("SELECT d.sender, SUM(d.amount) as total " +
           "FROM Donation d " +
           "WHERE d.receiver.id = :userId AND d.status = 'COMPLETED' " +
           "GROUP BY d.sender " +
           "ORDER BY total DESC")
    List<Object[]> findTopDonors(@Param("userId") Long userId, Pageable pageable);
}
```

---

## 🎯 Key Improvements Summary

1. ✅ **Fixed table name** - Changed from `blog_posts` to `donations`
2. ✅ **Added proper constraints** - NOT NULL, UNIQUE, precision/scale
3. ✅ **Added validation** - @NotNull, @Positive, @Size, etc.
4. ✅ **Fixed naming** - camelCase in Java, snake_case in DB
5. ✅ **Added missing fields** - payment tracking, UX fields, timestamps
6. ✅ **Added indexes** - For common queries
7. ✅ **Changed to LAZY loading** - Better performance
8. ✅ **Added helper methods** - Business logic in entity
9. ✅ **Added comprehensive queries** - Common use cases covered
10. ✅ **Added PaymentMethod enum** - Type safety

---

## 🚀 Next Steps

1. **Update the entity** with the recommended model
2. **Create migration script** if you have existing data
3. **Update repository** with custom queries
4. **Create DTOs** for request/response
5. **Implement service layer** with business logic
6. **Add validation** in controller layer
7. **Create donation number generator** utility
8. **Add scheduled job** to clean up stale pending donations

---

## 💡 Business Logic Considerations

### Commission Calculation
```java
public BigDecimal calculateCommission(BigDecimal amount) {
    // Example: 5% platform fee
    BigDecimal commissionRate = new BigDecimal("0.05");
    return amount.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
}
```

### Donation Number Generation
```java
public String generateDonationNumber() {
    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String random = String.format("%05d", new Random().nextInt(100000));
    return "DON-" + date + "-" + random;
}
```

### Anonymous Donation Display
```java
public String getDisplayName() {
    if (isAnonymous) {
        return "Anonymous Supporter";
    }
    return donorName != null ? donorName : sender.getUsername();
}
```

---

**This model is production-ready for a "Buy Me a Coffee" style donation system!** 🎉
