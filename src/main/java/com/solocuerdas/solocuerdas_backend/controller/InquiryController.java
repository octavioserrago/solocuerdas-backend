package com.solocuerdas.solocuerdas_backend.controller;

import com.solocuerdas.solocuerdas_backend.dto.CreateInquiryRequest;
import com.solocuerdas.solocuerdas_backend.dto.InquiryResponse;
import com.solocuerdas.solocuerdas_backend.model.InquiryStatus;
import com.solocuerdas.solocuerdas_backend.service.InquiryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * INQUIRY CONTROLLER
 * Manages buyer interest expressions and seller responses.
 *
 * Base path: /api/inquiries
 */
@RestController
@RequestMapping("/api/inquiries")
public class InquiryController {

    @Autowired
    private InquiryService inquiryService;

    /**
     * CREATE INQUIRY (buyer action)
     * POST /api/inquiries
     * Header: X-User-Id: <buyerId>
     */
    @PostMapping
    public ResponseEntity<?> createInquiry(
            @RequestHeader("X-User-Id") Long buyerId,
            @RequestBody CreateInquiryRequest request) {
        try {
            InquiryResponse response = inquiryService.createInquiry(buyerId, request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * GET INQUIRY BY ID (buyer or seller)
     * GET /api/inquiries/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getInquiry(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long requesterId) {
        try {
            InquiryResponse response = inquiryService.getInquiry(id, requesterId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * GET MY INQUIRIES AS BUYER
     * GET /api/inquiries/as-buyer
     */
    @GetMapping("/as-buyer")
    public ResponseEntity<?> getBuyerInquiries(
            @RequestHeader("X-User-Id") Long buyerId) {
        try {
            List<InquiryResponse> response = inquiryService.getBuyerInquiries(buyerId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * GET MY INQUIRIES AS SELLER
     * GET /api/inquiries/as-seller
     */
    @GetMapping("/as-seller")
    public ResponseEntity<?> getSellerInquiries(
            @RequestHeader("X-User-Id") Long sellerId) {
        try {
            List<InquiryResponse> response = inquiryService.getSellerInquiries(sellerId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * ACCEPT INQUIRY (seller action)
     * POST /api/inquiries/{id}/accept
     */
    @PostMapping("/{id}/accept")
    public ResponseEntity<?> acceptInquiry(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long sellerId) {
        try {
            InquiryResponse response = inquiryService.respondToInquiry(id, sellerId, InquiryStatus.ACCEPTED);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * REJECT INQUIRY (seller action)
     * POST /api/inquiries/{id}/reject
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectInquiry(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long sellerId) {
        try {
            InquiryResponse response = inquiryService.respondToInquiry(id, sellerId, InquiryStatus.REJECTED);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * CANCEL INQUIRY (buyer action)
     * POST /api/inquiries/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelInquiry(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long buyerId) {
        try {
            InquiryResponse response = inquiryService.cancelInquiry(id, buyerId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
