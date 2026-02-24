package org.blogapp.dg_blogapp.payment.model;

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
import org.blogapp.dg_blogapp.payment.enums.LedgerEntityStatus;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;

@Entity
@Table(name = "ledger_entity")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class LedgerEntry extends BaseEntity
{
    @ManyToOne
    @JoinColumn(name = "sender_wallet_id")
    private Wallet from_wallet;

    @ManyToOne
    @JoinColumn(name= "receiver_wallet_id")
    private Wallet to_wallet;

    @Digits(integer = 10, fraction = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private LedgerEntityStatus status;

    private String note;

}
