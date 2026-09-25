package com.ecommerce.prices.domain.exception;

import com.ecommerce.prices.domain.model.PriceQuery;

public class PriceNotFoundException extends RuntimeException {

    public PriceNotFoundException(PriceQuery query) {
        super("No price applies to product %d of brand %d at %s".formatted(
                query.productId().value(), query.brandId().value(), query.applicationDate()));
    }
}
