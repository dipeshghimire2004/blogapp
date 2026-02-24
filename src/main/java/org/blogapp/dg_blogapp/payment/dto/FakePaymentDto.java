package org.blogapp.dg_blogapp.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.blogapp.dg_blogapp.payment.enums.DonationStatus;
import org.blogapp.dg_blogapp.payment.enums.GatewayType;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class FakePaymentDto {

    private UUID donationId;

    private GatewayType gateway;

    private DonationStatus donationStatus;
}
