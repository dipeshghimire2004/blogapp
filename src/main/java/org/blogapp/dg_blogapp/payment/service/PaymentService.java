package org.blogapp.dg_blogapp.payment.service;

import org.blogapp.dg_blogapp.payment.dto.AcceptDonationRequest;
import org.blogapp.dg_blogapp.payment.dto.AcceptDonationResponse;
import org.blogapp.dg_blogapp.payment.dto.DonateRequestDto;
import org.blogapp.dg_blogapp.payment.dto.DonateResponseDto;
import org.blogapp.dg_blogapp.payment.dto.InitiatePaymentRequest;
import org.blogapp.dg_blogapp.payment.dto.InitiatePaymentResponse;
import org.blogapp.dg_blogapp.payment.dto.PaymentVerifyResponse;
import org.blogapp.dg_blogapp.payment.dto.PaymentVerifyRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentService {

    UUID donateInitiate(DonateRequestDto request);

    InitiatePaymentResponse initiatePayment(InitiatePaymentRequest request);

    PaymentVerifyResponse verifyPayment(PaymentVerifyRequest request);

    AcceptDonationResponse acceptDonation(AcceptDonationRequest request);

    List<DonateResponseDto> getPendingAcceptance();

    BigDecimal getEscrowBalance();

}
