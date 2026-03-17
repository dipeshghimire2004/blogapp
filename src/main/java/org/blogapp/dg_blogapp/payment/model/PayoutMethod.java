package org.blogapp.dg_blogapp.payment.model;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.blogapp.dg_blogapp.model.BaseEntity;
import org.blogapp.dg_blogapp.model.User;
import org.blogapp.dg_blogapp.payment.converter.AccountNumberConverter;
import org.blogapp.dg_blogapp.payment.enums.GatewayType;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PayoutMethod extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "writer_id")
    private User writer;

    @Enumerated(EnumType.STRING)
    private GatewayType gateway;

    @NotNull(message = "account Name is needed")
    private String accountName;

    @NotNull(message = "account Number is needed.")
    @Convert(converter = AccountNumberConverter.class)
    private String accountNumber;

    @NotNull(message = "Bank name is needed")
    private String bankName;

    @Builder.Default
    private boolean isVerified = false;

}
