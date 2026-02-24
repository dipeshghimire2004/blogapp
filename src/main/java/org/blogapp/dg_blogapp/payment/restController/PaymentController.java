package org.blogapp.dg_blogapp.payment.restController;

import lombok.RequiredArgsConstructor;
import org.blogapp.dg_blogapp.payment.dto.DonateDto;
import org.blogapp.dg_blogapp.payment.dto.DonateRequestDto;
import org.blogapp.dg_blogapp.payment.dto.FakePaymentDto;
import org.blogapp.dg_blogapp.payment.service.PaymentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;


    @PostMapping()
    public ResponseEntity<Map<String,UUID>> donateInitiate(@RequestBody DonateRequestDto request) {
            UUID donationId= paymentService.donateInitiate(request);
        return ResponseEntity.ok(Map.of("donationID ", donationId));
    }

    @PostMapping("/fake")
    public ResponseEntity<Void> fakePayment(@RequestBody FakePaymentDto request){
        paymentService.fakePayment(request);
        return ResponseEntity.ok().build();
    }

    //Mono eg.
    @GetMapping("/mono")
    public Mono<String> mono()
    {
        return Mono.just("mono babu");
    }

    //Flux (simulate streaming every seconds
    @GetMapping(value = "/flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> flux()
    {
        List<String> items = List.of("one", "two", "three", "four", "five");
        return Flux.fromIterable(items).delayElements(Duration.ofSeconds(1)).map(seq -> "\nStreaming data: " + seq).take(2);
    }
}
