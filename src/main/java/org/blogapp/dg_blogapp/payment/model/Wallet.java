package org.blogapp.dg_blogapp.payment.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.blogapp.dg_blogapp.model.BaseEntity;
import org.blogapp.dg_blogapp.model.User;
import org.blogapp.dg_blogapp.payment.enums.WalletStatus;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;

@Entity
@Table(name = "wallet")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Wallet extends BaseEntity
{

    @ManyToOne()
    @JoinColumn(name = "user_id")
    private User user;

    @Digits(integer=10, fraction=2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    private WalletStatus status;

}
