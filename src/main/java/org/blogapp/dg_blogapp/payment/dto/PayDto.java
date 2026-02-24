package org.blogapp.dg_blogapp.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PayDto
{

    private UUID donationId;

    private UUID senderId;

    private UUID receiverId;


}
