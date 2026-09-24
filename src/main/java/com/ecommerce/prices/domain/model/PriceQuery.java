package com.ecommerce.prices.domain.model;

import java.time.LocalDateTime;

public record PriceQuery(long brandId, long productId, LocalDateTime applicationDate) {
}
