package com.solocuerdas.solocuerdas_backend.controller;

import com.solocuerdas.solocuerdas_backend.dto.ConfirmTransactionRequest;
import com.solocuerdas.solocuerdas_backend.dto.InitiateTransactionRequest;
import com.solocuerdas.solocuerdas_backend.dto.TransactionResponse;
import com.solocuerdas.solocuerdas_backend.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * TRANSACTION CONTROLLER
 * Manages the in-person sale confirmation flow with a 6-digit code.
 *
 * Flow:
 * 1. Seller calls POST /api/inquiries/{id}/transaction → gets the code to show
 * buyer in person
 * 2. Buyer calls POST /api/transactions/{id}/confirm → enters the code to
 * complete the sale
 *
 * Base paths:
 * /api/inquiries/{inquiryId}/transaction
 * /api/transactions/{transactionId}
 */
@RestController
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    /**
     * INITIATE TRANSACTION (seller action)
     * POST /api/inquiries/{inquiryId}/transaction
     * Header: X-User-Id: <sellerId>
     * Body: { "agreedPrice": 1500.00 } (optional — defaults to publication price)
     *
     * Response includes the confirmationCode that the seller must show to the buyer
     * in person.
     */
    @PostMapping("/api/inquiries/{inquiryId}/transaction")
    public ResponseEntity<?> initiateTransaction(
            @PathVariable Long inquiryId,
            @RequestHeader("X-User-Id") Long sellerId,
            @RequestBody(required = false) InitiateTransactionRequest request) {
        try {
            if (request == null)
                request = new InitiateTransactionRequest();
            TransactionResponse response = transactionService.initiateTransaction(inquiryId, sellerId, request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * GET TRANSACTION FOR AN INQUIRY
     * GET /api/inquiries/{inquiryId}/transaction
     * Header: X-User-Id: <requesterId>
     *
     * The confirmationCode is only visible to the seller while status is
     * AWAITING_BUYER_CODE.
     */
    @GetMapping("/api/inquiries/{inquiryId}/transaction")
    public ResponseEntity<?> getByInquiry(
            @PathVariable Long inquiryId,
            @RequestHeader("X-User-Id") Long requesterId) {
        try {
            TransactionResponse response = transactionService.getByInquiry(inquiryId, requesterId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * CONFIRM TRANSACTION (buyer action — in person)
     * POST /api/transactions/{transactionId}/confirm
     * Header: X-User-Id: <buyerId>
     * Body: { "confirmationCode": "482931" }
     */
    @PostMapping("/api/transactions/{transactionId}/confirm")
    public ResponseEntity<?> confirmTransaction(
            @PathVariable Long transactionId,
            @RequestHeader("X-User-Id") Long buyerId,
            @RequestBody ConfirmTransactionRequest request) {
        try {
            TransactionResponse response = transactionService.confirmTransaction(
                    transactionId, buyerId, request.getConfirmationCode());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * CANCEL TRANSACTION (seller action, before buyer confirms)
     * POST /api/transactions/{transactionId}/cancel
     * Header: X-User-Id: <sellerId>
     */
    @PostMapping("/api/transactions/{transactionId}/cancel")
    public ResponseEntity<?> cancelTransaction(
            @PathVariable Long transactionId,
            @RequestHeader("X-User-Id") Long sellerId) {
        try {
            TransactionResponse response = transactionService.cancelTransaction(transactionId, sellerId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
