# Escrow Release Flow Diagram

## Money Flow Visualization

```
┌─────────────────────────────────────────────────────────────────┐
│                    INITIAL STATE                                 │
│                                                                  │
│  Donor pays 100 NPR via Khalti                                  │
│         ↓                                                        │
│  Money goes to Receiver's ESCROW Wallet                         │
│                                                                  │
│  Receiver ESCROW Wallet: 100 NPR                                │
│  Receiver ACTIVE Wallet: 0 NPR                                  │
│  Platform REVENUE Wallet: 0 NPR                                 │
└─────────────────────────────────────────────────────────────────┘
                            ↓
                            ↓ Donor clicks "Accept Donation"
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│                    ESCROW RELEASE                                │
│                                                                  │
│  Calculate Split:                                                │
│  - Platform Commission: 100 × 10% = 10 NPR                      │
│  - Provider Amount: 100 × 90% = 90 NPR                          │
│                                                                  │
│  Transfer 1: ESCROW → ACTIVE (90 NPR)                           │
│  Transfer 2: ESCROW → REVENUE (10 NPR)                          │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│                    FINAL STATE                                   │
│                                                                  │
│  Receiver ESCROW Wallet: 0 NPR                                  │
│  Receiver ACTIVE Wallet: 90 NPR ✅ (can withdraw)               │
│  Platform REVENUE Wallet: 10 NPR ✅ (platform profit)           │
│                                                                  │
│  Donation Status: ACCEPTED                                       │
│  LedgerEntry Records: 2 (audit trail)                           │
└─────────────────────────────────────────────────────────────────┘
```

---

## Wallet State Transitions

```
┌──────────────┐
│ ESCROW       │  Holding area for unconfirmed donations
│ (Cannot      │  - Money locked until donor accepts
│  withdraw)   │  - Protected from disputes
└──────┬───────┘
       │
       │ Donor accepts
       │
       ↓
┌──────────────┐
│ ACTIVE       │  Available for withdrawal
│ (Can         │  - Provider can request withdrawal
│  withdraw)   │  - Can be used for platform features
└──────────────┘

┌──────────────┐
│ REVENUE      │  Platform earnings
│ (Platform    │  - 10% commission from all donations
│  only)       │  - Used for operational costs
└──────────────┘
```

---

## Database State Changes

### Before Acceptance

**Donation Table:**
```sql
id: uuid-123
sender_id: donor-uuid
receiver_id: author-uuid
amount: 100.00
status: COMPLETED
accepted_at: NULL
platform_commission: NULL
provider_amount: NULL
```

**Wallet Table:**
```sql
-- Receiver's Escrow Wallet
id: wallet-escrow-uuid
user_id: author-uuid
balance: 100.00
status: ESCROW

-- Receiver's Active Wallet
id: wallet-active-uuid
user_id: author-uuid
balance: 0.00
status: ACTIVE

-- Platform Revenue Wallet
id: wallet-revenue-uuid
user_id: platform-uuid
balance: 0.00
status: REVENUE
```

**LedgerEntry Table:**
```sql
-- No entries yet
```

---

### After Acceptance

**Donation Table:**
```sql
id: uuid-123
sender_id: donor-uuid
receiver_id: author-uuid
amount: 100.00
status: ACCEPTED ✅
accepted_at: 2026-02-27T10:30:00 ✅
platform_commission: 10.00 ✅
provider_amount: 90.00 ✅
```

**Wallet Table:**
```sql
-- Receiver's Escrow Wallet
id: wallet-escrow-uuid
user_id: author-uuid
balance: 0.00 ✅ (100 - 90 - 10)
status: ESCROW

-- Receiver's Active Wallet
id: wallet-active-uuid
user_id: author-uuid
balance: 90.00 ✅ (0 + 90)
status: ACTIVE

-- Platform Revenue Wallet
id: wallet-revenue-uuid
user_id: platform-uuid
balance: 10.00 ✅ (0 + 10)
status: REVENUE
```

**LedgerEntry Table:**
```sql
-- Entry 1: Provider share
id: ledger-1-uuid
from_wallet_id: wallet-escrow-uuid
to_wallet_id: wallet-active-uuid
amount: 90.00
status: COMPLETED
note: "Donation acceptance - Provider share - Donation: DON-123"

-- Entry 2: Platform commission
id: ledger-2-uuid
from_wallet_id: wallet-escrow-uuid
to_wallet_id: wallet-revenue-uuid
amount: 10.00
status: COMPLETED
note: "Donation acceptance - Platform commission - Donation: DON-123"
```

---

## API Call Sequence

```
┌──────────┐                ┌──────────┐                ┌──────────┐
│  Donor   │                │ Backend  │                │ Database │
│ (Client) │                │  API     │                │          │
└────┬─────┘                └────┬─────┘                └────┬─────┘
     │                           │                           │
     │ POST /escrow/accept       │                           │
     │ { donationId: "uuid" }    │                           │
     ├──────────────────────────>│                           │
     │                           │                           │
     │                           │ 1. Validate donation      │
     │                           ├──────────────────────────>│
     │                           │ SELECT * FROM donation    │
     │                           │<──────────────────────────┤
     │                           │                           │
     │                           │ 2. Check donor is sender  │
     │                           │                           │
     │                           │ 3. Calculate split        │
     │                           │                           │
     │                           │ 4. Get wallets            │
     │                           ├──────────────────────────>│
     │                           │ SELECT * FROM wallet      │
     │                           │<──────────────────────────┤
     │                           │                           │
     │                           │ 5. BEGIN TRANSACTION      │
     │                           ├──────────────────────────>│
     │                           │                           │
     │                           │ 6. Update escrow wallet   │
     │                           ├──────────────────────────>│
     │                           │ UPDATE wallet SET         │
     │                           │   balance = balance - 100 │
     │                           │                           │
     │                           │ 7. Update active wallet   │
     │                           ├──────────────────────────>│
     │                           │ UPDATE wallet SET         │
     │                           │   balance = balance + 90  │
     │                           │                           │
     │                           │ 8. Update revenue wallet  │
     │                           ├──────────────────────────>│
     │                           │ UPDATE wallet SET         │
     │                           │   balance = balance + 10  │
     │                           │                           │
     │                           │ 9. Create ledger entries  │
     │                           ├──────────────────────────>│
     │                           │ INSERT INTO ledger_entry  │
     │                           │   (2 records)             │
     │                           │                           │
     │                           │ 10. Update donation       │
     │                           ├──────────────────────────>│
     │                           │ UPDATE donation SET       │
     │                           │   status = 'ACCEPTED'     │
     │                           │   accepted_at = NOW()     │
     │                           │                           │
     │                           │ 11. COMMIT TRANSACTION    │
     │                           ├──────────────────────────>│
     │                           │                           │
     │ Response 200              │                           │
     │ { totalAmount: 100,       │                           │
     │   platformCommission: 10, │                           │
     │   providerAmount: 90 }    │                           │
     │<──────────────────────────┤                           │
     │                           │                           │
```

---

## Error Scenarios

### Scenario 1: Non-Donor Tries to Accept
```
Donor A donates to Author B
User C tries to accept donation
❌ Error: "Only the donor can accept this donation"
```

### Scenario 2: Already Accepted
```
Donor A accepts donation
Donor A tries to accept again
❌ Error: "Donation already accepted at: 2026-02-27T10:30:00"
```

### Scenario 3: Insufficient Escrow Balance
```
Escrow wallet: 50 NPR
Donation amount: 100 NPR
❌ Error: "Insufficient escrow balance. Required: 100, Available: 50"
```

### Scenario 4: Wrong Status
```
Donation status: PENDING
Donor tries to accept
❌ Error: "Donation must be in COMPLETED status. Current: PENDING"
```

---

## Commission Calculation Examples

### Example 1: 100 NPR Donation
```
Total: 100.00 NPR
Platform (10%): 10.00 NPR
Provider (90%): 90.00 NPR
```

### Example 2: 250 NPR Donation
```
Total: 250.00 NPR
Platform (10%): 25.00 NPR
Provider (90%): 225.00 NPR
```

### Example 3: 1000 NPR Donation
```
Total: 1000.00 NPR
Platform (10%): 100.00 NPR
Provider (90%): 900.00 NPR
```

### Example 4: 15.50 NPR Donation (with rounding)
```
Total: 15.50 NPR
Platform (10%): 1.55 NPR (rounded to 2 decimals)
Provider (90%): 13.95 NPR
```

---

## Wallet Balance Tracking

### Provider's Perspective
```
┌─────────────────────────────────────────────────────┐
│ My Wallets                                          │
├─────────────────────────────────────────────────────┤
│ ESCROW Balance:  250.00 NPR                         │
│ (Pending acceptance from donors)                    │
│                                                     │
│ ACTIVE Balance:  450.00 NPR                         │
│ (Available for withdrawal)                          │
│                                                     │
│ Total Earnings:  700.00 NPR                         │
└─────────────────────────────────────────────────────┘
```

### Platform's Perspective
```
┌─────────────────────────────────────────────────────┐
│ Platform Revenue                                    │
├─────────────────────────────────────────────────────┤
│ Total Revenue:   5,432.10 NPR                       │
│ This Month:      1,234.50 NPR                       │
│ Today:           123.45 NPR                         │
│                                                     │
│ Total Donations: 54,321.00 NPR                      │
│ Commission Rate: 10%                                │
└─────────────────────────────────────────────────────┘
```

---

## Timeline Example

```
Day 1, 10:00 AM
├─ Donor donates 100 NPR
├─ Payment verified
└─ 100 NPR in ESCROW wallet

Day 1, 10:30 AM
├─ Donor clicks "Accept Donation"
├─ 90 NPR → Provider ACTIVE wallet
├─ 10 NPR → Platform REVENUE wallet
└─ Donation status: ACCEPTED

Day 2, 09:00 AM
├─ Provider requests withdrawal
├─ 90 NPR transferred to bank
└─ ACTIVE wallet: 0 NPR
```

---

## Security Checks

```
┌─────────────────────────────────────────────────────┐
│ Validation Checklist                                │
├─────────────────────────────────────────────────────┤
│ ✅ Is user authenticated?                           │
│ ✅ Does donation exist?                             │
│ ✅ Is user the donor (sender)?                      │
│ ✅ Is donation status COMPLETED?                    │
│ ✅ Is donation not already accepted?                │
│ ✅ Does escrow have sufficient balance?             │
│ ✅ Are all wallets available?                       │
│ ✅ Is transaction atomic?                           │
└─────────────────────────────────────────────────────┘
```

---

## Monitoring Dashboard

```
┌─────────────────────────────────────────────────────┐
│ Escrow System Health                                │
├─────────────────────────────────────────────────────┤
│ Total in Escrow:        12,345.00 NPR               │
│ Pending Acceptances:    45 donations                │
│ Accepted Today:         23 donations                │
│ Failed Acceptances:     2 (investigate)             │
│                                                     │
│ Platform Revenue:       5,432.10 NPR                │
│ Provider Earnings:      48,888.90 NPR               │
│                                                     │
│ Average Acceptance Time: 2.5 hours                  │
│ Oldest Pending:         3 days                      │
└─────────────────────────────────────────────────────┘
```

