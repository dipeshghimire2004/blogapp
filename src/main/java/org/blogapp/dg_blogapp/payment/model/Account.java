package org.blogapp.dg_blogapp.payment.model;


import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.blogapp.dg_blogapp.model.BaseEntity;
import org.blogapp.dg_blogapp.payment.enums.AccountStatus;
import org.blogapp.dg_blogapp.payment.enums.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Account extends BaseEntity {

    @Digits(integer = 6, fraction = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    private UUID ownerId;    // polymorphic: userId, orderId, platformId

    private String ownerType;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

}
