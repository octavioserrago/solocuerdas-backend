package com.solocuerdas.solocuerdas_backend.model;

public enum TransactionStatus {
    AWAITING_BUYER_CODE, // Seller confirmed, code generated, waiting for buyer to enter it
    COMPLETED, // Buyer entered valid code, sale confirmed
    EXPIRED, // Code expired (24h), transaction cancelled
    CANCELLED // Manually cancelled before completion
}
