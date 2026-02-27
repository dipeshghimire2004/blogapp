package org.blogapp.dg_blogapp.payment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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
import org.blogapp.dg_blogapp.payment.enums.PaymentStatus;
import org.blogapp.dg_blogapp.payment.enums.TransactionType;

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

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private GatewayType gateway=GatewayType.KHALTI;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private String pidx;   //payment Id

//    @Type(type = "jsonb")               // <-- Hibernate JSONB type
//    @Column(columnDefinition = "jsonb")
//    private Map<String, Object> rawResponse;

    @Lob
    @Column(columnDefinition = "TEXT")   // store raw JSON safely
    private String rawResponse;


    @Enumerated(EnumType.STRING)
    private DonationStatus donationStatus;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
}
