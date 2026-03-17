# Withdrawal Feature - Implementation Plan

## Overview

Allow content creators to withdraw funds from their WRITER_WALLET to their bank account.

---

## Prerequisites Analysis

### ✅ Already Implemented
1. **Wallet System**
   - WRITER_WALLET (active wallet with withdrawable funds)
   - PLATFORM_ESCROW (holding area)
   - PLATFORM_REVENUE (platform earnings)

2. **Donation Acceptance Flow**
   - Donors can accept donations
   - 90% goes to WRITER_WALLET
   - 10% goes to PLATFORM_REVENUE

3. **LedgerEntry Model**
   - Tracks all fund transfers
   - Audit trail for transactions

4. **User Authentication**
   - JWT-based authentication
   - User identification system

### ❌ Missing Prerequisites

#### 1. Bank Account Information Storage
**Need:** Store user's bank account details securely

**Required Fields:**
- Bank name
- Account holder name
- Account number
- Bank branch (optional)
- SWIFT/IFSC code (for Nepal: Bank code)
- Account type (Savings/Current)

**Security Considerations:**
- Encrypt sensitive data (account number)
- Store only necessary information
- Verify account ownership

#### 2. Withdrawal Request Model
**Need:** Track withdrawal requests and their status

**Required Fields:**
- User ID
- Amount
- Bank account reference
- Status (PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED)
- Request date
- Processing date
- Completion date
- Transaction reference
- Rejection reason (if failed)

#### 3. Payment Gateway Integration
**Options for Nepal:**

**Option A: Manual Bank Transfer (MVP)**
- Admin manually processes withdrawals
- User submits request
- Admin transfers via bank
- Admin marks as completed

**Option B: Khalti Payout API**
- Automated transfers to bank
- Requires Khalti business account
- API integration needed

**Option C: eSewa Merchant API**
- Alternative payment gateway
- Similar to Khalti

**Recommendation for MVP:** Start with Option A (Manual)

#### 4. Minimum Withdrawal Amount
**Need:** Set minimum threshold to avoid micro-transactions

**Considerations:**
- Bank transfer fees
- Processing costs
- User experience

**Recommendation:** NPR 500 minimum

#### 5. Withdrawal Limits
**Need:** Prevent fraud and manage cash flow

**Types:**
- Daily limit (e.g., NPR 50,000)
- Weekly limit (e.g., NPR 200,000)
- Per-transaction limit (e.g., NPR 100,000)

#### 6. KYC/Verification
**Need:** Verify user identity before allowing withdrawals

**Levels:**
- Level 1: Email verified (withdraw up to NPR 10,000)
- Level 2: Phone verified (withdraw up to NPR 50,000)
- Level 3: ID verified (unlimited)

**For MVP:** Email verification only

#### 7. Admin Dashboard
**Need:** Interface for admins to process withdrawals

**Features:**
- View pending withdrawals
- Approve/reject requests
- Mark as completed
- View transaction history
- Export reports

---

## Implementation Phases

### Phase 1: Database & Models (MVP)
**Estimated Time:** 4-6 hours

**Tasks:**
1. Create BankAccount entity
2. Create WithdrawalRequest entity
3. Add withdrawal-related enums
4. Create repositories
5. Database migrations

### Phase 2: Service Layer (MVP)
**Estimated Time:** 6-8 hours

**Tasks:**
1. WithdrawalService interface
2. WithdrawalServiceImpl
3. Validation logic
4. Balance checks
5. Ledger entry creation

### Phase 3: API Endpoints (MVP)
**Estimated Time:** 3-4 hours

**Tasks:**
1. POST /withdrawal/request
2. GET /withdrawal/history
3. GET /withdrawal/balance
4. POST /withdrawal/bank-account (add/update)
5. GET /withdrawal/bank-account

### Phase 4: Admin Endpoints
**Estimated Time:** 4-5 hours

**Tasks:**
1. GET /admin/withdrawal/pending
2. POST /admin/withdrawal/approve
3. POST /admin/withdrawal/reject
4. POST /admin/withdrawal/complete

### Phase 5: Frontend (User)
**Estimated Time:** 6-8 hours

**Tasks:**
1. Withdrawal request page
2. Bank account management
3. Withdrawal history
4. Balance display

### Phase 6: Frontend (Admin)
**Estimated Time:** 6-8 hours

**Tasks:**
1. Admin dashboard
2. Pending withdrawals list
3. Approval interface
4. Transaction history

---

## Total Estimated Time
**MVP (Manual Processing):** 29-39 hours (4-5 days)
**With Automation:** +15-20 hours (payment gateway integration)

---

## Next Steps

Would you like me to:
1. Create a detailed spec for the withdrawal feature?
2. Start implementing Phase 1 (Database & Models)?
3. Create a requirements document first?

Let me know your preference!
