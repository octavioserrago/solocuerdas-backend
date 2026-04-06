package com.solocuerdas.solocuerdas_backend.service;

import com.solocuerdas.solocuerdas_backend.dto.MessageResponse;
import com.solocuerdas.solocuerdas_backend.dto.SendMessageRequest;
import com.solocuerdas.solocuerdas_backend.model.Inquiry;
import com.solocuerdas.solocuerdas_backend.model.InquiryStatus;
import com.solocuerdas.solocuerdas_backend.model.Message;
import com.solocuerdas.solocuerdas_backend.model.Usuario;
import com.solocuerdas.solocuerdas_backend.repository.MessageRepository;
import com.solocuerdas.solocuerdas_backend.repository.UsuarioRepository;
import com.solocuerdas.solocuerdas_backend.repository.InquiryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private InquiryRepository inquiryRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PushNotificationService pushNotificationService;

    /**
     * SEND MESSAGE
     * Only buyer and seller of an ACCEPTED inquiry can send messages.
     */
    public MessageResponse sendMessage(Long inquiryId, Long senderId, SendMessageRequest request) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("Inquiry not found"));

        if (inquiry.getStatus() != InquiryStatus.ACCEPTED) {
            throw new RuntimeException("Messaging is only available for accepted inquiries.");
        }

        validateParticipant(inquiry, senderId);

        if (request.getBody() == null || request.getBody().trim().isEmpty()) {
            throw new RuntimeException("Message body cannot be empty.");
        }

        Usuario sender = usuarioRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Message message = new Message(inquiry, sender, request.getBody().trim());
        Message saved = messageRepository.save(message);

        // Notify the other participant
        Usuario recipient = inquiry.getBuyer().getId().equals(senderId)
                ? inquiry.getSeller()
                : inquiry.getBuyer();
        pushNotificationService.send(recipient,
                "Nuevo mensaje de " + sender.getName(),
                request.getBody().trim().length() > 60
                        ? request.getBody().trim().substring(0, 60) + "..."
                        : request.getBody().trim());

        return mapToResponse(saved);
    }

    /**
     * GET MESSAGES FOR AN INQUIRY
     * Marks unread messages from the other party as read.
     */
    public List<MessageResponse> getMessages(Long inquiryId, Long requesterId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("Inquiry not found"));

        validateParticipant(inquiry, requesterId);

        List<Message> messages = messageRepository.findByInquiryOrderBySentAtAsc(inquiry);

        // Mark messages from the other party as read
        messages.stream()
                .filter(m -> !m.getSender().getId().equals(requesterId) && m.getReadAt() == null)
                .forEach(m -> {
                    m.setReadAt(LocalDateTime.now());
                    messageRepository.save(m);
                });

        return messages.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ============ HELPERS ============

    private void validateParticipant(Inquiry inquiry, Long userId) {
        boolean isBuyer = inquiry.getBuyer().getId().equals(userId);
        boolean isSeller = inquiry.getSeller().getId().equals(userId);
        if (!isBuyer && !isSeller) {
            throw new RuntimeException("You are not a participant in this inquiry.");
        }
    }

    private MessageResponse mapToResponse(Message m) {
        MessageResponse r = new MessageResponse();
        r.setId(m.getId());
        r.setInquiryId(m.getInquiry().getId());
        r.setSenderId(m.getSender().getId());
        r.setSenderName(m.getSender().getName());
        r.setBody(m.getBody());
        r.setSentAt(m.getSentAt());
        r.setReadAt(m.getReadAt());
        return r;
    }
}
