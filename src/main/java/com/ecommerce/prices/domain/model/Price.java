package com.ecommerce.prices.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

public record Price(
        BrandId brandId,
        ProductId productId,
        long priceList,
        LocalDateTime startDate,
        LocalDateTime endDate,
        int priority,
        BigDecimal amount,
        Currency currency) {
}
