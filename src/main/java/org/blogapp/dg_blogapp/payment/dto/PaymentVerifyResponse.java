package org.blogapp.dg_blogapp.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.blogapp.dg_blogapp.payment.enums.PaymentStatus;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentVerifyResponse {

//    private String orderId;
    private PaymentStatus status;
//    private BigDecimal amount;
    private String message;

}
