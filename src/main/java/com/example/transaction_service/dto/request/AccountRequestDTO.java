package com.example.transaction_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public class AccountRequestDTO {
    @NotBlank(message = "Document number is required")
    @Size(min = 9, max = 12, message = "Document number must be between 9 and 12 characters long")
    @Pattern(regexp = "^[a-zA-Z0-9-]*$", message = "Document number must contain only alphanumeric characters and hyphens (-)")
    private String documentNumber;

    // Getters and setters
    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }
}