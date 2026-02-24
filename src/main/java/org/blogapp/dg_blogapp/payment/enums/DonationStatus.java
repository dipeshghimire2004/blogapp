package org.blogapp.dg_blogapp.payment.enums;

import lombok.Getter;

@Getter
public enum DonationStatus {
    INITIATED,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    REFUNDED,
    DISPUTED
}
