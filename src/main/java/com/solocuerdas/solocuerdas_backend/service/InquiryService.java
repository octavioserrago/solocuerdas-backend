package com.solocuerdas.solocuerdas_backend.service;

import com.solocuerdas.solocuerdas_backend.dto.CreateInquiryRequest;
import com.solocuerdas.solocuerdas_backend.dto.InquiryResponse;
import com.solocuerdas.solocuerdas_backend.model.*;
import com.solocuerdas.solocuerdas_backend.repository.InquiryRepository;
import com.solocuerdas.solocuerdas_backend.repository.MessageRepository;
import com.solocuerdas.solocuerdas_backend.repository.PublicationRepository;
import com.solocuerdas.solocuerdas_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InquiryService {

    // Anti-fraud limits
    private static final int MAX_WEEKLY_TRANSACTIONS = 3;
    private static final int MAX_SAME_USER_MONTHLY_TRANSACTIONS = 1;

    @Autowired
    private InquiryRepository inquiryRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PushNotificationService pushNotificationService;

    /**
     * CREATE INQUIRY
     * Buyer expresses interest in a publication.
     * Anti-fraud: blocks users with pending reviews, prevents duplicate inquiries,
     * and restricts repeated inquiries between the same pair of users.
     */
    public InquiryResponse createInquiry(Long buyerId, CreateInquiryRequest request) {
        Usuario buyer = usuarioRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Boolean.TRUE.equals(buyer.getHasPendingReview())) {
            throw new RuntimeException(
                    "You must submit your pending review before starting new inquiries.");
        }

        Publication publication = publicationRepository.findById(request.getPublicationId())
                .orElseThrow(() -> new RuntimeException("Publication not found"));

        if (publication.getStatus() != PublicationStatus.ACTIVE) {
            throw new RuntimeException("This publication is no longer available.");
        }

        Usuario seller = publication.getUser();

        if (seller.getId().equals(buyerId)) {
            throw new RuntimeException("You cannot send an inquiry to your own publication.");
        }

        if (inquiryRepository.existsByBuyerIdAndPublicationId(buyerId, request.getPublicationId())) {
            throw new RuntimeException("You already have an open inquiry for this publication.");
        }

        // Anti-fraud: max 1 ongoing interaction between same buyer/seller pair
        long activeWithSeller = inquiryRepository.countActiveInquiriesBetween(buyerId, seller.getId());
        if (activeWithSeller >= MAX_SAME_USER_MONTHLY_TRANSACTIONS) {
            throw new RuntimeException(
                    "You already have an active inquiry with this seller.");
        }

        Inquiry inquiry = new Inquiry(buyer, seller, publication, request.getMessage());
        Inquiry saved = inquiryRepository.save(inquiry);

        pushNotificationService.send(seller,
                "Nueva consulta",
                buyer.getName() + " está interesado en tu publicación \"" + publication.getTitle() + "\"");

        return mapToResponse(saved, 0L);
    }

    /**
     * RESPOND TO INQUIRY (seller accepts or rejects)
     */
    public InquiryResponse respondToInquiry(Long inquiryId, Long sellerId, InquiryStatus newStatus) {
        Inquiry inquiry = findAndValidateSeller(inquiryId, sellerId);

        if (inquiry.getStatus() != InquiryStatus.OPEN) {
            throw new RuntimeException("This inquiry is not open.");
        }

        if (newStatus != InquiryStatus.ACCEPTED && newStatus != InquiryStatus.REJECTED) {
            throw new RuntimeException("Invalid status. Use ACCEPTED or REJECTED.");
        }

        inquiry.setStatus(newStatus);
        Inquiry saved = inquiryRepository.save(inquiry);

        if (newStatus == InquiryStatus.ACCEPTED) {
            pushNotificationService.send(inquiry.getBuyer(),
                    "Consulta aceptada",
                    "¡" + inquiry.getSeller().getName() + " aceptó tu consulta por \""
                            + inquiry.getPublication().getTitle() + "\"! Ya podés chatear.");
        } else {
            pushNotificationService.send(inquiry.getBuyer(),
                    "Consulta rechazada",
                    inquiry.getSeller().getName() + " no pudo atender tu consulta por \""
                            + inquiry.getPublication().getTitle() + "\".");
        }

        return mapToResponse(saved,
                messageRepository.countByInquiryIdAndReadAtIsNullAndSenderIdNot(inquiryId, sellerId));
    }

    /**
     * CANCEL INQUIRY (buyer cancels their own inquiry)
     */
    public InquiryResponse cancelInquiry(Long inquiryId, Long buyerId) {
        Inquiry inquiry = findAndValidateBuyer(inquiryId, buyerId);

        if (inquiry.getStatus() == InquiryStatus.CLOSED) {
            throw new RuntimeException("This inquiry is already closed.");
        }

        inquiry.setStatus(InquiryStatus.CANCELLED);
        return mapToResponse(inquiryRepository.save(inquiry), 0L);
    }

    /**
     * GET ALL INQUIRIES FOR A BUYER
     */
    public List<InquiryResponse> getBuyerInquiries(Long buyerId) {
        Usuario buyer = usuarioRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return inquiryRepository.findByBuyerOrderByCreatedAtDesc(buyer).stream()
                .map(i -> mapToResponse(i,
                        messageRepository.countByInquiryIdAndReadAtIsNullAndSenderIdNot(i.getId(), buyerId)))
                .collect(Collectors.toList());
    }

    /**
     * GET ALL INQUIRIES FOR A SELLER
     */
    public List<InquiryResponse> getSellerInquiries(Long sellerId) {
        Usuario seller = usuarioRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return inquiryRepository.findBySellerOrderByCreatedAtDesc(seller).stream()
                .map(i -> mapToResponse(i,
                        messageRepository.countByInquiryIdAndReadAtIsNullAndSenderIdNot(i.getId(), sellerId)))
                .collect(Collectors.toList());
    }

    /**
     * GET INQUIRY BY ID (buyer or seller can access their own)
     */
    public InquiryResponse getInquiry(Long inquiryId, Long requesterId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("Inquiry not found"));

        if (!inquiry.getBuyer().getId().equals(requesterId) &&
                !inquiry.getSeller().getId().equals(requesterId)) {
            throw new RuntimeException("You don't have access to this inquiry.");
        }

        long unread = messageRepository.countByInquiryIdAndReadAtIsNullAndSenderIdNot(inquiryId, requesterId);
        return mapToResponse(inquiry, unread);
    }

    // ============ HELPERS ============

    public Inquiry findAndValidateSeller(Long inquiryId, Long sellerId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("Inquiry not found"));
        if (!inquiry.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("You are not the seller of this inquiry.");
        }
        return inquiry;
    }

    public Inquiry findAndValidateBuyer(Long inquiryId, Long buyerId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("Inquiry not found"));
        if (!inquiry.getBuyer().getId().equals(buyerId)) {
            throw new RuntimeException("You are not the buyer of this inquiry.");
        }
        return inquiry;
    }

    public Inquiry getInquiryEntity(Long inquiryId) {
        return inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("Inquiry not found"));
    }

    public void closeInquiry(Inquiry inquiry) {
        inquiry.setStatus(InquiryStatus.CLOSED);
        inquiryRepository.save(inquiry);
    }

    private InquiryResponse mapToResponse(Inquiry inquiry, long unread) {
        InquiryResponse r = new InquiryResponse();
        r.setId(inquiry.getId());
        r.setBuyerId(inquiry.getBuyer().getId());
        r.setBuyerName(inquiry.getBuyer().getName());
        r.setSellerId(inquiry.getSeller().getId());
        r.setSellerName(inquiry.getSeller().getName());
        r.setPublicationId(inquiry.getPublication().getId());
        r.setPublicationTitle(inquiry.getPublication().getTitle());
        r.setMessage(inquiry.getMessage());
        r.setStatus(inquiry.getStatus());
        r.setCreatedAt(inquiry.getCreatedAt());
        r.setUpdatedAt(inquiry.getUpdatedAt());
        r.setUnreadMessages(unread);
        return r;
    }
}
