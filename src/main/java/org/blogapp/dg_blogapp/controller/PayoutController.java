package org.blogapp.dg_blogapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.blogapp.dg_blogapp.dto.PayoutMethodRequestDto;
import org.blogapp.dg_blogapp.service.PayoutMethodService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("payout")
@RequiredArgsConstructor
@Slf4j
public class PayoutController {

    private final PayoutMethodService service;

    @PostMapping
    public ResponseEntity<Void> payout(@RequestBody @Valid PayoutMethodRequestDto requestDto) {
        service.addPayoutMethod(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<PayoutMethodRequestDto>> payoutMethodList(){
       List<PayoutMethodRequestDto> list= service.listPayoutMethods();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<PayoutMethodRequestDto>> getPayoutMethod(@PathVariable UUID userId)
    {
        List<PayoutMethodRequestDto> userPayouts = service.listByUserId(userId);
        return ResponseEntity.ok(userPayouts);
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<Void> updatePayout(@PathVariable UUID id, @RequestBody @Valid PayoutMethodRequestDto requestDto)
    {
        service.updatePayoutMethod(requestDto, id);
        return ResponseEntity.ok().build();
    }
}
