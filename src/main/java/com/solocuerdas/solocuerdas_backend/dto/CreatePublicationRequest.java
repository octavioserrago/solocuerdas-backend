package com.solocuerdas.solocuerdas_backend.dto;

import com.solocuerdas.solocuerdas_backend.model.Category;
import com.solocuerdas.solocuerdas_backend.model.Condition;

import java.math.BigDecimal;

/**
 * DTO for creating a new publication
 */
public class CreatePublicationRequest {

    private String title;
    private String description;
    private BigDecimal price;
    private Category category;
    private Condition condition;
    private String brand;
    private Integer year;
    private String location;

    // Constructors
    public CreatePublicationRequest() {
    }

    public CreatePublicationRequest(String title, String description, BigDecimal price,
            Category category, Condition condition, String location) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.condition = condition;
        this.location = location;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
