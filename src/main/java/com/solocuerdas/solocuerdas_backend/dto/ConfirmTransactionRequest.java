package com.solocuerdas.solocuerdas_backend.dto;

public class ConfirmTransactionRequest {

    private String confirmationCode;

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }
}
