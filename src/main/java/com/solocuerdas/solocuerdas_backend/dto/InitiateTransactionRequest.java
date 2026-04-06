package com.solocuerdas.solocuerdas_backend.dto;

import java.math.BigDecimal;

public class InitiateTransactionRequest {

    private BigDecimal agreedPrice;

    public BigDecimal getAgreedPrice() {
        return agreedPrice;
    }

    public void setAgreedPrice(BigDecimal agreedPrice) {
        this.agreedPrice = agreedPrice;
    }
}
