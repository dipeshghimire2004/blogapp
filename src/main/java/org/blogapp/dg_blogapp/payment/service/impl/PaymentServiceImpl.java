package org.blogapp.dg_blogapp.payment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.blogapp.dg_blogapp.model.BlogPost;
import org.blogapp.dg_blogapp.model.User;
import org.blogapp.dg_blogapp.payment.dto.DonateRequestDto;
import org.blogapp.dg_blogapp.payment.dto.FakePaymentDto;
import org.blogapp.dg_blogapp.payment.enums.DonationStatus;
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

import java.util.UUID;

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
        donation.setDonationNumber(UUID.randomUUID().toString());
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
        paymentTransaction.setProviderReference(UUID.randomUUID().toString());
        paymentTransaction.setDonationStatus(DonationStatus.COMPLETED);
        paymentRepository.save(paymentTransaction);

        donation.setStatus(DonationStatus.COMPLETED);
        donationRepository.save(donation);
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
