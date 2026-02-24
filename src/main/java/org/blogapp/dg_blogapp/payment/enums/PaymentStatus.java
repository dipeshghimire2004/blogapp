package org.blogapp.dg_blogapp.payment.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    INITIATED,          // Payment initiated
    PENDING,            // Waiting for gateway response
    PROCESSING,         // Gateway processing
    COMPLETED,          // Payment successful
    FAILED,             // Payment failed
    REFUNDED,           // Payment refunded
    CANCELLED           // Payment cancelled
}
