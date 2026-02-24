# 🔍 Payment System Data Model Review

## Your Current Flow Analysis

### Flow Overview
```
Client → Payment Gateway (Khalti/eSewa) → Platform Wallet → [Escrow 24h] → Writer
```

### Key Requirements
1. ✅ Money goes to platform merchant wallet (NOT writer directly)
2. ✅ Escrow period: 24 hours
3. ✅ Dispute handling before release
4. ✅ Trust mechanism for both parties

---

## ✅ Your Existing Data Model (EXCELLENT!)

You already have a solid foundation:

### 1. Wallet System ✅
```java
Wallet {
    user: User
    status: WalletStatus (ESCROW, WRITER_WALLET, PLATFORM_REVENUE, DISPUTE_HOLD)
}
```

### 2. Ledger System ✅
```java
LedgerEntry {
    from_wallet: Wallet
    to_wallet: Wallet
    amount: BigDecimal
    status: LedgerEntityStatus (PAYMENT, COMMISSION, RELEASE, REFUND, DISPUTE)
    note: String
}
```

### 3. Withdrawal System ✅
```java
WithdrawalRequest {
    user: User
    amount: BigDecimal
    status: DonationStatus
}
```

---

## 📊 Complete Data Model for Escrow Flow

### Entity Relationships
```
ServiceOrder (1) ←→ (1) Payment
ServiceOrder (1) ←→ (*) LedgerEntry
ServiceOrder (*) → (1) User (client)
ServiceOrder (*) → (1) User (writer)
User (1) ←→ (1) Wallet
Wallet (1) ←→ (*) LedgerEntry
```

---

## 🔄 Complete Escrow Flow with Your Models


### Step 1: Client Submits Order
```java
ServiceOrder order = ServiceOrder.builder()
    .orderNum