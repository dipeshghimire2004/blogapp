# Payment Acceptance & Commission System - Implementation Plan

## 📋 Overview

This phase implements the payment acceptance flow where:
1. Money is held in **PLATFORM_ESCROW** after Khalti payment
2. Writer can **accept/confirm** the donation (manual approval)
3. Upon acceptance, money is split:
   - **90%** → Writer's wallet (WRITER_WALLET)
   - **10%** → Platform revenue (PLATFORM_REVENUE)
4. Transaction history is maintained for audit

---

## 🎯 Current State Analysis

### Existing Components ✅
- ✅ Donation model with `commission` field
- ✅ Wallet model with status enum
- ✅ WalletStatus enum includes: PLATFORM_ESCROW, WRITER_WALLET, PLATFORM_REVENUE
- ✅ DonationStatus enum includes: INITIATED, IN_PROGRESS, COMPLETED, FAILED, REFUNDED, DISPUTED
- ✅ PaymentTransaction model for tracking
- ✅ TransactionType enum: DEBIT, CREDIT, WITHDRAWAL

### What Needs to Be Built 🔨
1. **Backend API endpoints** for donation acceptance
2. **Commission calculation** logic (10%)
3. **Wallet transfer** mechanism
4. **Transaction ledger** for audit trail
5. **Frontend UI** for writers to view and accept donations
6. **Admin dashboard** to monitor platform revenue

---

## 🏗️ Architecture Design

### Database Schema Updates

#### 1. Update DonationStatus Flow
```
INITIATED → IN_PROGRESS → PENDING_ACCEPTANCE → ACCEPTED → COMPLETED
                       ↓
                    FAILED / REFUNDED / DISPUTED
```

**New Status Needed:**
- `PENDING_ACCEPTANCE` - Payment verified, waiting for writer approval

#### 2. Wallet Types
```
PLATFORM_ESCROW      - Holds money after Khalti payment (before acceptance)
WRITER_WALLET        - Writer's available balance (after acceptance)
PLATFORM_REVENUE     - Platform's 10% commission
DISPUTE_HOLD         - For disputed transactions
WITHDRAWAL_PROCESSING - Money being withdrawn
```

#### 3. New Model: WalletTransaction
Track all wallet movements for audit trail.

```java
@Entity
public class WalletTransaction {
    private UUID id;
    private Wallet sourceWallet;      // From which wallet
    private Wallet destinationWallet; // To which wallet
    private BigDecimal amount;
    private TransactionType typ