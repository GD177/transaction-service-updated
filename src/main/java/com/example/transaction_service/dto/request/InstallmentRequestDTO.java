package com.example.transaction_service.dto.request;

import java.math.BigDecimal;

public class InstallmentRequestDTO {
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
