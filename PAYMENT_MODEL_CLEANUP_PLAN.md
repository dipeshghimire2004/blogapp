# Payment Model Cleanup Plan

## Executive Summary

**Current State:** 6 payment entities, 2 are redundant, 2 are unused  
**Target State:** 3 active entities for MVP  
**Action:** Delete 1, document 2 as future features

---

## Issues Found

### 🔴 Critical: Account Entity is Redundant
- **Problem:** Duplicates Wallet functionality
- **Impact:** Confusing codebase, maintenance overhead
- **Evidence:** Zero usage in entire codebase
- **Action:** DELETE

### 🟡 Warning: Unused Entities
- **LedgerEntry:** Not implemented, no repository, no service
- **AuditLog:** Not implemented, no repository, no service
- **Impact:** Dead code, confusing for developers
- **Action:** Keep but mark as "Future Feature"

### 🟢 Good: Core Entities
- **Donation:** ✅ Properly used, correct relationships
- **PaymentTransaction:** ✅ Used, but has redundant fields
- **Wallet:** ✅ Properly used

---

## Detailed Analysis

### 1. Wallet vs Account Comparison

| Feature | Wallet | Account | Winner |
|---------|--------|---------|--------|
| User Relationship | Direct (ManyToOne) | Polymorphic (UUID) | Wallet ✅ |
| Type Safety | Strong | Weak | Wallet ✅ |
| Simplicity | Simple | Complex | Wallet ✅ |
| Usage | Active | None | Wallet ✅ |
| Status Enum | WalletStatus | AccountStatus | Wallet ✅ |

**Verdict:** Account adds no value, delete it.

---

### 2. Sender/Receiver Relationships

#### Current Implementation (Donation)
```java
@ManyToOne
@JoinColumn(name = "sender_id")
private User sender;  // ✅ CORRECT

@ManyToOne
@JoinColumn(name = "receiver_id")
private User receiver;  // ✅ CORRECT
```

#### Why User (not Wallet)?

**✅ Correct Approach:**
```java
// Business logic: Donation is between people
Donation donation = new Donation();
donation.setSender(currentUser);
donation.setReceiver(postAuthor);

// Wallet is derived when needed
Wallet receiverWallet = walletRepository
    .findByUserAndStatus(postAuthor, WalletStatus.ESCROW);
```

**❌ Wrong Approach:**
```java
// Don't do this - tight coupling
Donation donation = new Donation();
donation.setSenderWallet(senderWallet);
donation.setReceiverWallet(receiverWallet);
```

**Reasons:**
1. **Flexibility:** User can have multiple wallets (active, escrow, frozen)
2. **Business Logic:** Donation is a user action, not a wallet action
3. **Clarity:** `donation.sender.username` vs `donation.senderWallet.user.username`
4. **Decoupling:** Wallet implementation can change without affecting Donation

---

### 3. PaymentTransaction Issues

#### Current Problems
```java
@ManyToOne
private User user;  // ⚠️ Ambiguous - sender or receiver?

@Enumerated(EnumType.STRING)
private TransactionType transactionType;  // ❌ Always DEBIT for payments

@Enumerated(EnumType.STRING)
private DonationStatus donationStatus;  // ❌ Duplicates donation.status
```

#### Recommended Cleanup
```java
// Remove these fields:
- user (use donation.sender)
- transactionType (always payment from gateway)
- donationStatus (use donation.status)

// Keep these:
- donation (link to business transaction)
- amount, currency, gateway
- pidx (Khalti payment ID)
- rawResponse (gateway response)
- paymentStatus (PENDING, COMPLETED, FAILED)
```

---

## Action Plan

### Phase 1: Delete Redundant Entity (Now)

**1. Delete Account Entity**
```bash
# Delete model
rm src/main/java/org/blogapp/dg_blogapp/payment/model/Account.java

# Delete enums
rm src/main/java/org/blogapp/dg_blogapp/payment/enums/AccountType.java
rm src/main/java/org/blogapp/dg_blogapp/payment/enums/AccountStatus.java

# No repository/service to delete (never created)
```

**Impact:** None - entity is not used anywhere

---

### Phase 2: Document Unused Entities (Now)

**1. Add JavaDoc to LedgerEntry**
```java
/**
 * LedgerEntry - Double-entry bookkeeping for wallet transfers
 * 
 * STATUS: Not implemented in MVP
 * PLANNED: Phase 2 - Withdrawal Feature
 * 
 * Purpose:
 * - Track wallet-to-wallet transfers
 * - Maintain audit trail for financial reconciliation
 * - Support withdrawal to bank account
 * 
 * Example Usage (Future):
 * <pre>
 * LedgerEntry entry = LedgerEntry.builder()
 *     .from_wallet(userWallet)
 *     .to_wallet(platformWallet)
 *     .amount(withdrawalAmount)
 *     .status(LedgerEntityStatus.COMPLETED)
 *     .note("Withdrawal to bank")
 *     .build();
 * </pre>
 * 
 * @see Wallet
 * @see WithdrawalRequest
 */
@Entity
@Table(name = "ledger_entity")
public class LedgerEntry extends BaseEntity { ... }
```

**2. Add JavaDoc to AuditLog**
```java
/**
 * AuditLog - Generic audit trail for compliance
 * 
 * STATUS: Not implemented in MVP
 * PLANNED: Phase 3 - Compliance & Reporting
 * 
 * Current Alternative:
 * - Use BaseEntity.createdAt and updatedAt
 * - Use Donation.status history
 * - Use PaymentTransaction.rawResponse
 * 
 * Future Improvements:
 * - Add entityType and entityId
 * - Add userId (who performed action)
 * - Add before/after values
 * - Add IP address and user agent
 * 
 * @deprecated Use entity timestamps for MVP
 */
@Entity
public class AuditLog extends BaseEntity { ... }
```

---

### Phase 3: Clean PaymentTransaction (Optional)

**Current:**
```java
@Entity
public class PaymentTransaction extends BaseEntity {
    @ManyToOne private User user;  // Remove
    @ManyToOne private Donation donation;
    private BigDecimal amount;
    private String currency;
    private GatewayType gateway;
    private TransactionType transactionType;  // Remove
    private String pidx;
    private String rawResponse;
    private DonationStatus donationStatus;  // Remove
    private PaymentStatus paymentStatus;
}
```

**Recommended:**
```java
@Entity
public class PaymentTransaction extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "donation_id", nullable = false)
    private Donation donation;  // Links to business transaction
    
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private String currency = "NPR";
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GatewayType gateway = GatewayType.KHALTI;
    
    @Column(unique = true)
    private String pidx;  // Khalti payment identifier
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String rawResponse;  // Store Khalti API response
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    // Helper methods
    public User getSender() {
        return donation.getSender();
    }
    
    public User getReceiver() {
        return donation.getReceiver();
    }
}
```

**Migration Steps:**
1. Add helper methods first (backward compatible)
2. Update service to use helper methods
3. Remove old fields in next release
4. Create database migration

---

## Entity Relationship Diagram

### Current MVP Structure
```
┌─────────┐
│  User   │
└────┬────┘
     │
     ├─────────────┐
     │             │
     ▼             ▼
┌─────────┐   ┌──────────┐
│ Wallet  │   │ BlogPost │
└─────────┘   └────┬─────┘
                   │
     ┌─────────────┴─────────────┐
     │                           │
     ▼                           ▼
┌──────────┐              ┌──────────┐
│ Donation │◄─────────────│ Payment  │
│          │              │Transaction│
│ sender   │              └──────────┘
│ receiver │
└──────────┘
```

### Relationships Explained
- **User → Wallet:** One user can have multiple wallets (active, escrow)
- **User → BlogPost:** One user can create multiple posts
- **User → Donation (sender):** One user can make multiple donations
- **User → Donation (receiver):** One user can receive multiple donations
- **Donation → PaymentTransaction:** One donation has one payment transaction
- **BlogPost → Donation:** One post can receive multiple donations

---

## Testing Checklist

After cleanup, verify:

- [ ] Donation creation works
- [ ] Payment initiation works
- [ ] Payment verification works
- [ ] Wallet credit works
- [ ] No references to Account entity
- [ ] No compilation errors
- [ ] Database migrations run successfully
- [ ] Existing donations still accessible

---

## Database Migration

### Drop Account Table
```sql
-- Check if table exists and has data
SELECT COUNT(*) FROM account;

-- If empty, drop it
DROP TABLE IF EXISTS account CASCADE;
```

### Optional: Clean PaymentTransaction
```sql
-- Add helper columns (if implementing Phase 3)
ALTER TABLE payment_transaction 
    ADD COLUMN IF NOT EXISTS sender_id UUID,
    ADD COLUMN IF NOT EXISTS receiver_id UUID;

-- Populate from donation
UPDATE payment_transaction pt
SET 
    sender_id = d.sender_id,
    receiver_id = d.receiver_id
FROM donation d
WHERE pt.donate_id = d.id;

-- Later: Drop old columns
-- ALTER TABLE payment_transaction DROP COLUMN user_id;
-- ALTER TABLE payment_transaction DROP COLUMN transaction_type;
-- ALTER TABLE payment_transaction DROP COLUMN donation_status;
```

---

## Summary

### Immediate Actions (Do Now)
1. ✅ Delete Account entity and enums
2. ✅ Add documentation to LedgerEntry
3. ✅ Add documentation to AuditLog

### Optional Actions (Later)
1. ⏸️ Clean PaymentTransaction fields
2. ⏸️ Add helper methods to PaymentTransaction
3. ⏸️ Create database migration

### No Action Needed
1. ✅ Donation entity (perfect as-is)
2. ✅ Wallet entity (working well)
3. ✅ Sender/Receiver relationships (correct)

**Result:** Cleaner codebase, easier maintenance, clear MVP scope
