package org.blogapp.dg_blogapp.payment.service;

import org.blogapp.dg_blogapp.payment.dto.DonateDto;
import org.blogapp.dg_blogapp.payment.dto.DonateRequestDto;
import org.blogapp.dg_blogapp.payment.dto.FakePaymentDto;

import java.util.UUID;

public interface PaymentService {

    UUID donateInitiate(DonateRequestDto request);

    void fakePayment(FakePaymentDto request);
}
