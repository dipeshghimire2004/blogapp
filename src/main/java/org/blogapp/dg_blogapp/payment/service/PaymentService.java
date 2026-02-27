package org.blogapp.dg_blogapp.payment.service;

import org.blogapp.dg_blogapp.payment.dto.DonateRequestDto;
import org.blogapp.dg_blogapp.payment.dto.InitiatePaymentRequest;
import org.blogapp.dg_blogapp.payment.dto.InitiatePaymentResponse;
import org.blogapp.dg_blogapp.payment.dto.PaymentVerifyResponse;
import org.blogapp.dg_blogapp.payment.dto.PaymentVerifyRequest;

import java.util.UUID;

public interface PaymentService {

    UUID donateInitiate(DonateRequestDto request);

    InitiatePaymentResponse initiatePayment(InitiatePaymentRequest request);

    PaymentVerifyResponse verifyPayment(PaymentVerifyRequest request);

}
