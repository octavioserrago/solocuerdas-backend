package com.solocuerdas.solocuerdas_backend.model;

public enum InquiryStatus {
    OPEN, // Buyer sent interest, waiting for seller response
    ACCEPTED, // Seller accepted, chat enabled
    REJECTED, // Seller rejected the inquiry
    CANCELLED, // Buyer cancelled before seller responded
    CLOSED // Inquiry closed after transaction completed or expired
}
