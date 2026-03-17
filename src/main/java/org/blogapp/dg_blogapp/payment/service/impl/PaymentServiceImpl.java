package org.blogapp.dg_blogapp.payment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.blogapp.dg_blogapp.exception.ResourceNotFoundException;
import org.blogapp.dg_blogapp.model.BlogPost;
import org.blogapp.dg_blogapp.model.User;
import org.blogapp.dg_blogapp.payment.config.KhaltiConfig;
import org.blogapp.dg_blogapp.payment.dto.AcceptDonationRequest;
import org.blogapp.dg_blogapp.payment.dto.AcceptDonationResponse;
import org.blogapp.dg_blogapp.payment.dto.DonateRequestDto;
import org.blogapp.dg_blogapp.payment.dto.DonateResponseDto;
import org.blogapp.dg_blogapp.payment.dto.InitiatePaymentRequest;
import org.blogapp.dg_blogapp.payment.dto.InitiatePaymentResponse;
import org.blogapp.dg_blogapp.payment.dto.PaymentVerifyResponse;
import org.blogapp.dg_blogapp.payment.dto.PaymentVerifyRequest;
import org.blogapp.dg_blogapp.payment.enums.DonationStatus;
import org.blogapp.dg_blogapp.payment.enums.LedgerEntityStatus;
import org.blogapp.dg_blogapp.payment.enums.PaymentStatus;
import org.blogapp.dg_blogapp.payment.enums.TransactionType;
import org.blogapp.dg_blogapp.payment.enums.WalletStatus;
import org.blogapp.dg_blogapp.payment.model.Donation;
import org.blogapp.dg_blogapp.payment.model.LedgerEntry;
import org.blogapp.dg_blogapp.payment.model.PaymentTransaction;
import org.blogapp.dg_blogapp.payment.model.Wallet;
import org.blogapp.dg_blogapp.payment.repository.DonationRepository;
import org.blogapp.dg_blogapp.payment.repository.LedgerEntryRepository;
import org.blogapp.dg_blogapp.payment.repository.PaymentRepository;
import org.blogapp.dg_blogapp.payment.repository.WalletRepository;
import org.blogapp.dg_blogapp.payment.service.PaymentService;
import org.blogapp.dg_blogapp.repository.PostRepository;
import org.blogapp.dg_blogapp.repository.UserRepository;
import org.blogapp.dg_blogapp.service.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.random;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final DonationRepository donationRepository;
    private final PaymentRepository paymentRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final KhaltiConfig khaltiConfig;
    private final WebClient khaltiWebClient;
    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;


    private static final String PLATFORM_USER_EMAIL = "platform@blogapp.com";

    @Override
    @Transactional
    public UUID donateInitiate(DonateRequestDto request) {
        UUID userId =jwtService.getCurrentUserIdFromJwtToken();

        log.info("Sender userId: {}", userId);
        Donation donation = new Donation();

        donation.setAmount(request.getAmount());
        log.info("Amount: {}", request.getAmount());
        donation.setSender(getuserById(userId));
        
        // Derive receiver from post if not provided
        UUID receiverId = request.getReceiverId();
        if (receiverId == null && request.getPostId() != null) {
            BlogPost post = getBlogpostById(request.getPostId());
            receiverId = post.getUser().getId();
            log.info("Derived receiver from post author: {}", receiverId);
        }
        
        if (receiverId == null) {
            throw new RuntimeException("Receiver ID must be provided or post must exist");
        }
        
        donation.setReceiver(getuserById(receiverId));
        log.info("Receiver receiverId: {}", receiverId);
        donation.setStatus(DonationStatus.INITIATED);
        donation.setDonationNumber("DON-" + System.currentTimeMillis() + "-" + random(5));
        if (request.getPostId() != null) {
            donation.setBlogPost(getBlogpostById(request.getPostId()));
        }
        log.info("Post postId: {}", request.getPostId());
        donationRepository.save(donation);
        log.info("Donation id {}", donation.getId());
        return donation.getId();
    }




    @Override
    @Transactional
    public InitiatePaymentResponse initiatePayment(InitiatePaymentRequest request){
        Donation donation = getDonationById(request.getDonationId());
        UUID userId = jwtService.getCurrentUserIdFromJwtToken();
        log.info("Sender userId: {}", userId);
        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setUser(getuserById(userId));
        paymentTransaction.setDonation(donation);
        paymentTransaction.setAmount(donation.getAmount());
        paymentTransaction.setDonationStatus(DonationStatus.INITIATED);
        paymentTransaction.setTransactionType(TransactionType.DEBIT);
        paymentRepository.save(paymentTransaction);

        // 4️⃣ Build Khalti payload (Backend controls everything)
        Map<String, Object> payload = new HashMap<>();
        payload.put("return_url", khaltiConfig.getReturnUrl());
        payload.put("website_url", khaltiConfig.getWebsiteUrl());
        payload.put("amount", donation.getAmount().multiply(BigDecimal.valueOf(100))); // convert to paisa
        payload.put("purchase_order_id", donation.getId().toString());
        String orderName = donation.getBlogPost() != null
                ? donation.getBlogPost().getTitle()
                : "Donation to " + donation.getReceiver().getUsername();
        payload.put("purchase_order_name", orderName);
//        payload.put("purchase_order_name", donation.getBlogPost().getTitle());

        // 5️⃣ Call Khalti Initiate API
        Map<String, Object> khaltiResponse = khaltiWebClient.post()
                .uri(khaltiConfig.getInitiatePath())
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        // 6️⃣ Extract response
        String pidx = (String) khaltiResponse.get("pidx");
        String paymentUrl = (String) khaltiResponse.get("payment_url");

        // 7️⃣ Update transaction with pidx
        paymentTransaction.setPidx(pidx);
        paymentTransaction.setPaymentStatus(PaymentStatus.PENDING);
        paymentRepository.save(paymentTransaction);

        // 8️⃣ Return response DTO to frontend
        InitiatePaymentResponse response = new InitiatePaymentResponse();
        response.setPidx(pidx);
        response.setPaymentUrl(paymentUrl);

        return response;
    }

    @Override
    @Transactional
    public PaymentVerifyResponse verifyPayment(PaymentVerifyRequest request) {
        PaymentTransaction transaction = paymentRepository.findByPidx(request.getPidx())
                .orElseThrow(() -> new RuntimeException("Transaction with pidx not found"));

        if (PaymentStatus.COMPLETED.equals(transaction.getPaymentStatus())) {
            return new PaymentVerifyResponse(PaymentStatus.COMPLETED, "Payment already verified");
        }

        // Call khalti lookup
        Map<String, Object> payload = Map.of("pidx", request.getPidx());

        Map<String, Object> khaltiResponse = khaltiWebClient.post()
                .uri(khaltiConfig.getVerifyPath())
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        String khaltiStatus = (String) khaltiResponse.get("status");
        log.info("Khalti payment status: {}", khaltiStatus);

        // Get related donation
        Donation donation = transaction.getDonation();

        // Validate amount
        Integer totalAmount = (Integer) khaltiResponse.get("total_amount");
        BigDecimal khaltiAmount = BigDecimal.valueOf(totalAmount)
                .divide(BigDecimal.valueOf(100));

        if (khaltiAmount.compareTo(transaction.getAmount()) != 0) {
            transaction.setPaymentStatus(PaymentStatus.FAILED);
            transaction.setDonationStatus(DonationStatus.FAILED);
            donation.setStatus(DonationStatus.FAILED);
            paymentRepository.save(transaction);
            donationRepository.save(donation);
            throw new RuntimeException("Amount mismatch detected");
        }

        // If payment successful (check Khalti status)
        if ("Completed".equalsIgnoreCase(khaltiStatus)) {
            transaction.setPaymentStatus(PaymentStatus.COMPLETED);
            transaction.setDonationStatus(DonationStatus.COMPLETED);
            donation.setStatus(DonationStatus.COMPLETED);

            paymentRepository.save(transaction);
            donationRepository.save(donation);

            // Credit escrow wallet (optional - create if doesn't exist)
            try {
                Wallet escrowWallet = walletRepository.findByUserAndStatus(donation.getReceiver(), WalletStatus.PLATFORM_ESCROW)
                        .orElseGet(() -> {
                            log.info("Creating new escrow wallet for user: {}", donation.getReceiver().getId());
                            Wallet newWallet = new Wallet();
                            newWallet.setUser(donation.getReceiver());
                            newWallet.setStatus(WalletStatus.PLATFORM_ESCROW);
                            newWallet.setBalance(BigDecimal.ZERO);
                            return walletRepository.save(newWallet);
                        });

                escrowWallet.setBalance(escrowWallet.getBalance().add(khaltiAmount));
                walletRepository.save(escrowWallet);
                log.info("Credited {} to escrow wallet. New balance: {}", khaltiAmount, escrowWallet.getBalance());
            } catch (Exception e) {
                log.error("Failed to credit wallet, but payment is completed: {}", e.getMessage());
                // Payment is still successful even if wallet credit fails
            }
            
            return new PaymentVerifyResponse(PaymentStatus.COMPLETED, "Payment successful");
        }

        // If payment is still pending
        if ("Pending".equalsIgnoreCase(khaltiStatus)) {
            return new PaymentVerifyResponse(PaymentStatus.PENDING, "Payment is still pending");
        }

        // If failed
        transaction.setPaymentStatus(PaymentStatus.FAILED);
        transaction.setDonationStatus(DonationStatus.FAILED);
        donation.setStatus(DonationStatus.FAILED);
        paymentRepository.save(transaction);
        donationRepository.save(donation);
        
        return new PaymentVerifyResponse(PaymentStatus.FAILED, "Payment failed");
    }


    public AcceptDonationResponse acceptDonation(AcceptDonationRequest request){
        UUID donorUserId = jwtService.getCurrentUserIdFromJwtToken();
        log.info("Processing donation acceptance: donationId={}, donorUserId={}",
                request.getDonationId(), donorUserId);

        Donation donation = donationRepository.findById(request.getDonationId()).orElseThrow(()-> new RuntimeException("Donation not found"));

        //validate sender id
        if(!donation.getSender().getId().equals(donorUserId)){
            throw new RuntimeException("Only the donor can accept this donation");
        }

        //validate donation status
        if(donation.getStatus() != DonationStatus.COMPLETED){
            throw new RuntimeException("Donation must be in completed state");
        }

        // 4. Check if already accepted
        if (donation.getAcceptedAt() != null) {
            throw new RuntimeException("Donation already accepted at: "
                    + donation.getAcceptedAt());
        }

        BigDecimal totalAmount = donation.getAmount();
        BigDecimal platformCommission = totalAmount.multiply(BigDecimal.valueOf(10)).divide(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal providerAmount = totalAmount.subtract(platformCommission);

        //Get wallet
        User reciever = donation.getReceiver();
        Wallet escrowWallet = getOrCreateWallet(reciever,WalletStatus.PLATFORM_ESCROW);
        Wallet activeWallet = getOrCreateWallet(reciever, WalletStatus.WRITER_WALLET);
        Wallet platformWallet = getPlatformWallet();

        // Validate escrow  has sufficient balance
        if(escrowWallet.getBalance().compareTo(totalAmount) < 0){
            throw new RuntimeException("Insufficient escrow balance. Required: "
                    + totalAmount + ", Available: "
                    + escrowWallet.getBalance());
        }

        //execute transfers (atomic)
        transferFunds(escrowWallet, activeWallet, providerAmount,"Donation acceptance - Provider share", donation);
        transferFunds(escrowWallet, platformWallet, platformCommission,"Donation acceptance - platform commission", donation);

        //update donation
        donation.setStatus(DonationStatus.COMPLETED);
        donation.setAcceptedAt(LocalDateTime.now());
        donation.setCommission(platformCommission);
        donation.setProviderAmount(providerAmount);
        donationRepository.save(donation);

        log.info("Donation accepted successfully");

        return AcceptDonationResponse.builder()
                .donationId(donation.getId())
                .totalAmount(totalAmount)
                .platformCommission(platformCommission)
                .providerAmount(providerAmount)
                .providerName(reciever.getUsername())
                .build();
    }


    private Wallet getOrCreateWallet(User user, WalletStatus status){
        return walletRepository.findByUserAndStatus(user, status)
                .orElseGet(()->{
                    log.info("Creating {} wallet for user: {}", status, user);
                    Wallet newWallet = new Wallet();
                    newWallet.setUser(user);
                    newWallet.setBalance(BigDecimal.ZERO);
                    newWallet.setStatus(status);
                    return walletRepository.save(newWallet);
                });
    }

    private void transferFunds(Wallet fromWallet, Wallet toWallet, BigDecimal amount,String note, Donation donation) {
        //Deduct the source
        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        walletRepository.save(fromWallet);

        //credit to destination
        toWallet.setBalance(toWallet.getBalance().add(amount));
        walletRepository.save(toWallet);

        //create ledger entry
        LedgerEntry ledgerEntry = LedgerEntry.builder()
                .from_wallet(fromWallet)
                .to_wallet(toWallet)
                .amount(amount)
                .status(LedgerEntityStatus.RELEASE)
                .note(note + " - Donation: " + donation.getDonationNumber())
                .build();
        ledgerEntryRepository.save(ledgerEntry);
        log.info("Transfer completed: {} -> {}, Amount: {}", fromWallet.getId(), toWallet.getId(), amount);


    }

    @Transactional(readOnly = true)
    public List<DonateResponseDto> getPendingAcceptance(){
        UUID donorUserId = jwtService.getCurrentUserIdFromJwtToken();
        log.info("Fetching pending donations for donor: {}", donorUserId);
        List<Donation> donations = donationRepository.findBySenderIdAndStatus(donorUserId,DonationStatus.COMPLETED);
        return donations.stream().map(this::toDonationResponseDto).toList();
    }

    @Override
    public BigDecimal getEscrowBalance() {
        UUID userId = jwtService.getCurrentUserIdFromJwtToken();
        log.info("Fetching escrow balance for user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return walletRepository.findByUserAndStatus(user, WalletStatus.PLATFORM_ESCROW)
                .map(Wallet::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    private Wallet getPlatformWallet() {
        User platformWallet = userRepository.findByEmail(PLATFORM_USER_EMAIL)
                .orElseThrow(()-> new RuntimeException("Platform user not found"));
        return getOrCreateWallet(platformWallet, WalletStatus.PLATFORM_REVENUE);
    }

    private Donation getDonationById(UUID donationId)
    {
        return donationRepository.findById(donationId).orElseThrow(()->new RuntimeException("Donation not found"));
    }
    private User getuserById(UUID id){
        return userRepository.findById(id).orElseThrow(()-> new RuntimeException("User not found."));
    }

    private BlogPost getBlogpostById(UUID id) {
        return postRepository.findById(id).orElseThrow(()->new RuntimeException("blog post not found"));
    }

    private Donation getDonateById(UUID id)
    {
        return donationRepository.findById(id).orElseThrow(()->new RuntimeException("donate not found."));
    }

    private DonateResponseDto toDonationResponseDto(Donation donation) {
        return DonateResponseDto.builder()
                .id(donation.getId())
                .donationNumber(donation.getDonationNumber())
                .amount(donation.getAmount())
                .receiverUsername(donation.getReceiver().getUsername())
                .postTitle(donation.getBlogPost() != null
                        ? donation.getBlogPost().getTitle()
                        : "Direct donation")
                .status(donation.getStatus())
                .acceptedAt(donation.getAcceptedAt())
                .build();
    }
}
