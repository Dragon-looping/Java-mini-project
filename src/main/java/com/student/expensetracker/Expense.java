package com.student.expensetracker;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Model class representing a single expense entry.
 *
 * Demonstrates:
 * 1. Encapsulation: All state fields are declared private.
 * 2. Immutability of identity: The 'id' field is final so it never changes during edits.
 * 3. Proper Java data types: LocalDate for dates and BigDecimal for currency calculations.
 */
public class Expense {

    // Unique identifier for the expense (stable across edits)
    private final int id;

    // Date when the expense occurred
    private LocalDate date;

    // Category of expense (Food, Travel, Education, etc.)
    private String category;

    // Amount spent in Indian Rupees (BigDecimal ensures exact precision without floating-point errors)
    private BigDecimal amount;

    // Optional short description/note of the expense
    private String description;

    /**
     * Constructor to initialize an Expense object with all fields.
     */
    public Expense(int id, LocalDate date, String category, BigDecimal amount, String description) {
        this.id = id;
        this.date = date;
        this.category = category;
        this.amount = amount;
        this.description = (description != null) ? description.trim() : "";
    }

    // --- Getters ---

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    // --- Setters for editable fields (id remains unchanged) ---

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = (description != null) ? description.trim() : "";
    }

    @Override
    public String toString() {
        return "Expense{" +
                "id=" + id +
                ", date=" + date +
                ", category='" + category + '\'' +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                '}';
    }
}
