package org.blogapp.dg_blogapp.payment.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.blogapp.dg_blogapp.model.BaseEntity;
import org.blogapp.dg_blogapp.model.User;
import org.blogapp.dg_blogapp.payment.enums.DonationStatus;
import org.blogapp.dg_blogapp.payment.enums.GatewayType;
import java.math.BigDecimal;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentTransaction extends BaseEntity {

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name="donate_id")
    private Donation donation;

    @Digits(integer=10, fraction=2)
    private BigDecimal amount;

    @Builder.Default
    private String currency="NPR";

    @Enumerated(EnumType.STRING)
    private GatewayType gateway;

    private String providerReference;   //payment Id

    @Enumerated(EnumType.STRING)
    private DonationStatus donationStatus;

}
