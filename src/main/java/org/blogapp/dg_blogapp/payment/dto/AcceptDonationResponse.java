package org.blogapp.dg_blogapp.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcceptDonationResponse {

    private UUID donationId;
    private BigDecimal totalAmount;
    private BigDecimal platformCommission;
    private BigDecimal providerAmount;
    private String providerName;
//    private String
}
