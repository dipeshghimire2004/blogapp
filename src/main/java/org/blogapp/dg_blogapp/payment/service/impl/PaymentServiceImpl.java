package org.blogapp.dg_blogapp.payment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.blogapp.dg_blogapp.model.BlogPost;
import org.blogapp.dg_blogapp.model.User;
import org.blogapp.dg_blogapp.payment.config.KhaltiConfig;
import org.blogapp.dg_blogapp.payment.dto.DonateRequestDto;
import org.blogapp.dg_blogapp.payment.dto.InitiatePaymentRequest;
import org.blogapp.dg_blogapp.payment.dto.InitiatePaymentResponse;
import org.blogapp.dg_blogapp.payment.dto.PaymentVerifyResponse;
import org.blogapp.dg_blogapp.payment.dto.PaymentVerifyRequest;
import org.blogapp.dg_blogapp.payment.enums.DonationStatus;
import org.blogapp.dg_blogapp.payment.enums.PaymentStatus;
import org.blogapp.dg_blogapp.payment.enums.TransactionType;
import org.blogapp.dg_blogapp.payment.enums.WalletStatus;
import org.blogapp.dg_blogapp.payment.model.Donation;
import org.blogapp.dg_blogapp.payment.model.PaymentTransaction;
import org.blogapp.dg_blogapp.payment.model.Wallet;
import org.blogapp.dg_blogapp.payment.repository.DonationRepository;
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
import java.util.HashMap;
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
                Wallet escrowWallet = walletRepository.findByUserAndStatus(donation.getReceiver(), WalletStatus.ESCROW)
                        .orElseGet(() -> {
                            log.info("Creating new escrow wallet for user: {}", donation.getReceiver().getId());
                            Wallet newWallet = new Wallet();
                            newWallet.setUser(donation.getReceiver());
                            newWallet.setStatus(WalletStatus.ESCROW);
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
}
