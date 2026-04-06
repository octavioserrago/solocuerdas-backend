package com.solocuerdas.solocuerdas_backend.controller;

import com.solocuerdas.solocuerdas_backend.dto.MessageResponse;
import com.solocuerdas.solocuerdas_backend.dto.SendMessageRequest;
import com.solocuerdas.solocuerdas_backend.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * MESSAGE CONTROLLER
 * In-app chat for accepted inquiries.
 *
 * Base path: /api/inquiries/{inquiryId}/messages
 */
@RestController
@RequestMapping("/api/inquiries/{inquiryId}/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * SEND MESSAGE
     * POST /api/inquiries/{inquiryId}/messages
     * Header: X-User-Id: <senderId>
     */
    @PostMapping
    public ResponseEntity<?> sendMessage(
            @PathVariable Long inquiryId,
            @RequestHeader("X-User-Id") Long senderId,
            @RequestBody SendMessageRequest request) {
        try {
            MessageResponse response = messageService.sendMessage(inquiryId, senderId, request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * GET ALL MESSAGES FOR AN INQUIRY
     * Also marks unread messages from the other party as read.
     * GET /api/inquiries/{inquiryId}/messages
     * Header: X-User-Id: <requesterId>
     */
    @GetMapping
    public ResponseEntity<?> getMessages(
            @PathVariable Long inquiryId,
            @RequestHeader("X-User-Id") Long requesterId) {
        try {
            List<MessageResponse> response = messageService.getMessages(inquiryId, requesterId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
