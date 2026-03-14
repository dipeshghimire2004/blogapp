# Escrow Release Implementation Checklist

## Phase 1: Database & Models (2-3 hours)

### Enums
- [ ] Add `ACCEPTED` to `DonationStatus` enum
- [ ] Add `REVENUE` to `WalletStatus` enum  
- [ ] Add `SYSTEM` to `Role` enum
- [ ] Verify `LedgerEntityStatus` has all needed values

### Donation Entity
- [ ] Add `acceptedAt` field (LocalDateTime)
- [ ] Add `platformCommission` field (BigDecimal)
- [ ] Add `providerAmount` field (BigDecimal)
- [ ] Add database migration script

### Repositories
- [ ] Add `findBySenderIdAndStatus()` to DonationRepository
- [ ] Add `findByReceiverIdAndStatus()` to DonationRepository
- [ ] Add `countPendingAcceptanceForDonor()` to DonationRepository
- [ ] Create `LedgerEntryRepository` interface
- [ ] Add ledger query methods

---

## Phase 2: DTOs (1 hour)

- [ ] Create `AcceptDonationRequest.java`
- [ ] Create `AcceptDonationResponse.java`
- [ ] Create `DonationResponseDto.java`
- [ ] Add validation annotations

---

## Phase 3: Service Layer (4-5 hours)

### EscrowService Interface
- [ ] Create `EscrowService.java` interface
- [ ] Define `acceptDonation()` method
- [ ] Define `getPendingAcceptance()` method
- [ ] Define `getEscrowBalance()` method

### EscrowServiceImpl
- [ ] Create `EscrowServiceImpl.java`
- [ ] Implement donation validation logic
- [ ] Implement commission calculation (10%)
- [ ] Implement wallet retrieval/creation
- [ ] Implement atomic fund transfer
- [ ] Implement ledger entry creation
- [ ] Add comprehensive logging
- [ ] Add error handling

### Helper Methods
- [ ] `transferFunds()` - atomic transfer with ledger
- [ ] `getOrCreateWallet()` - wallet management
- [ ] `getPlatformWallet()` - platform wallet retrieval
- [ ] `toDonationResponseDto()` - DTO mapping

---

## Phase 4: Controller Layer (1-2 hours)

### EscrowController
- [ ] Create `EscrowController.java`
- [ ] Implement `POST /escrow/accept` endpoint
- [ ] Implement `GET /escrow/pending` endpoint
- [ ] Implement `GET /escrow/balance` endpoint
- [ ] Add Swagger/OpenAPI annotations
- [ ] Add request validation
- [ ] Add JWT authentication

---

## Phase 5: Configuration (1 hour)

### Platform User Setup
- [ ] Create `PlatformUserInitializer.java`
- [ ] Implement `ApplicationRunner` interface
- [ ] Create platform user on startup
- [ ] Create platform revenue wallet
- [ ] Add idempotency check
- [ ] Add logging

### Application Properties
- [ ] Add platform commission rate config (optional)
- [ ] Add platform user email config (optional)

---

## Phase 6: Testing (3-4 hours)

### Unit Tests
- [ ] `EscrowServiceImplTest.java`
  - [ ] Test successful acceptance
  - [ ] Test non-sender rejection
  - [ ] Test already accepted rejection
  - [ ] Test insufficient balance
  - [ ] Test wrong status rejection
  - [ ] Test commission calculation
  - [ ] Test wallet balance updates

### Integration Tests
- [ ] `EscrowControllerIntegrationTest.java`
  - [ ] Test end-to-end acceptance flow
  - [ ] Test authentication required
  - [ ] Test concurrent acceptance attempts
  - [ ] Test ledger entry creation

### Manual Testing
- [ ] Test with Postman
- [ ] Test with real Khalti payment
- [ ] Test edge cases
- [ ] Test error scenarios

---

## Phase 7: Documentation (1 hour)

- [ ] Update API documentation
- [ ] Add Swagger examples
- [ ] Document error codes
- [ ] Create Postman collection
- [ ] Update README

---

## Phase 8: Database Migration (1 hour)

### Migration Script
```sql
-- Add new columns to donation table
ALTER TABLE donation 
ADD COLUMN accepted_at TIMESTAMP,
ADD COLUMN platform_commission DECIMAL(10,2),
ADD COLUMN provider_amount DECIMAL(10,2);

-- Add new enum values (if using PostgreSQL enum)
ALTER TYPE donation_status ADD VALUE 'ACCEPTED';
ALTER TYPE wallet_status ADD VALUE 'REVENUE';
ALTER TYPE role ADD VALUE 'SYSTEM';

-- Create platform user (if not using initializer)
-- INSERT INTO users (id, username, email, password, role, is_active)
-- VALUES (gen_random_uuid(), 'platform', 'platform@blogapp.com', 
--         'hashed_password', 'SYSTEM', true);
```

- [ ] Create migration file
- [ ] Test on development database
- [ ] Backup production database
- [ ] Run on production

---

## Phase 9: Deployment (1-2 hours)

### Pre-Deployment
- [ ] Code review
- [ ] All tests passing
- [ ] Documentation complete
- [ ] Database migration ready

### Deployment Steps
- [ ] Deploy database migration
- [ ] Deploy backend code
- [ ] Verify platform user created
- [ ] Verify platform wallet created
- [ ] Test acceptance endpoint
- [ ] Monitor logs for errors

### Post-Deployment
- [ ] Smoke test all endpoints
- [ ] Check platform wallet balance
- [ ] Monitor error rates
- [ ] Check ledger entries

---

## Phase 10: Monitoring (Ongoing)

### Metrics to Track
- [ ] Total escrow balance
- [ ] Total platform revenue
- [ ] Number of pending acceptances
- [ ] Average acceptance time
- [ ] Failed acceptance attempts
- [ ] Wallet balance discrepancies

### Alerts to Set Up
- [ ] Escrow balance mismatch
- [ ] Failed acceptance rate > 5%
- [ ] Platform wallet balance anomaly
- [ ] Ledger entry creation failure

---

## Estimated Timeline

| Phase | Duration | Dependencies |
|-------|----------|--------------|
| 1. Database & Models | 2-3 hours | None |
| 2. DTOs | 1 hour | Phase 1 |
| 3. Service Layer | 4-5 hours | Phase 1, 2 |
| 4. Controller | 1-2 hours | Phase 3 |
| 5. Configuration | 1 hour | Phase 1 |
| 6. Testing | 3-4 hours | Phase 1-5 |
| 7. Documentation | 1 hour | Phase 1-6 |
| 8. Migration | 1 hour | Phase 1 |
| 9. Deployment | 1-2 hours | Phase 1-8 |
| 10. Monitoring | Ongoing | Phase 9 |

**Total Estimated Time:** 15-20 hours (2-3 days)

---

## Risk Assessment

### High Risk
- [ ] **Atomic Transaction Failure** - Use `@Transactional` properly
- [ ] **Balance Mismatch** - Validate before and after transfers
- [ ] **Double Acceptance** - Check `acceptedAt` field

### Medium Risk
- [ ] **Platform User Missing** - Use initializer or migration
- [ ] **Concurrent Requests** - Use database locks if needed
- [ ] **Rounding Errors** - Use `setScale(2, HALF_UP)`

### Low Risk
- [ ] **Performance** - Acceptable for MVP
- [ ] **Scalability** - Can optimize later

---

## Success Criteria

- [ ] Donor can accept donation via API
- [ ] 90% goes to provider's active wallet
- [ ] 10% goes to platform revenue wallet
- [ ] Escrow wallet balance is zero after acceptance
- [ ] Ledger entries created for audit trail
- [ ] Donation status updated to ACCEPTED
- [ ] Only donor can accept their own donations
- [ ] Cannot accept already accepted donations
- [ ] All operations are atomic (all-or-nothing)
- [ ] Comprehensive error handling
- [ ] Full test coverage (>80%)

---

## Next Steps After Backend Complete

1. **Frontend Implementation**
   - Add "Accept Donation" button
   - Show pending donations list
   - Display wallet balances
   - Show acceptance confirmation

2. **Admin Dashboard**
   - View platform revenue
   - View all escrow balances
   - View acceptance statistics
   - Export financial reports

3. **Withdrawal Feature**
   - Allow providers to withdraw from active wallet
   - Integrate with bank transfer API
   - Add withdrawal limits and verification

4. **Dispute System**
   - Allow donors to dispute donations
   - Freeze funds during dispute
   - Admin resolution interface

---

## Questions Before Starting

1. ✅ Commission rate confirmed at 10%?
2. ❓ Should donations auto-accept after X days?
3. ❓ What happens if donor never accepts?
4. ❓ Can donations be refunded? From where?
5. ❓ When can providers withdraw from active wallet?
6. ❓ Should we send email notifications on acceptance?
7. ❓ Should we track acceptance history?

---

## Ready to Start?

Once you approve this plan, I can begin implementing:
1. Start with Phase 1 (Database & Models)
2. Then Phase 2 (DTOs)
3. Then Phase 3 (Service Layer)
4. And so on...

Or we can implement all phases at once if you prefer!
