package com.solocuerdas.solocuerdas_backend.dto;

import java.math.BigDecimal;

public class UserRatingResponse {

    private Long userId;
    private String userName;
    private BigDecimal ratingAsSeller;
    private Integer totalSales;
    private BigDecimal ratingAsBuyer;
    private Integer totalPurchases;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public BigDecimal getRatingAsSeller() {
        return ratingAsSeller;
    }

    public void setRatingAsSeller(BigDecimal ratingAsSeller) {
        this.ratingAsSeller = ratingAsSeller;
    }

    public Integer getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(Integer totalSales) {
        this.totalSales = totalSales;
    }

    public BigDecimal getRatingAsBuyer() {
        return ratingAsBuyer;
    }

    public void setRatingAsBuyer(BigDecimal ratingAsBuyer) {
        this.ratingAsBuyer = ratingAsBuyer;
    }

    public Integer getTotalPurchases() {
        return totalPurchases;
    }

    public void setTotalPurchases(Integer totalPurchases) {
        this.totalPurchases = totalPurchases;
    }
}
