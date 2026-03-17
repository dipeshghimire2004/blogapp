package org.blogapp.dg_blogapp.payment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.blogapp.dg_blogapp.model.BaseEntity;
import org.blogapp.dg_blogapp.model.BlogPost;
import org.blogapp.dg_blogapp.model.User;
import org.blogapp.dg_blogapp.payment.enums.DonationStatus;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "donation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Donation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_post_id")
    private BlogPost blogPost;

    @Positive
    @Digits(integer = 10, fraction = 2)
    private BigDecimal amount;

    @Positive
    @Digits(integer = 10, fraction = 2)
    private BigDecimal commission;

    @Positive
    @Digits(integer = 10, fraction = 2)
    private BigDecimal providerAmount;

    @Enumerated(EnumType.STRING)
    private DonationStatus status;

    private String donationNumber;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
}
