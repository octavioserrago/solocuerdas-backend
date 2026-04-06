package com.solocuerdas.solocuerdas_backend.dto;

public class CreateInquiryRequest {

    private Long publicationId;
    private String message;

    public Long getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(Long publicationId) {
        this.publicationId = publicationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
