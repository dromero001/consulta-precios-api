package com.ecommerce.prices.domain.model;

import java.time.LocalDateTime;

public record PriceQuery(BrandId brandId, ProductId productId, LocalDateTime applicationDate) {
}
