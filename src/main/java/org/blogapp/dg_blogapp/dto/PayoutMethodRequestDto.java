package org.blogapp.dg_blogapp.dto;


import org.blogapp.dg_blogapp.payment.enums.GatewayType;

import java.util.UUID;

public record PayoutMethodRequestDto(
        UUID id,
        GatewayType gateway,
        String accountName,
        String accountNumber,
        String bankName,
        boolean isVerified
) {

}
//@ManyToOne
//@JoinColumn(name = "writer_id")
//private User writer;
//
//@Enumerated(EnumType.STRING)
//private GatewayType gateway;
//
//@NotNull(message = "account Name is needed")
//private String accountName;
//
//@NotNull(message = "account Number is needed.")
//private String accountNumber;
//
//@NotNull(message = "Bank name is needed")
//private String bankName;
//
//@Builder.Default
//private boolean isVerified = false;