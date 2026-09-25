package com.ecommerce.prices.domain.exception;

import com.ecommerce.prices.domain.model.BrandId;

public class BrandNotFoundException extends RuntimeException {

    public BrandNotFoundException(BrandId brandId) {
        super("Brand %d not found".formatted(brandId.value()));
    }
}
