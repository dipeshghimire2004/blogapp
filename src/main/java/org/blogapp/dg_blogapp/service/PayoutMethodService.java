package org.blogapp.dg_blogapp.service;

import lombok.RequiredArgsConstructor;
import org.blogapp.dg_blogapp.dto.PayoutMethodRequestDto;
import org.blogapp.dg_blogapp.exception.ResourceNotFoundException;
import org.blogapp.dg_blogapp.mapper.PayoutMapper;
import org.blogapp.dg_blogapp.model.User;
import org.blogapp.dg_blogapp.payment.model.PayoutMethod;
import org.blogapp.dg_blogapp.repository.PayoutMethodRepository;
import org.blogapp.dg_blogapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayoutMethodService {

    private final PayoutMethodRepository payoutMethodRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PayoutMapper payoutMapper;

    public void addPayoutMethod(PayoutMethodRequestDto requestDto) {

        UUID userId = jwtService.getCurrentUserIdFromJwtToken();
        User writer = userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User not found"));

        PayoutMethod payoutMethod = payoutMapper.toEntity(requestDto);
        payoutMethod.setWriter(writer);
        payoutMethodRepository.save(payoutMethod);
    }

    public List<PayoutMethodRequestDto> listPayoutMethods() {
        List<PayoutMethod> payoutMethods = payoutMethodRepository.findAll();
        return payoutMethods.stream().map(payoutMapper:: toDto).toList();
    }

    public List<PayoutMethodRequestDto> listByUserId(UUID userId)
    {
        List<PayoutMethod> payoutMethods = payoutMethodRepository.findByUserId(userId);
        return payoutMethods.stream().map(payoutMapper:: toDto).toList();
    }

    @Transactional
    public void updatePayoutMethod(PayoutMethodRequestDto requestDto, UUID id)
    {
        UUID userId = jwtService.getCurrentUserIdFromJwtToken();
        User writer = userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User not found"));

        PayoutMethod payoutMethod = payoutMethodRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Payout method not found"));

        if(!payoutMethod.getWriter().getId().equals(writer.getId())){
            throw new ResourceNotFoundException("Unauthorized to update this payout method");
        }

        // Update existing entity fields
        payoutMethod.setAccountName(requestDto.accountName());
        payoutMethod.setAccountNumber(requestDto.accountNumber());
        payoutMethod.setBankName(requestDto.bankName());
        payoutMethod.setGateway(requestDto.gateway());
        
        payoutMethodRepository.save(payoutMethod);
    }


}
