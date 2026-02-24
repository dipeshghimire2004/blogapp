package org.blogapp.dg_blogapp.payment.enums;

import lombok.Getter;

@Getter
public enum WalletStatus {
    ESCROW,
    WRITER_WALLET,
    PLATFORM_REVENUE,
    DISPUTE_HOLD,
    WITHDRAWAL_PROCESSING,

}
