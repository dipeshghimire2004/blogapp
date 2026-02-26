package org.blogapp.dg_blogapp.payment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.blogapp.dg_blogapp.model.BlogPost;
import org.blogapp.dg_blogapp.model.User;
import org.blogapp.dg_blogapp.payment.config.KhaltiConfig;
import org.blogapp.dg_blogapp.payment.dto.DonateRequestDto;
import org.blogapp.dg_blogapp.payment.dto.FakePaymentDto;
import org.blogapp.dg_blogapp.payment.dto.InitiatePaymentRequest;
import org.blogapp.dg_blogapp.payment.dto.InitiatePaymentResponse;
import org.blogapp.dg_blogapp.payment.enums.DonationStatus;
import org.blogapp.dg_blogapp.payment.enums.PaymentStatus;
import org.blogapp.dg_blogapp.payment.enums.TransactionType;
import org.blogapp.dg_blogapp.payment.model.Donation;
import org.blogapp.dg_blogapp.payment.model.PaymentTransaction;
import org.blogapp.dg_blogapp.payment.repository.DonationRepository;
import org.blogapp.dg_blogapp.payment.repository.PaymentRepository;
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
public class PaymentServiceImpl implements PaymentService
{

    private final DonationRepository donationRepository;
    private final PaymentRepository paymentRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final KhaltiConfig khaltiConfig;
    private final WebClient khaltiWebClient;

    @Override
    @Transactional
    public UUID donateInitiate(DonateRequestDto request) {
        UUID userId =jwtService.getCurrentUserIdFromJwtToken();

        log.info("Sender userId: {}", userId);
        Donation donation = new Donation();

        donation.setAmount(request.getAmount());
        log.info("Amount: {}", request.getAmount());
        donation.setSender(getuserById(userId));
        donation.setReceiver(getuserById(request.getReceiverId()));
        log.info("Receiver receiverId: {}", request.getReceiverId());
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


    public void fakePayment(FakePaymentDto request){
        UUID userId = jwtService.getCurrentUserIdFromJwtToken();

        Donation donation = getDonateById(request.getDonationId());
        log.info("Sender userId: {}", userId);
        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setUser(getuserById(userId));
        paymentTransaction.setDonation(donation);
        paymentTransaction.setGateway(request.getGateway());
        paymentTransaction.setPidx(UUID.randomUUID().toString());
        paymentTransaction.setDonationStatus(DonationStatus.COMPLETED);
        paymentRepository.save(paymentTransaction);

        donation.setStatus(DonationStatus.COMPLETED);
        donationRepository.save(donation);
    }


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
        payload.put("purchase_order_name", donation.getBlogPost().getTitle());

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
