package com.example.demo.dto;

import java.math.BigDecimal;

public record Currency(
        String symbol,
        BigDecimal price
) {
}
