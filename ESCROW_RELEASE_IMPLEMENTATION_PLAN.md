# Escrow Release Implementation Plan

## Overview

Implement a system where donors can confirm/accept donations, triggering the release of funds from escrow to:
- **90%** to service provider (blog post author)
- **10%** to platform revenue

---

## Business Flow

```
┌─────────────────────────────────────────────────────────────┐
│ Current State: Money in Escrow                              │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ Donor clicks "Confirm/Accept Donation"                      │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ Backend validates:                                           │
│ - Donation exists and is COMPLETED                          │
│ - Donor is the sender                                       │
│ - Funds are in escrow                                       │
│ - Not already accepted                                      │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ Calculate Split:                                             │
│ - Platform Commission: 10% of amount                        │
│ - Provider Amount: 90% of amount                            │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ Execute Transfers (Atomic Transaction):                     │
│ 1. Deduct from receiver's ESCROW wallet                     │
│ 2. Credit receiver's ACTIVE wallet (90%)                    │
│ 3. Credit platform REVENUE wallet (10%)                     │
│ 4. Create LedgerEntry records                               │
│ 5. Update Donation status to ACCEPTED                       │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ Result: Money distributed, ready for withdrawal             │
└─────────────────────────────────────────────────────────────┘
```

---

## Database Schema Changes

### 1. Add New Enum Values

**DonationStatus.java**
```java
public enum DonationStatus {
    INITIATED,      // Donation created
    PENDING,        // Payment initiated
    COMPLETED,      // Payment verified, in escrow
    ACCEPTED,       // ✨ NEW: Donor confirmed, funds released
    DISPUTED,       // Donor raised dispute
    REFUNDED,       // Money returned to donor
    FAILED          // Payment failed
}
```

**WalletStatus.java**
```java
public enum WalletStatus {
    ACTIVE,         // User's main wallet (can withdraw)
    ESCROW,         // Holding funds (cannot withdraw)
    FROZEN,         // Suspended account
    REVENUE         // ✨ NEW: Platform revenue wallet
}
```

**LedgerEntityStatus.java**
```java
public enum LedgerEntityStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REVERSED
}
```

### 2. Update Donation Entity

**Add new fields:**
```java
@Entity
public class Donation extends BaseEntity {
    // ... existing fields ...
    
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;  // ✨ NEW: When donor confirmed
    
    @Column(name = "platform_commission")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal platformCommission;  // ✨ NEW: 10% commission
    
    @Column(name = "provider_amount")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal providerAmount;  // ✨ NEW: 90% to provider
}
```

### 3. Create Platform User (System Account)

**Migration or initialization:**
```java
// Create a system user for platform
User platformUser = User.builder()
    .username("platform")
    .email("platform@blogapp.com")
    .role(Role.SYSTEM)
    .isActive(true)
    .build();

// Create platform revenue wallet
Wallet platformWallet = Wallet.builder()
    .user(platformUser)
    .balance(BigDecimal.ZERO)
    .status(WalletStatus.REVENUE)
    .build();
```

---

## Backend Implementation

### Phase 1: Core Service Layer

#### 1.1 Create EscrowService Interface

**File:** `payment/service/EscrowService.java`
```java
public interface EscrowService {
    /**
     * Release funds from escrow to provider and platform
     * @param donationId The donation to accept
     * @param donorUserId The user confirming (must be sender)
     * @return AcceptDonationResponse with transfer details
     */
    AcceptDonationResponse acceptDonation(UUID donationId, UUID donorUserId);
    
    /**
     * Get all donations pending acceptance for a donor
     * @param donorUserId The donor's user ID
     * @return List of donations in COMPLETED status
     */
    List<DonationResponseDto> getPendingAcceptance(UUID donorUserId);
    
    /**
     * Get escrow balance for a user
     * @param userId The user ID
     * @return Escrow wallet balance
     */
    BigDecimal getEscrowBalance(UUID userId);
}
```

#### 1.2 Create DTOs

**File:** `payment/dto/AcceptDonationRequest.java`
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcceptDonationRequest {
    @NotNull
    private UUID donationId;
}
```

**File:** `payment/dto/AcceptDonationResponse.java`
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcceptDonationResponse {
    private UUID donationId;
    private BigDecimal totalAmount;
    private BigDecimal platformCommission;
    private BigDecimal providerAmount;
    private String providerUsername;
    private LocalDateTime acceptedAt;
    private String message;
}
```

**File:** `payment/dto/DonationResponseDto.java`
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationResponseDto {
    private UUID id;
    private String donationNumber;
    private BigDecimal amount;
    private String receiverUsername;
    private String blogPostTitle;
    private DonationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
}
```

#### 1.3 Implement EscrowServiceImpl

**File:** `payment/service/impl/EscrowServiceImpl.java`
```java
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class EscrowServiceImpl implements EscrowService {

    private final DonationRepository donationRepository;
    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final UserRepository userRepository;
    
    private static final BigDecimal PLATFORM_COMMISSION_RATE = new BigDecimal("0.10"); // 10%
    private static final String PLATFORM_USER_EMAIL = "platform@blogapp.com";

    @Override
    public AcceptDonationResponse acceptDonation(UUID donationId, UUID donorUserId) {
        log.info("Processing donation acceptance: donationId={}, donorUserId={}", 
                 donationId, donorUserId);
        
        // 1. Validate donation
        Donation donation = donationRepository.findById(donationId)
            .orElseThrow(() -> new RuntimeException("Donation not found"));
        
        // 2. Validate donor is the sender
        if (!donation.getSender().getId().equals(donorUserId)) {
            throw new RuntimeException("Only the donor can accept this donation");
        }
        
        // 3. Validate donation status
        if (donation.getStatus() != DonationStatus.COMPLETED) {
            throw new RuntimeException("Donation must be in COMPLETED status. Current: " 
                                     + donation.getStatus());
        }
        
        // 4. Check if already accepted
        if (donation.getAcceptedAt() != null) {
            throw new RuntimeException("Donation already accepted at: " 
                                     + donation.getAcceptedAt());
        }
        
        // 5. Calculate split
        BigDecimal totalAmount = donation.getAmount();
        BigDecimal platformCommission = totalAmount.multiply(PLATFORM_COMMISSION_RATE)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal providerAmount = totalAmount.subtract(platformCommission);
        
        log.info("Amount split - Total: {}, Platform: {}, Provider: {}", 
                 totalAmount, platformCommission, providerAmount);
        
        // 6. Get wallets
        User receiver = donation.getReceiver();
        Wallet escrowWallet = getOrCreateWallet(receiver, WalletStatus.ESCROW);
        Wallet activeWallet = getOrCreateWallet(receiver, WalletStatus.ACTIVE);
        Wallet platformWallet = getPlatformWallet();
        
        // 7. Validate escrow has sufficient balance
        if (escrowWallet.getBalance().compareTo(totalAmount) < 0) {
            throw new RuntimeException("Insufficient escrow balance. Required: " 
                                     + totalAmount + ", Available: " 
                                     + escrowWallet.getBalance());
        }
        
        // 8. Execute transfers (atomic)
        transferFunds(escrowWallet, activeWallet, providerAmount, 
                     "Donation acceptance - Provider share", donation);
        transferFunds(escrowWallet, platformWallet, platformCommission, 
                     "Donation acceptance - Platform commission", donation);
        
        // 9. Update donation
        donation.setStatus(DonationStatus.ACCEPTED);
        donation.setAcceptedAt(LocalDateTime.now());
        donation.setPlatformCommission(platformCommission);
        donation.setProviderAmount(providerAmount);
        donationRepository.save(donation);
        
        log.info("Donation accepted successfully: {}", donationId);
        
        // 10. Build response
        return AcceptDonationResponse.builder()
            .donationId(donationId)
            .totalAmount(totalAmount)
            .platformCommission(platformCommission)
            .providerAmount(providerAmount)
            .providerUsername(receiver.getUsername())
            .acceptedAt(donation.getAcceptedAt())
            .message("Donation accepted successfully")
            .build();
    }
    
    private void transferFunds(Wallet fromWallet, Wallet toWallet, 
                              BigDecimal amount, String note, Donation donation) {
        // Deduct from source
        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        walletRepository.save(fromWallet);
        
        // Credit to destination
        toWallet.setBalance(toWallet.getBalance().add(amount));
        walletRepository.save(toWallet);
        
        // Create ledger entry
        LedgerEntry ledgerEntry = LedgerEntry.builder()
            .from_wallet(fromWallet)
            .to_wallet(toWallet)
            .amount(amount)
            .status(LedgerEntityStatus.COMPLETED)
            .note(note + " - Donation: " + donation.getDonationNumber())
            .build();
        ledgerEntryRepository.save(ledgerEntry);
        
        log.info("Transfer completed: {} -> {}, Amount: {}", 
                 fromWallet.getId(), toWallet.getId(), amount);
    }
    
    private Wallet getOrCreateWallet(User user, WalletStatus status) {
        return walletRepository.findByUserAndStatus(user, status)
            .orElseGet(() -> {
                log.info("Creating {} wallet for user: {}", status, user.getId());
                Wallet wallet = Wallet.builder()
                    .user(user)
                    .balance(BigDecimal.ZERO)
                    .status(status)
                    .build();
                return walletRepository.save(wallet);
            });
    }
    
    private Wallet getPlatformWallet() {
        User platformUser = userRepository.findByEmail(PLATFORM_USER_EMAIL)
            .orElseThrow(() -> new RuntimeException("Platform user not found. " +
                                                   "Run initialization script."));
        return getOrCreateWallet(platformUser, WalletStatus.REVENUE);
    }
    
    @Override
    public List<DonationResponseDto> getPendingAcceptance(UUID donorUserId) {
        List<Donation> donations = donationRepository
            .findBySenderIdAndStatus(donorUserId, DonationStatus.COMPLETED);
        
        return donations.stream()
            .map(this::toDonationResponseDto)
            .collect(Collectors.toList());
    }
    
    @Override
    public BigDecimal getEscrowBalance(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return walletRepository.findByUserAndStatus(user, WalletStatus.ESCROW)
            .map(Wallet::getBalance)
            .orElse(BigDecimal.ZERO);
    }
    
    private DonationResponseDto toDonationResponseDto(Donation donation) {
        return DonationResponseDto.builder()
            .id(donation.getId())
            .donationNumber(donation.getDonationNumber())
            .amount(donation.getAmount())
            .receiverUsername(donation.getReceiver().getUsername())
            .blogPostTitle(donation.getBlogPost() != null 
                          ? donation.getBlogPost().getTitle() 
                          : "Direct donation")
            .status(donation.getStatus())
            .createdAt(donation.getCreatedAt())
            .acceptedAt(donation.getAcceptedAt())
            .build();
    }
}
```

---

### Phase 2: Repository Layer

#### 2.1 Update DonationRepository

**File:** `payment/repository/DonationRepository.java`
```java
public interface DonationRepository extends JpaRepository<Donation, UUID> {
    
    // Existing methods...
    
    // ✨ NEW: Find donations by sender and status
    List<Donation> findBySenderIdAndStatus(UUID senderId, DonationStatus status);
    
    // ✨ NEW: Find donations by receiver and status
    List<Donation> findByReceiverIdAndStatus(UUID receiverId, DonationStatus status);
    
    // ✨ NEW: Find all completed donations (for admin)
    List<Donation> findByStatus(DonationStatus status);
    
    // ✨ NEW: Count pending acceptance for donor
    @Query("SELECT COUNT(d) FROM Donation d WHERE d.sender.id = :senderId " +
           "AND d.status = 'COMPLETED'")
    long countPendingAcceptanceForDonor(@Param("senderId") UUID senderId);
}
```

#### 2.2 Create LedgerEntryRepository

**File:** `payment/repository/LedgerEntryRepository.java`
```java
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
    
    // Find all ledger entries for a wallet
    List<LedgerEntry> findByFrom_walletOrTo_wallet(Wallet fromWallet, Wallet toWallet);
    
    // Find ledger entries by status
    List<LedgerEntry> findByStatus(LedgerEntityStatus status);
    
    // Get total transferred from a wallet
    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM LedgerEntry l " +
           "WHERE l.from_wallet.id = :walletId AND l.status = 'COMPLETED'")
    BigDecimal getTotalDebitForWallet(@Param("walletId") UUID walletId);
    
    // Get total received by a wallet
    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM LedgerEntry l " +
           "WHERE l.to_wallet.id = :walletId AND l.status = 'COMPLETED'")
    BigDecimal getTotalCreditForWallet(@Param("walletId") UUID walletId);
}
```

---

### Phase 3: Controller Layer

#### 3.1 Create EscrowController

**File:** `payment/restController/EscrowController.java`
```java
@RestController
@RequestMapping("/escrow")
@RequiredArgsConstructor
@Slf4j
public class EscrowController {

    private final EscrowService escrowService;
    private final JwtService jwtService;

    @PostMapping("/accept")
    @Operation(summary = "Accept donation and release funds from escrow")
    public ResponseEntity<AcceptDonationResponse> acceptDonation(
            @Valid @RequestBody AcceptDonationRequest request) {
        
        UUID donorUserId = jwtService.getCurrentUserIdFromJwtToken();
        log.info("Donor {} accepting donation {}", donorUserId, request.getDonationId());
        
        AcceptDonationResponse response = escrowService.acceptDonation(
            request.getDonationId(), 
            donorUserId
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/pending")
    @Operation(summary = "Get donations pending acceptance")
    public ResponseEntity<List<DonationResponseDto>> getPendingDonations() {
        UUID donorUserId = jwtService.getCurrentUserIdFromJwtToken();
        List<DonationResponseDto> donations = escrowService
            .getPendingAcceptance(donorUserId);
        return ResponseEntity.ok(donations);
    }
    
    @GetMapping("/balance")
    @Operation(summary = "Get escrow balance")
    public ResponseEntity<Map<String, BigDecimal>> getEscrowBalance() {
        UUID userId = jwtService.getCurrentUserIdFromJwtToken();
        BigDecimal balance = escrowService.getEscrowBalance(userId);
        return ResponseEntity.ok(Map.of("escrowBalance", balance));
    }
}
```

---

### Phase 4: Configuration & Initialization

#### 4.1 Create Platform User Initializer

**File:** `payment/config/PlatformUserInitializer.java`
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class PlatformUserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    
    private static final String PLATFORM_EMAIL = "platform@blogapp.com";
    private static final String PLATFORM_USERNAME = "platform";

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Check if platform user exists
        if (userRepository.findByEmail(PLATFORM_EMAIL).isPresent()) {
            log.info("Platform user already exists");
            return;
        }
        
        log.info("Creating platform system user...");
        
        // Create platform user
        User platformUser = User.builder()
            .username(PLATFORM_USERNAME)
            .email(PLATFORM_EMAIL)
            .password(passwordEncoder.encode(UUID.randomUUID().toString()))
            .role(Role.SYSTEM)
            .isActive(true)
            .build();
        platformUser = userRepository.save(platformUser);
        
        // Create platform revenue wallet
        Wallet revenueWallet = Wallet.builder()
            .user(platformUser)
            .balance(BigDecimal.ZERO)
            .status(WalletStatus.REVENUE)
            .build();
        walletRepository.save(revenueWallet);
        
        log.info("Platform user and revenue wallet created successfully");
    }
}
```

#### 4.2 Add SYSTEM Role

**File:** `model/Role.java`
```java
public enum Role {
    USER,
    ADMIN,
    SYSTEM  // ✨ NEW: For platform/system accounts
}
```

---

## Testing Strategy

### Unit Tests

**File:** `EscrowServiceImplTest.java`
```java
@SpringBootTest
@Transactional
class EscrowServiceImplTest {

    @Test
    void acceptDonation_Success() {
        // Given: Completed donation with funds in escrow
        // When: Donor accepts donation
        // Then: 90% to provider active wallet, 10% to platform
    }
    
    @Test
    void acceptDonation_NotSender_ThrowsException() {
        // Given: Completed donation
        // When: Non-sender tries to accept
        // Then: Throws exception
    }
    
    @Test
    void acceptDonation_AlreadyAccepted_ThrowsException() {
        // Given: Already accepted donation
        // When: Try to accept again
        // Then: Throws exception
    }
    
    @Test
    void acceptDonation_InsufficientEscrow_ThrowsException() {
        // Given: Escrow balance < donation amount
        // When: Try to accept
        // Then: Throws exception
    }
}
```

### Integration Tests

**File:** `EscrowControllerIntegrationTest.java`
```java
@SpringBootTest
@AutoConfigureMockMvc
class EscrowControllerIntegrationTest {

    @Test
    void acceptDonation_EndToEnd() {
        // 1. Create donation
        // 2. Complete payment (funds in escrow)
        // 3. Accept donation
        // 4. Verify balances updated
        // 5. Verify ledger entries created
    }
}
```

---

## API Documentation

### Accept Donation
```http
POST /escrow/accept
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "donationId": "uuid"
}

Response 200:
{
  "donationId": "uuid",
  "totalAmount": 100.00,
  "platformCommission": 10.00,
  "providerAmount": 90.00,
  "providerUsername": "author123",
  "acceptedAt": "2026-02-27T10:30:00",
  "message": "Donation accepted successfully"
}
```

### Get Pending Donations
```http
GET /escrow/pending
Authorization: Bearer {jwt_token}

Response 200:
[
  {
    "id": "uuid",
    "donationNumber": "DON-1234567890-ABCDE",
    "amount": 100.00,
    "receiverUsername": "author123",
    "blogPostTitle": "My Amazing Post",
    "status": "COMPLETED",
    "createdAt": "2026-02-27T10:00:00",
    "acceptedAt": null
  }
]
```

### Get Escrow Balance
```http
GET /escrow/balance
Authorization: Bearer {jwt_token}

Response 200:
{
  "escrowBalance": 250.00
}
```

---

## Security Considerations

1. **Authorization:** Only donor (sender) can accept their own donations
2. **Idempotency:** Prevent double-acceptance with `acceptedAt` check
3. **Atomic Transactions:** Use `@Transactional` to ensure all-or-nothing
4. **Balance Validation:** Check escrow balance before transfer
5. **Audit Trail:** LedgerEntry records all transfers
6. **Rate Limiting:** Prevent abuse of accept endpoint

---

## Monitoring & Logging

### Key Metrics to Track
- Total escrow balance
- Total platform revenue
- Average acceptance time
- Failed acceptance attempts
- Escrow balance discrepancies

### Log Events
```java
log.info("Donation accepted: donationId={}, amount={}, commission={}", ...);
log.warn("Insufficient escrow balance: required={}, available={}", ...);
log.error("Transfer failed: from={}, to={}, amount={}", ...);
```

---

## Rollout Plan

### Phase 1: Backend Implementation (Week 1)
- [ ] Add new enums and fields
- [ ] Create EscrowService and implementation
- [ ] Create DTOs
- [ ] Create repositories
- [ ] Create controller
- [ ] Create platform user initializer
- [ ] Write unit tests

### Phase 2: Testing (Week 1)
- [ ] Integration tests
- [ ] Manual testing with Postman
- [ ] Test edge cases
- [ ] Performance testing

### Phase 3: Frontend Implementation (Week 2)
- [ ] Add "Accept Donation" button
- [ ] Show pending donations list
- [ ] Display escrow balance
- [ ] Show acceptance confirmation

### Phase 4: Production Deployment (Week 2)
- [ ] Database migration
- [ ] Deploy backend
- [ ] Deploy frontend
- [ ] Monitor logs and metrics

---

## Next Steps

1. Review and approve this plan
2. Create database migration scripts
3. Implement backend (this plan)
4. Test thoroughly
5. Create frontend plan
6. Deploy to production

---

## Questions to Clarify

1. **Commission Rate:** Confirm 10% platform commission is correct
2. **Auto-Accept:** Should donations auto-accept after X days?
3. **Dispute Handling:** What happens if donor disputes instead of accepts?
4. **Refund Policy:** Can donations be refunded? If yes, from where?
5. **Withdrawal:** When can providers withdraw from active wallet?

