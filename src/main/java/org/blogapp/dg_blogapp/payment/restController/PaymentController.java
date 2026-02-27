package org.blogapp.dg_blogapp.payment.restController;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.blogapp.dg_blogapp.payment.dto.DonateRequestDto;
import org.blogapp.dg_blogapp.payment.dto.InitiatePaymentRequest;
import org.blogapp.dg_blogapp.payment.dto.InitiatePaymentResponse;
import org.blogapp.dg_blogapp.payment.dto.PaymentVerifyResponse;
import org.blogapp.dg_blogapp.payment.dto.PaymentVerifyRequest;
import org.blogapp.dg_blogapp.payment.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<Map<String, UUID>> donateInitiate(@RequestBody DonateRequestDto request) {
        UUID donationId = paymentService.donateInitiate(request);
        return ResponseEntity.ok(Map.of("donationId", donationId));
    }

    @PostMapping("/khalti/initiate")
    public ResponseEntity<InitiatePaymentResponse> initiatePayment(@RequestBody InitiatePaymentRequest request) {
        InitiatePaymentResponse response = paymentService.initiatePayment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> handleKhaltiCallback(@RequestParam("pidx") String pidx, HttpServletResponse response) throws IOException {
        PaymentVerifyRequest request = new PaymentVerifyRequest();
        request.setPidx(pidx);
        PaymentVerifyResponse verifyResponse = paymentService.verifyPayment(request);
        
        // Redirect to frontend with status
        String frontendUrl = "http://localhost:3000/payment/callback";
        String redirectUrl = frontendUrl + "?pidx=" + pidx + "&status=" + verifyResponse.getStatus();
        
        response.sendRedirect(redirectUrl);
        return ResponseEntity.status(HttpStatus.FOUND).build();
    }

    @PostMapping("/khalti/verify")
    public ResponseEntity<PaymentVerifyResponse> verifyPayment(@RequestBody PaymentVerifyRequest request) {
        PaymentVerifyResponse response=  paymentService.verifyPayment(request);
        return ResponseEntity.ok(response);
    }
}
