package org.blogapp.dg_blogapp.payment.service;

import org.blogapp.dg_blogapp.payment.dto.DonateDto;
import org.blogapp.dg_blogapp.payment.dto.DonateRequestDto;
import org.blogapp.dg_blogapp.payment.dto.FakePaymentDto;
import org.blogapp.dg_blogapp.payment.dto.InitiatePaymentRequest;
import org.blogapp.dg_blogapp.payment.dto.InitiatePaymentResponse;
import org.blogapp.dg_blogapp.payment.dto.PaymentVerifyResponse;
import org.blogapp.dg_blogapp.payment.dto.PaymentiVerifyRequest;

import java.util.UUID;

public interface PaymentService {

    UUID donateInitiate(DonateRequestDto request);

    InitiatePaymentResponse initiatePayment(InitiatePaymentRequest request);

//    PaymentVerifyResponse verifyPayment(PaymentiVerifyRequest request);

    void fakePayment(FakePaymentDto request);
}
