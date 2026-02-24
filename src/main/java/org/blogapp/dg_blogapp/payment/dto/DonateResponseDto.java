package org.blogapp.dg_blogapp.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.blogapp.dg_blogapp.payment.enums.DonationStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DonateResponseDto
{

    private UUID id;

    private UUID receiverId;

    private UUID senderId;

    private UUID postId;

    private BigDecimal amount;

    private DonationStatus status;

    private String postTitle;
}
