package org.blogapp.dg_blogapp.payment.enums;

public enum LedgerEntityStatus
{
    PAYMENT,    //client to escrow
    COMMISSION, //escrow to platfrom revenue
    RELEASE,    //escrow to writer balance
    WITHDRAWAL_PROCESSING,  //funds in temporary withdrawal wallet
    WITHDRAWAL, // writer balance to  withdrawal processing
    REFUND, //  Escrow/Dispute to client
    DISPUTE //  Any disputed transfer
}
