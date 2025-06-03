package com.ebanca.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStatistics {
    private BigDecimal totalAmount;
    private long count;
    private BigDecimal average;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
} 